package com.bytehamster.androidxmlformatter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

  @TempDir Path tempDir;

  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;

  @BeforeEach
  void setUpStreams() {
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));
  }

  void restoreStreams() {
    System.setOut(originalOut);
    System.setErr(originalErr);
  }

  private File createTestXmlFile(String content) throws Exception {
    Path xmlFile = tempDir.resolve("test.xml");
    Files.write(xmlFile, content.getBytes());
    return xmlFile.toFile();
  }

  @Test
  void testEmptyFileList() throws Exception {
    try {
      Main.main(new String[] {});
      String output = outContent.toString();
      assertTrue(
          output.contains("Empty list of files to re-format"),
          "Should print message when no files provided");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testDefaultOptionValues() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:layout_width=\"match_parent\"\n"
            + "    android:layout_height=\"match_parent\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(result.contains("android:layout_width"), "Should preserve layout_width");
      assertTrue(result.contains("android:layout_height"), "Should preserve layout_height");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testCustomIndentionOption() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
            + "<Button />\n"
            + "</LinearLayout>";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {"--indention", "2", testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(result.contains("<Button"), "Should contain Button element");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testCustomAttributeIndentionOption() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<Button xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:id=\"@+id/btn\"\n"
            + "    android:text=\"Click\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {"--attribute-indention", "8", testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(result.contains("android:id"), "Should contain id attribute");
      assertTrue(result.contains("android:text"), "Should contain text attribute");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testAttributeOrderOption() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<Button xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:id=\"@+id/btn\"\n"
            + "    android:text=\"Click\"\n"
            + "    android:layout_width=\"wrap_content\"\n"
            + "    android:layout_height=\"wrap_content\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(
          new String[] {
            "--attribute-order", "text,id,layout_width,layout_height", testFile.getAbsolutePath()
          });
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int textPos = result.indexOf("android:text");
      int idPos = result.indexOf("android:id");

      assertTrue(textPos < idPos, "text should come before id with custom order");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testNamespaceOrderOption() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n"
            + "    android:id=\"@+id/view\"\n"
            + "    app:custom=\"value\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {"--namespace-order", "app,android", testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int appPos = result.indexOf("app:custom");
      int androidPos = result.indexOf("android:id");

      assertTrue(appPos < androidPos, "app namespace should come before android with custom order");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testAttributeSortFlag() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:zIndex=\"1\"\n"
            + "    android:alpha=\"0.5\"\n"
            + "    android:background=\"#FFF\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(
          new String[] {"--attribute-sort", "--attribute-order", "", testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int alphaPos = result.indexOf("android:alpha");
      int bgPos = result.indexOf("android:background");
      int zPos = result.indexOf("android:zIndex");

      assertTrue(alphaPos < bgPos, "alpha should come before background when sorted");
      assertTrue(bgPos < zPos, "background should come before zIndex when sorted");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testNamespaceSortFlag() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    xmlns:tools=\"http://schemas.android.com/tools\"\n"
            + "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n"
            + "    tools:toolsAttr=\"value\"\n"
            + "    app:appAttr=\"value\"\n"
            + "    android:androidAttr=\"value\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(
          new String[] {"--namespace-sort", "--namespace-order", "", testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int androidPos = result.indexOf("android:androidAttr");
      int appPos = result.indexOf("app:appAttr");
      int toolsPos = result.indexOf("tools:toolsAttr");

      assertTrue(androidPos < appPos, "android should come before app when namespace sorted");
      assertTrue(appPos < toolsPos, "app should come before tools when namespace sorted");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testComplexLayoutFormatting() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:layout_height=\"match_parent\"\n"
            + "    android:id=\"@+id/container\"\n"
            + "    android:layout_width=\"match_parent\"\n"
            + "    android:orientation=\"vertical\">\n"
            + "    <Button\n"
            + "        android:text=\"Click Me\"\n"
            + "        android:id=\"@+id/button\"\n"
            + "        android:layout_width=\"wrap_content\"\n"
            + "        android:layout_height=\"wrap_content\" />\n"
            + "</LinearLayout>";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int idPos = result.indexOf("android:id=\"@+id/container\"");
      int widthPos = result.indexOf("android:layout_width=\"match_parent\"");
      int heightPos = result.indexOf("android:layout_height=\"match_parent\"");

      assertTrue(idPos < widthPos, "id should come before layout_width");
      assertTrue(widthPos < heightPos, "layout_width should come before layout_height");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testMultipleFiles() throws Exception {
    String xml1 =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:id=\"@+id/view1\" />";

    String xml2 =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:id=\"@+id/view2\" />";

    Path file1 = tempDir.resolve("test1.xml");
    Path file2 = tempDir.resolve("test2.xml");
    Files.write(file1, xml1.getBytes());
    Files.write(file2, xml2.getBytes());

    try {
      Main.main(new String[] {file1.toString(), file2.toString()});

      String result1 = new String(Files.readAllBytes(file1));
      String result2 = new String(Files.readAllBytes(file2));

      assertTrue(result1.contains("android:id=\"@+id/view1\""), "First file should be formatted");
      assertTrue(result2.contains("android:id=\"@+id/view2\""), "Second file should be formatted");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testNestedLayoutFormatting() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<FrameLayout xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
            + "    <LinearLayout>\n"
            + "        <TextView />\n"
            + "    </LinearLayout>\n"
            + "</FrameLayout>";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(result.contains("<FrameLayout"), "Should contain FrameLayout");
      assertTrue(result.contains("<LinearLayout"), "Should contain LinearLayout");
      assertTrue(result.contains("<TextView"), "Should contain TextView");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testZeroIndention() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
            + "<Button />\n"
            + "</LinearLayout>";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(
          new String[] {
            "--indention", "0", "--attribute-indention", "0", testFile.getAbsolutePath()
          });
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(result.contains("<LinearLayout"), "Should contain LinearLayout");
      assertTrue(result.contains("<Button"), "Should contain Button");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testCombinedOptions() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:zIndex=\"1\"\n"
            + "    android:alpha=\"0.5\" />";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(
          new String[] {
            "--indention",
            "2",
            "--attribute-indention",
            "6",
            "--attribute-sort",
            "--attribute-order",
            "",
            testFile.getAbsolutePath()
          });
      String result = new String(Files.readAllBytes(testFile.toPath()));

      int alphaPos = result.indexOf("android:alpha");
      int zPos = result.indexOf("android:zIndex");

      assertTrue(alphaPos < zPos, "alpha should come before zIndex with alphabetical sort");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testOutputFileNotTruncated() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:layout_width=\"match_parent\"\n"
            + "    android:layout_height=\"match_parent\">\n"
            + "    <Button />\n"
            + "</LinearLayout>\n";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(
          result.endsWith("</LinearLayout>\n"),
          "Output file should end with complete closing tag and newline, but was: '"
              + result.substring(Math.max(0, result.length() - 20))
              + "'");
      assertTrue(
          result.contains("</LinearLayout>"),
          "Output file should contain complete closing tag");
    } finally {
      restoreStreams();
    }
  }

  @Test
  void testSelfClosingElementNotTruncated() throws Exception {
    String xml =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<View xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
            + "    android:id=\"@+id/view\" />\n";

    File testFile = createTestXmlFile(xml);

    try {
      Main.main(new String[] {testFile.getAbsolutePath()});
      String result = new String(Files.readAllBytes(testFile.toPath()));

      assertTrue(
          result.endsWith("/>\n"),
          "Self-closing element should end with '/>' and newline, but was: '"
              + result.substring(Math.max(0, result.length() - 20))
              + "'");
    } finally {
      restoreStreams();
    }
  }
}
