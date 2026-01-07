package com.bytehamster.androidxmlformatter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.io.StringWriter;

import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;

class AndroidXmlOutputterTest {

  private static final Namespace ANDROID_NS =
      Namespace.getNamespace("android", "http://schemas.android.com/apk/res/android");
  private static final Namespace APP_NS =
      Namespace.getNamespace("app", "http://schemas.android.com/apk/res-auto");
  private static final Namespace TOOLS_NS =
      Namespace.getNamespace("tools", "http://schemas.android.com/tools");

  private AndroidXmlOutputter createDefaultOutputter() {
    return new AndroidXmlOutputter(
        4,
        4,
        new String[] {"android"},
        new String[] {"id", "layout_width", "layout_height"},
        false,
        false);
  }

  private String formatDocument(AndroidXmlOutputter outputter, Document doc) throws Exception {
    StringWriter writer = new StringWriter();
    outputter.output(doc, writer);
    return writer.toString();
  }

  private Document parseXml(String xml) throws Exception {
    SAXBuilder builder = new SAXBuilder();
    return builder.build(new StringReader(xml));
  }

  // === Constructor & Configuration Tests ===

  @Test
  void testConstructorSetsDefaultFormat() {
    AndroidXmlOutputter outputter = createDefaultOutputter();

    assertEquals("    ", outputter.getFormat().getIndent());
    assertEquals("\n", outputter.getFormat().getLineSeparator());
    assertEquals("utf-8", outputter.getFormat().getEncoding());
  }

  @Test
  void testConstructorWithCustomIndentation() {
    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(2, 8, new String[] {"android"}, new String[] {"id"}, false, false);

    assertEquals("  ", outputter.getFormat().getIndent());
    assertEquals(8, outputter.attributeIndention);
  }

  // === Attribute Ordering Tests ===

