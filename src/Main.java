import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception {
        String input = IOUtils.resourceToString("test.xml", StandardCharsets.UTF_8, Main.class.getClassLoader());
        String formatted = format(input);
        System.out.println(formatted);
    }

    static String format(String input) throws IOException, JDOMException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        XMLOutputter outputter = new AndroidXmlOutputter();
        Format format = Format.getPrettyFormat();
        format.setIndent("    ");
        outputter.setFormat(format);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        outputter.output(doc, stream);
        return stream.toString();
    }
}
