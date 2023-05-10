package com.github.enokiy.hashcollision.Controller;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
public class QlApplication {
    @RequestMapping({"/"})
    public String index(){
        return "welcome! :)";
    }

    @RequestMapping({"/exp"})
    public String exp(@RequestBody Map params) throws Exception {
        String key  = (String)params.get("token").toString();
        String secret = "HFCTF2022";
        if (key.hashCode() == secret.hashCode() && !secret.equals(key)) {
            String cmd = params.get("cmd").toString();
            Pattern pattern = Pattern.compile("Process|runtime|javascript|\\+|char|\\\\|from|\\|\\]|load",2);
            if (pattern.matcher(cmd).find()){
                return "nononono!!";
            }

            ExpressRunner runner = new ExpressRunner();
            DefaultContext<String,Object> context = new DefaultContext<>();
            Object r = runner.execute(cmd,context,null,true,false);
            return "hack me";
        }

        return key;
    }
}
