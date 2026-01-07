package com.bytehamster.androidxmlformatter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jdom2.Attribute;
import org.jdom2.CDATA;
import org.jdom2.Comment;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.EntityRef;
import org.jdom2.Namespace;
import org.jdom2.ProcessingInstruction;
import org.jdom2.Text;
import org.jdom2.Verifier;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class AndroidXmlOutputter {
  private final XMLOutputter outputter;
  private final Format format;
  final String[] namespaceOrder;
  final String[] attributeNameOrder;
  final int attributeIndention;
  final boolean alphabeticalAttributes;
  final boolean alphabeticalNamespaces;
  final boolean multilineTagEnd;

  public AndroidXmlOutputter(
      int indention,
      int attributeIndention,
      String[] namespaceOrder,
      String[] attributeNameOrder,
      boolean alphabeticalAttributes,
      boolean alphabeticalNamespaces,
      boolean multilineTagEnd) {
    this.attributeIndention = attributeIndention;
    this.namespaceOrder = namespaceOrder;
    this.attributeNameOrder = attributeNameOrder;
    this.alphabeticalAttributes = alphabeticalAttributes;
    this.alphabeticalNamespaces = alphabeticalNamespaces;
    this.multilineTagEnd = multilineTagEnd;

    this.format = Format.getPrettyFormat();
    this.format.setIndent(StringUtils.repeat(" ", indention));
    this.format.setLineSeparator("\n");
    this.format.setEncoding("utf-8");

    this.outputter = new XMLOutputter(this.format);
  }

  public Format getFormat() {
    return format;
  }

  public void output(Document doc, Writer writer) throws IOException {
    writer.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    Element root = doc.getRootElement();
    printElement(writer, root, 0);
  }

  public void output(Document doc, OutputStream stream) throws IOException {
    stream.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n".getBytes());
    Element root = doc.getRootElement();
    StringBuilder sb = new StringBuilder();
    printElementToBuilder(sb, root, 0);
    stream.write(sb.toString().getBytes());
  }

  private void printElement(Writer writer, Element element, int depth) throws IOException {
    StringBuilder sb = new StringBuilder();
    printElementToBuilder(sb, element, depth);
    writer.write(sb.toString());
  }

  private void printElementToBuilder(StringBuilder sb, Element element, int depth) {
    String indent = StringUtils.repeat(format.getIndent(), depth);

    sb.append(indent);
    sb.append("<");
    printQualifiedName(sb, element);

    // Print namespace declarations for this element's namespace if at root
    if (depth == 0 && !element.getNamespace().equals(Namespace.NO_NAMESPACE)) {
      sb.append(" ");
      printNamespaceDeclaration(sb, element.getNamespace());
    }

    // Print additional namespace declarations
    for (Namespace ns : element.getAdditionalNamespaces()) {
      if (attributeIndention > 0) {
        sb.append("\n");
        sb.append(indent);
        sb.append(StringUtils.repeat(" ", attributeIndention));
      } else {
        sb.append(" ");
      }
      printNamespaceDeclaration(sb, ns);
    }

    // Print attributes
    List<Attribute> attributes = element.getAttributes();
    if (attributes != null && !attributes.isEmpty()) {
      printAttributes(sb, element, depth);
    }

    List<Content> content = element.getContent();
    int start = skipLeadingWhite(content, 0);
    int size = content.size();

    boolean hasAttributes =
        (attributes != null && !attributes.isEmpty())
            || !element.getAdditionalNamespaces().isEmpty();

    if (start >= size) {
      if (multilineTagEnd && hasAttributes && attributeIndention > 0) {
        sb.append("\n");
        sb.append(indent);
        sb.append(StringUtils.repeat(" ", attributeIndention));
        sb.append("/>\n");
      } else {
        sb.append(" />\n");
      }
    } else {
      if (multilineTagEnd && hasAttributes && attributeIndention > 0) {
        sb.append("\n");
        sb.append(indent);
        sb.append(StringUtils.repeat(" ", attributeIndention));
        sb.append(">");
      } else {
        sb.append(">");
      }
      if (nextNonText(content, start) < size) {
        sb.append("\n");
        printContentRange(sb, content, start, size, depth + 1);
        sb.append(indent);
      } else {
        printTextRange(sb, content, start, size);
      }
      sb.append("</");
      printQualifiedName(sb, element);
      sb.append(">\n");
    }
  }

  private void printNamespaceDeclaration(StringBuilder sb, Namespace ns) {
    sb.append("xmlns");
    if (!ns.getPrefix().isEmpty()) {
      sb.append(":");
      sb.append(ns.getPrefix());
    }
    sb.append("=\"");
    sb.append(escapeAttribute(ns.getURI()));
    sb.append("\"");
  }

  private void printAttributes(StringBuilder sb, Element parent, int depth) {
    List<Attribute> attributes = new ArrayList<>(parent.getAttributes());
    String indent = StringUtils.repeat(format.getIndent(), depth);

    Collections.sort(
        attributes,
        (a1, a2) -> {
          if (!a1.getNamespacePrefix().equals(a2.getNamespacePrefix())) {
            for (String namespace : namespaceOrder) {
              if (a1.getNamespacePrefix().equals(namespace)) {
                return -1;
              } else if (a2.getNamespacePrefix().equals(namespace)) {
                return 1;
              }
            }
            if (alphabeticalNamespaces) {
              return a1.getNamespacePrefix().compareTo(a2.getNamespacePrefix());
            }
          }
          for (String name : attributeNameOrder) {
            if (a1.getName().equals(name)) {
              return -1;
            } else if (a2.getName().equals(name)) {
              return 1;
            }
          }
          if (alphabeticalAttributes) {
            return a1.getName().compareTo(a2.getName());
          } else {
            return 0; // Sort is stable
          }
        });

    for (Attribute attrib : attributes) {
      if (attributeIndention > 0) {
        sb.append("\n");
        sb.append(indent);
        sb.append(StringUtils.repeat(" ", attributeIndention));
      } else {
        sb.append(" ");
      }

      printQualifiedName(sb, attrib);
      sb.append("=\"");
      sb.append(escapeAttribute(attrib.getValue()));
      sb.append("\"");
    }
  }

  private void printContentRange(
      StringBuilder sb, List<Content> content, int start, int end, int depth) {
    int index = start;

    while (index < end) {
      Content next = content.get(index);
      if (!(next instanceof Text) && !(next instanceof EntityRef)) {
        String indent = StringUtils.repeat(format.getIndent(), depth);
        if (next instanceof Comment) {
          sb.append(indent);
          sb.append("<!--");
          sb.append(((Comment) next).getText());
          sb.append("-->\n");
        } else if (next instanceof Element) {
          printElementToBuilder(sb, (Element) next, depth);
        } else if (next instanceof ProcessingInstruction) {
          ProcessingInstruction pi = (ProcessingInstruction) next;
          sb.append(indent);
          sb.append("<?");
          sb.append(pi.getTarget());
          sb.append(" ");
          sb.append(pi.getData());
          sb.append("?>\n");
        } else if (next instanceof CDATA) {
          sb.append(indent);
          sb.append("<![CDATA[");
          sb.append(((CDATA) next).getText());
          sb.append("]]>\n");
        }
        ++index;
      } else {
        int first = skipLeadingWhite(content, index);
        index = nextNonText(content, first);
        if (first < index) {
          String indent = StringUtils.repeat(format.getIndent(), depth);
          sb.append(indent);
          printTextRange(sb, content, first, index);
          sb.append("\n");
        }
      }
    }
  }

  private void printTextRange(StringBuilder sb, List<Content> content, int start, int end) {
    start = skipLeadingWhite(content, start);
    int size = content.size();
    if (start < size) {
      end = skipTrailingWhite(content, end);

      for (int i = start; i < end; ++i) {
        Content node = content.get(i);
        String text;
        if (node instanceof Text) {
          text = ((Text) node).getText().trim();
        } else if (node instanceof EntityRef) {
          text = "&" + ((EntityRef) node).getName() + ";";
        } else if (node instanceof CDATA) {
          sb.append("<![CDATA[");
          sb.append(((CDATA) node).getText());
          sb.append("]]>");
          continue;
        } else {
          continue;
        }

        if (text != null && !text.isEmpty()) {
          sb.append(escapeText(text));
        }
      }
    }
  }

  private int skipLeadingWhite(List<Content> content, int start) {
    if (start < 0) {
      start = 0;
    }

    int index = start;
    int size = content.size();
    while (index < size) {
      if (!isAllWhitespace(content.get(index))) {
        return index;
      }
      ++index;
    }
    return index;
  }

  private int skipTrailingWhite(List<Content> content, int start) {
    int size = content.size();
    if (start > size) {
      start = size;
    }

    int index = start;
    while (index > 0 && isAllWhitespace(content.get(index - 1))) {
      --index;
    }

    return index;
  }

  private boolean isAllWhitespace(Object obj) {
    String str = null;
    if (obj instanceof String) {
      str = (String) obj;
    } else if (obj instanceof Text) {
      str = ((Text) obj).getText();
    } else if (obj instanceof EntityRef) {
      return false;
    } else {
      return false;
    }

    for (int i = 0; i < str.length(); ++i) {
      if (!Verifier.isXMLWhitespace(str.charAt(i))) {
        return false;
      }
    }

    return true;
  }

  private int nextNonText(List<Content> content, int start) {
    if (start < 0) {
      start = 0;
    }

    int index = start;
    int size = content.size();
    while (index < size) {
      Content node = content.get(index);
      if (!(node instanceof Text) && !(node instanceof EntityRef)) {
        return index;
      }
      ++index;
    }

    return size;
  }

  private void printQualifiedName(StringBuilder sb, Attribute a) {
    String prefix = a.getNamespace().getPrefix();
    if (prefix != null && !prefix.isEmpty()) {
      sb.append(prefix);
      sb.append(":");
    }
    sb.append(a.getName());
  }

  private void printQualifiedName(StringBuilder sb, Element e) {
    if (!e.getNamespace().getPrefix().isEmpty()) {
      sb.append(e.getNamespace().getPrefix());
      sb.append(":");
    }
    sb.append(e.getName());
  }

  private String escapeAttribute(String value) {
    if (value == null) {
      return "";
    }
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  private String escapeText(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