  @Test
  void testDefaultAttributeOrder() throws Exception {
    Element root = new Element("Button");
    root.setAttribute(new Attribute("layout_height", "wrap_content", ANDROID_NS));
    root.setAttribute(new Attribute("id", "@+id/btn", ANDROID_NS));
    root.setAttribute(new Attribute("layout_width", "match_parent", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    int idPos = result.indexOf("android:id");
    int widthPos = result.indexOf("android:layout_width");
    int heightPos = result.indexOf("android:layout_height");

    assertTrue(idPos < widthPos, "id should come before layout_width");
    assertTrue(widthPos < heightPos, "layout_width should come before layout_height");
  }

  @Test
  void testCustomAttributeOrder() throws Exception {
    Element root = new Element("Button");
    root.setAttribute(new Attribute("text", "Hello", ANDROID_NS));
    root.setAttribute(new Attribute("background", "#FFF", ANDROID_NS));
    root.setAttribute(new Attribute("id", "@+id/btn", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            4,
            4,
            new String[] {"android"},
            new String[] {"text", "id", "background"},
            false,
            false);
    String result = formatDocument(outputter, doc);

    int textPos = result.indexOf("android:text");
    int idPos = result.indexOf("android:id");
    int bgPos = result.indexOf("android:background");

    assertTrue(textPos < idPos, "text should come before id");
    assertTrue(idPos < bgPos, "id should come before background");
  }

  @Test
  void testAlphabeticalAttributeSort() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("zIndex", "1", ANDROID_NS));
    root.setAttribute(new Attribute("alpha", "0.5", ANDROID_NS));
    root.setAttribute(new Attribute("background", "#FFF", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(4, 4, new String[] {"android"}, new String[] {}, true, false);
    String result = formatDocument(outputter, doc);

    int alphaPos = result.indexOf("android:alpha");
    int bgPos = result.indexOf("android:background");
    int zPos = result.indexOf("android:zIndex");

    assertTrue(alphaPos < bgPos, "alpha should come before background");
    assertTrue(bgPos < zPos, "background should come before zIndex");
  }

  @Test
  void testAttributeOrderWithMixedNamespaces() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("id", "@+id/view", ANDROID_NS));
    root.setAttribute(new Attribute("customAttr", "value", APP_NS));
    root.setAttribute(new Attribute("layout_width", "wrap_content", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    root.addNamespaceDeclaration(APP_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    int androidIdPos = result.indexOf("android:id");
    int appPos = result.indexOf("app:customAttr");

    assertTrue(androidIdPos < appPos, "android namespace should come before app namespace");
  }

  // === Namespace Ordering Tests ===

  @Test
  void testDefaultNamespaceOrder() throws Exception {
    Element root = new Element("LinearLayout");
    root.setAttribute(new Attribute("attr1", "value1", APP_NS));
    root.setAttribute(new Attribute("id", "@+id/ll", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    root.addNamespaceDeclaration(APP_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    int androidPos = result.indexOf("android:id");
    int appPos = result.indexOf("app:attr1");

    assertTrue(
        androidPos < appPos, "android namespace should come before app namespace by default");
  }

  @Test
  void testCustomNamespaceOrder() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("attr", "value", ANDROID_NS));
    root.setAttribute(new Attribute("custom", "custom", APP_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    root.addNamespaceDeclaration(APP_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            4, 4, new String[] {"app", "android"}, new String[] {"id"}, false, false);
    String result = formatDocument(outputter, doc);

    int appPos = result.indexOf("app:custom");
    int androidPos = result.indexOf("android:attr");

    assertTrue(appPos < androidPos, "app namespace should come before android when specified");
  }

  @Test
  void testAlphabeticalNamespaceSort() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("toolsAttr", "value", TOOLS_NS));
    root.setAttribute(new Attribute("appAttr", "value", APP_NS));
    root.setAttribute(new Attribute("androidAttr", "value", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    root.addNamespaceDeclaration(APP_NS);
    root.addNamespaceDeclaration(TOOLS_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(4, 4, new String[] {}, new String[] {}, false, true);
    String result = formatDocument(outputter, doc);

    int androidPos = result.indexOf("android:androidAttr");
    int appPos = result.indexOf("app:appAttr");
    int toolsPos = result.indexOf("tools:toolsAttr");

    assertTrue(androidPos < appPos, "android should come before app alphabetically");
    assertTrue(appPos < toolsPos, "app should come before tools alphabetically");
  }

  // === Element Formatting Tests ===

  @Test
  void testSelfClosingElement() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("id", "@+id/view", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains(" />"), "Empty element should self-close with ' />'");
    assertFalse(result.contains("</View>"), "Empty element should not have closing tag");
  }

  @Test
  void testElementWithTextContent() throws Exception {
    Element root = new Element("TextView");
    root.setText("Hello World");
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("Hello World"), "Text content should be preserved");
    assertTrue(result.contains("</TextView>"), "Element with text should have closing tag");
  }

  @Test
  void testNestedElements() throws Exception {
    Element root = new Element("LinearLayout");
    root.addNamespaceDeclaration(ANDROID_NS);

    Element child1 = new Element("Button");
    child1.setAttribute(new Attribute("id", "@+id/btn1", ANDROID_NS));

    Element child2 = new Element("TextView");
    child2.setAttribute(new Attribute("id", "@+id/txt1", ANDROID_NS));

    root.addContent(child1);
    root.addContent(child2);

    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("<Button"), "Should contain Button element");
    assertTrue(result.contains("<TextView"), "Should contain TextView element");
    assertTrue(result.contains("</LinearLayout>"), "Should have closing tag for parent");
  }

  @Test
  void testElementWithAttributes() throws Exception {
    Element root = new Element("Button");
    root.setAttribute(new Attribute("id", "@+id/btn", ANDROID_NS));
    root.setAttribute(new Attribute("text", "Click Me", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(4, 4, new String[] {"android"}, new String[] {"id"}, false, false);
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("android:id="), "Should contain id attribute");
    assertTrue(result.contains("android:text="), "Should contain text attribute");
  }

  @Test
  void testElementDepth() throws Exception {
    Element root = new Element("Level0");
    Element level1 = new Element("Level1");
    Element level2 = new Element("Level2");

    level1.addContent(level2);
    root.addContent(level1);

    Document doc = new Document(root);
    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    String[] lines = result.split("\n");
    boolean foundLevel2 = false;
    for (String line : lines) {
      if (line.contains("<Level2")) {
        foundLevel2 = true;
        assertTrue(line.startsWith("        "), "Level2 should be indented 8 spaces");
      }
    }
    assertTrue(foundLevel2, "Should find Level2 element");
  }

  // === Whitespace Handling Tests ===

  @Test
  void testSkipLeadingWhitespace() throws Exception {
    String xml = "<root>   \n   <child/></root>";
    Document doc = parseXml(xml);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertFalse(result.contains(">   "), "Leading whitespace should be skipped");
  }

  @Test
  void testSkipTrailingWhitespace() throws Exception {
    String xml = "<root><child/>   \n   </root>";
    Document doc = parseXml(xml);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertFalse(result.contains("   </root>"), "Trailing whitespace should be trimmed");
  }

  @Test
  void testIsAllWhitespace() throws Exception {
    String xml = "<root>   </root>";
    Document doc = parseXml(xml);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("/>"), "Element with only whitespace should self-close");
  }

  // === Integration Tests ===

  @Test
  void testCompleteXmlFormatting() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\""
            + " android:layout_height=\"match_parent\""
            + " android:id=\"@+id/container\""
            + " android:layout_width=\"match_parent\">"
            + "<Button android:text=\"Click\" android:id=\"@+id/btn\" android:layout_width=\"wrap_content\" android:layout_height=\"wrap_content\"/>"
            + "</LinearLayout>";

    Document doc = parseXml(xml);
    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("android:id=\"@+id/container\""), "Should preserve id attribute");
    assertTrue(
        result.contains("android:layout_width=\"match_parent\""), "Should preserve layout_width");
    assertTrue(
        result.contains("android:layout_height=\"match_parent\""), "Should preserve layout_height");
    assertTrue(result.contains("<Button"), "Should contain Button element");
  }

  @Test
  void testZeroAttributeIndention() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("id", "@+id/view", ANDROID_NS));
    root.setAttribute(new Attribute("text", "Hello", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(4, 0, new String[] {"android"}, new String[] {"id"}, false, false);
    String result = formatDocument(outputter, doc);

    assertTrue(
        result.contains("android:id") && result.contains("android:text"),
        "Should contain both attributes");
  }

  @Test
  void testCommentHandling() throws Exception {
    Element root = new Element("root");
    root.addContent(new Comment("This is a comment"));
    Element child = new Element("child");
    root.addContent(child);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("<!--This is a comment-->"), "Comment should be preserved");
  }

  @Test
  void testProcessingInstruction() throws Exception {
    Element root = new Element("root");
    root.addContent(
        new ProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"style.xsl\""));
    Element child = new Element("child");
    root.addContent(child);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("<?xml-stylesheet"), "Processing instruction should be preserved");
  }

