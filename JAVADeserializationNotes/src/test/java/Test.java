import com.github.enokiy.deserialization.Constants;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Test {

    @org.junit.Test
    public void testexecByYaml() throws NamingException {
        InitialContext ctx = new InitialContext();
        ctx.lookup(Constants.rmiUrl + "execByYaml");
    }

    @org.junit.Test
    public void testexecByEL() throws NamingException {
        InitialContext ctx = new InitialContext();
        ctx.lookup(Constants.rmiUrl + "execByEL");
    }

    @org.junit.Test
    public void testexecByGroovyClassLoader() throws NamingException {
        InitialContext ctx = new InitialContext();
        ctx.lookup(Constants.rmiUrl + "execByGroovyClassLoader");
    }

    @org.junit.Test
    public void testexecByGroovyShell() throws NamingException {
        InitialContext ctx = new InitialContext();
        ctx.lookup(Constants.rmiUrl + "execByGroovyShell");
    }
}
