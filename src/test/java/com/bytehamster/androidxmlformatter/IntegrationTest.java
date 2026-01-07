package com.bytehamster.androidxmlformatter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests that verify XML formatting with various options by comparing input XML files
 * against expected output XML files.
 */
class IntegrationTest {

  private static final String INPUT_DIR = "/integration/input/";
  private static final String EXPECTED_DIR = "/integration/expected/";

  private String loadResource(String path) throws Exception {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + path);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private Document parseResource(String path) throws Exception {
    try (InputStream is = getClass().getResourceAsStream(path)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + path);
      }
      return new SAXBuilder().build(is);
    }
  }

  private String formatDocument(AndroidXmlOutputter outputter, Document doc) throws Exception {
    StringWriter writer = new StringWriter();
    outputter.output(doc, writer);
    return writer.toString();
  }

  private void assertFormattedOutputMatches(
      String testName,
      int indention,
      int attributeIndention,
      String[] namespaceOrder,
      String[] attributeOrder,
      boolean attributeSort,
      boolean namespaceSort)
      throws Exception {
    assertFormattedOutputMatches(
        testName,
        indention,
        attributeIndention,
        namespaceOrder,
        attributeOrder,
        attributeSort,
        namespaceSort,
        false);
  }

  private void assertFormattedOutputMatches(
      String testName,
      int indention,
      int attributeIndention,
      String[] namespaceOrder,
      String[] attributeOrder,
      boolean attributeSort,
      boolean namespaceSort,
      boolean multilineTagEnd)
      throws Exception {
    assertFormattedOutputMatches(
        testName,
        indention,
        attributeIndention,
        namespaceOrder,
        attributeOrder,
        new String[0],
        attributeSort,
        namespaceSort,
        multilineTagEnd);
  }

  private void assertFormattedOutputMatches(
      String testName,
      int indention,
      int attributeIndention,
      String[] namespaceOrder,
      String[] attributeOrder,
      String[] canOneline,
      boolean attributeSort,
      boolean namespaceSort,
      boolean multilineTagEnd)
      throws Exception {

    Document inputDoc = parseResource(INPUT_DIR + testName + ".xml");
    String expected = loadResource(EXPECTED_DIR + testName + ".xml");

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            indention,
            attributeIndention,
            namespaceOrder,
            attributeOrder,
            canOneline,
            attributeSort,
            namespaceSort,
            multilineTagEnd);

    String actual = formatDocument(outputter, inputDoc);

    // Normalize line endings for comparison
    expected = expected.replace("\r\n", "\n").trim();
    actual = actual.replace("\r\n", "\n").trim();

    assertEquals(expected, actual, "Formatted output should match expected for: " + testName);
  }

  // === Default Options Test ===

  @Test
  @DisplayName("Default options: id, layout_width, layout_height first with 4-space indentation")
  void testDefaultOptions() throws Exception {
    assertFormattedOutputMatches(
        "default_options",
        4, // indention
        4, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        false // namespace sort
        );
  }

  // === Custom Indentation Test ===

  @Test
  @DisplayName("Custom indentation: 2 spaces instead of 4")
  void testCustomIndention() throws Exception {
    assertFormattedOutputMatches(
        "custom_indention",
        2, // indention
        2, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        false // namespace sort
        );
  }

  // === Custom Attribute Indentation Test ===

  @Test
  @DisplayName("Custom attribute indentation: 8 spaces for attributes")
  void testCustomAttributeIndention() throws Exception {
    assertFormattedOutputMatches(
        "custom_attribute_indention",
        4, // indention
        8, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        false // namespace sort
        );
  }

  // === Custom Attribute Order Test ===

  @Test
  @DisplayName("Custom attribute order: text, background first")
  void testCustomAttributeOrder() throws Exception {
    assertFormattedOutputMatches(
        "custom_attribute_order",
        4, // indention
        4, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"text", "background"}, // attribute order
        false, // attribute sort
        false // namespace sort
        );
  }

  // === Alphabetical Attribute Sort Test ===

  @Test
  @DisplayName("Attribute sort: alphabetical ordering of attributes")
  void testAttributeSort() throws Exception {
    assertFormattedOutputMatches(
        "attribute_sort",
        4, // indention
        4, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {}, // attribute order (empty for pure alphabetical)
        true, // attribute sort
        false // namespace sort
        );
  }

  // === Custom Namespace Order Test ===

  @Test
  @DisplayName("Custom namespace order: tools, app, android")
  void testCustomNamespaceOrder() throws Exception {
    assertFormattedOutputMatches(
        "custom_namespace_order",
        4, // indention
        4, // attribute indention
        new String[] {"tools", "app", "android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        false // namespace sort
        );
  }

  // === Alphabetical Namespace Sort Test ===

  @Test
  @DisplayName("Namespace sort: alphabetical ordering of namespaces")
  void testNamespaceSort() throws Exception {
    assertFormattedOutputMatches(
        "namespace_sort",
        4, // indention
        4, // attribute indention
        new String[] {}, // namespace order (empty for pure alphabetical)
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        true // namespace sort
        );
  }

  // === Combined Options Test ===

  @Test
  @DisplayName("Combined options: 2-space indent, 6-space attr indent, both sorts enabled")
  void testCombinedOptions() throws Exception {
    assertFormattedOutputMatches(
        "combined_options",
        2, // indention
        6, // attribute indention
        new String[] {}, // namespace order (empty for alphabetical)
        new String[] {}, // attribute order (empty for alphabetical)
        true, // attribute sort
        true // namespace sort
        );
  }

  // === Multiline Tag End Test ===

  @Test
  @DisplayName("Multiline tag end: closing tags on their own line")
  void testMultilineTagEnd() throws Exception {
    assertFormattedOutputMatches(
        "multiline_tag_end",
        4, // indention
        4, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        false, // attribute sort
        false, // namespace sort
        true // multiline tag end
        );
  }

  // === Can Oneline Test ===

  @Test
  @DisplayName("Can oneline: include and merge elements with single attribute on one line")
  void testCanOneline() throws Exception {
    assertFormattedOutputMatches(
        "can_oneline",
        4, // indention
        4, // attribute indention
        new String[] {"android"}, // namespace order
        new String[] {"id", "layout_width", "layout_height"}, // attribute order
        new String[] {"include", "merge"}, // can oneline
        false, // attribute sort
        false, // namespace sort
        false // multiline tag end
        );
  }

  // === Additional Integration Tests ===

  @Test
  @DisplayName("Zero indentation: no indentation for elements or attributes")
  void testZeroIndentation() throws Exception {
    Document inputDoc = parseResource(INPUT_DIR + "default_options.xml");

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            0,
            0,
            new String[] {"android"},
            new String[] {"id", "layout_width", "layout_height"},
            false,
            false,
            false);

    String result = formatDocument(outputter, inputDoc);

    // With zero indentation, child elements should not be indented
    assertTrue(result.contains("<Button"), "Should contain Button element");
    assertTrue(result.contains("<TextView"), "Should contain TextView element");
    assertFalse(
        result.contains("\n    <Button"),
        "Button should not be indented with 4 spaces when indentation is 0");
  }

  @Test
  @DisplayName("Verify output is valid XML that can be parsed")
  void testOutputIsValidXml() throws Exception {
    Document inputDoc = parseResource(INPUT_DIR + "default_options.xml");

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            4,
            4,
            new String[] {"android"},
            new String[] {"id", "layout_width", "layout_height"},
            false,
            false,
            false);

    String result = formatDocument(outputter, inputDoc);

    // Parse the output to verify it's valid XML
    SAXBuilder builder = new SAXBuilder();
    Document parsedDoc = builder.build(new java.io.StringReader(result));

    assertNotNull(parsedDoc, "Output should be parseable as XML");
    assertEquals(
        "LinearLayout",
        parsedDoc.getRootElement().getName(),
        "Root element should be LinearLayout");
  }

  @Test
  @DisplayName("Nested elements maintain correct structure")
  void testNestedElementsStructure() throws Exception {
    Document inputDoc = parseResource(INPUT_DIR + "default_options.xml");

    AndroidXmlOutputter outputter =
        new AndroidXmlOutputter(
            4,
            4,
            new String[] {"android"},
            new String[] {"id", "layout_width", "layout_height"},
            false,
            false,
            false);

    String result = formatDocument(outputter, inputDoc);

    // Verify nested structure
    assertTrue(result.contains("<LinearLayout"), "Should contain LinearLayout");
    assertTrue(result.contains("</LinearLayout>"), "Should have closing LinearLayout tag");
    assertTrue(result.contains("<Button"), "Should contain Button");
    assertTrue(result.contains("<TextView"), "Should contain TextView");

    // Verify proper nesting (Button and TextView are inside LinearLayout)
    int linearLayoutStart = result.indexOf("<LinearLayout");
    int linearLayoutEnd = result.indexOf("</LinearLayout>");
    int buttonPos = result.indexOf("<Button");
    int textViewPos = result.indexOf("<TextView");

    assertTrue(
        buttonPos > linearLayoutStart && buttonPos < linearLayoutEnd,
        "Button should be inside LinearLayout");
    assertTrue(
        textViewPos > linearLayoutStart && textViewPos < linearLayoutEnd,
        "TextView should be inside LinearLayout");
  }
}