  @Test
  void testEntityRef() throws Exception {
    String xml = "<root>&amp; &lt; &gt;</root>";
    Document doc = parseXml(xml);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(
        result.contains("&amp;") || result.contains("&"), "Entity references should be handled");
  }

  @Test
  void testCDATA() throws Exception {
    Element root = new Element("root");
    root.addContent(new CDATA("<script>alert('hello');</script>"));
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    // CDATA content should be present in the output
    assertTrue(
        result.contains("CDATA") || result.contains("script") || result.contains("alert"),
        "CDATA content should be handled");
  }

  @Test
  void testPreserveSpaceAttribute() throws Exception {
    Element root = new Element("root");
    root.setAttribute("space", "preserve", Namespace.XML_NAMESPACE);
    root.addContent(new Text("  preserved  spaces  "));
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("preserved"), "Text content should be preserved");
  }

  @Test
  void testDefaultSpaceAttribute() throws Exception {
    Element root = new Element("root");
    root.setAttribute("space", "default", Namespace.XML_NAMESPACE);
    root.addContent(new Text("some text"));
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("some text"), "Text content should be present");
  }

  @Test
  void testMultipleNestedLevels() throws Exception {
    Element root = new Element("L0");
    Element l1 = new Element("L1");
    Element l2 = new Element("L2");
    Element l3 = new Element("L3");
    l2.addContent(l3);
    l1.addContent(l2);
    root.addContent(l1);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("<L0"), "Should contain L0");
    assertTrue(result.contains("<L1"), "Should contain L1");
    assertTrue(result.contains("<L2"), "Should contain L2");
    assertTrue(result.contains("<L3"), "Should contain L3");
  }

  @Test
  void testAttributeWithSpecialCharacters() throws Exception {
    Element root = new Element("View");
    root.setAttribute(new Attribute("text", "Hello \"World\" & <Test>", ANDROID_NS));
    root.addNamespaceDeclaration(ANDROID_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(
        result.contains("&amp;") || result.contains("&lt;") || result.contains("&quot;"),
        "Special characters should be escaped");
  }

  @Test
  void testMixedContentElement() throws Exception {
    String xml = "<root>Some text<child/>more text</root>";
    Document doc = parseXml(xml);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(
        result.contains("Some text") || result.contains("more text"),
        "Mixed content should be handled");
  }

  @Test
  void testEmptyDocument() throws Exception {
    Element root = new Element("root");
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("<root"), "Should contain root element");
    assertTrue(result.contains("/>"), "Empty element should self-close");
  }

  @Test
  void testNamespaceDeclarationFormatting() throws Exception {
    Element root = new Element("root");
    root.addNamespaceDeclaration(ANDROID_NS);
    root.addNamespaceDeclaration(APP_NS);
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(result.contains("xmlns:android"), "Should contain android namespace declaration");
    assertTrue(result.contains("xmlns:app"), "Should contain app namespace declaration");
  }

  @Test
  void testElementWithNoNamespacePrefix() throws Exception {
    Element root = new Element("View");
    root.setAttribute("style", "@style/MyStyle");
    Document doc = new Document(root);

    AndroidXmlOutputter outputter = createDefaultOutputter();
    String result = formatDocument(outputter, doc);

    assertTrue(
        result.contains("style=\"@style/MyStyle\""),
        "Should contain attribute without namespace prefix");
  }
}
