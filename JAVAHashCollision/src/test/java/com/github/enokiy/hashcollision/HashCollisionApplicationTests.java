package com.github.enokiy.hashcollision;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HashCollisionApplicationTests {

    @Test
    void contextLoads() {
    }


    @Test
    public void test() throws Exception {
        String cmd = "import java.nio.file.Paths;" +
                "Paths.get(new java.net.URI(\"file:///j:/tmp/flag.txt\"));";

//        String cmd1 = "import java.nio.file.Paths;" +
//                "Paths.get(\".\\flag.txt\");";   //抛异常 没有找到java.lang.Class的方法：get(java.lang.String) ？？ 为啥？
//        System.out.println(cmd1);
        ExpressRunner runner = new ExpressRunner();
        DefaultContext<String,Object> context = new DefaultContext<>();
//            Object r = runner.execute(cmd,context,null,true,false);
        runner.execute(cmd,context,null,true,false);
    }

}
