package com.bytehamster.androidxmlformatter;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Attribute;
import org.jdom.CDATA;
import org.jdom.Comment;
import org.jdom.Element;
import org.jdom.EntityRef;
import org.jdom.Namespace;
import org.jdom.ProcessingInstruction;
import org.jdom.Text;
import org.jdom.Verifier;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AndroidXmlOutputter extends XMLOutputter {
    final String[] namespaceOrder;
    final String[] attributeNameOrder;
    final int attributeIndention;
    final boolean alphabeticalAttributes;
    final boolean alphabeticalNamespaces;

    public AndroidXmlOutputter(int indention, int attributeIndention,
            String[] namespaceOrder, String[] attributeNameOrder,
            boolean alphabeticalAttributes, boolean alphabeticalNamespaces) {
        this.attributeIndention = attributeIndention;
        this.namespaceOrder = namespaceOrder;
        this.attributeNameOrder = attributeNameOrder;
        this.alphabeticalAttributes = alphabeticalAttributes;
        this.alphabeticalNamespaces = alphabeticalNamespaces;

        Format format = Format.getPrettyFormat();
        format.setIndent(StringUtils.repeat(" ", indention));
        format.setLineSeparator("\n");
        format.setEncoding("utf-8");
        setFormat(format);
    }

    static private int elementDepth(Element element) {
        int result = 0;
        while (element != null) {
            result++;
            element = element.getParentElement();
        }
        return result;
    }

    private void printNamespace(Writer out, Namespace ns, XMLOutputter.NamespaceStack namespaces)
            throws IOException {
        String prefix = ns.getPrefix();
        String uri = ns.getURI();
        if (!uri.equals(namespaces.getURI(prefix))) {
            out.write("xmlns");
            if (!prefix.equals("")) {
                out.write(":");
                out.write(prefix);
            }

            out.write("=\"");
            out.write(this.escapeAttributeEntities(uri));
            out.write("\"");
            namespaces.push(ns);
        }
    }

    private void printElementNamespace(Writer out, Element element,
            XMLOutputter.NamespaceStack namespaces) throws IOException {
        Namespace ns = element.getNamespace();
        if (ns != Namespace.XML_NAMESPACE) {
            if (ns != Namespace.NO_NAMESPACE || namespaces.getURI("") != null) {
                this.printNamespace(out, ns, namespaces);
            }

        }
    }

    private void printAdditionalNamespaces(Writer out, Element element,
            XMLOutputter.NamespaceStack namespaces) throws IOException {
        List list = element.getAdditionalNamespaces();
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                Namespace additional = (Namespace) list.get(i);
                if (attributeIndention > 0) {
                    newline(out);
                    indent(out, elementDepth(element) - 1);
                    out.write(StringUtils.repeat(" ", attributeIndention));
                } else {
                    out.write(" ");
                }
                this.printNamespace(out, additional, namespaces);
            }
        }

    }

    private int skipTrailingWhite(List content, int start) {
        int size = content.size();
        if (start > size) {
            start = size;
        }

        int index = start;
        if (this.currentFormat.getTextMode() == Format.TextMode.TRIM_FULL_WHITE
                || this.currentFormat.getTextMode() == Format.TextMode.NORMALIZE
                || this.currentFormat.getTextMode() == Format.TextMode.TRIM) {
            while (index >= 0 && this.isAllWhitespace(content.get(index - 1))) {
                --index;
            }
        }

        return index;
    }

    private boolean isAllWhitespace(Object obj) {
        String str = null;
        if (obj instanceof String) {
            str = (String) obj;
        } else {
            if (!(obj instanceof Text)) {
                if (obj instanceof EntityRef) {
                    return false;
                }

                return false;
            }

            str = ((Text) obj).getText();
        }

        for (int i = 0; i < str.length(); ++i) {
            if (!Verifier.isXMLWhitespace(str.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    private int skipLeadingWhite(List content, int start) {
        if (start < 0) {
            start = 0;
        }

        int index = start;
        int size = content.size();
        if (true) {
            while (index < size) {
                if (!this.isAllWhitespace(content.get(index))) {
                    return index;
                }

                ++index;
            }
        }

        return index;
    }

    private static int nextNonText(List content, int start) {
        if (start < 0) {
            start = 0;
        }

        int index = start;

        int size;
        for (size = content.size(); index < size; ++index) {
            Object node = content.get(index);
            if (!(node instanceof Text) && !(node instanceof EntityRef)) {
                return index;
            }
        }

        return size;
    }

    private void printContentRange(Writer out, List content, int start, int end, int level,
            XMLOutputter.NamespaceStack namespaces) throws IOException {
        int index = start;

        while (true) {
            while (index < end) {
                boolean firstNode = index == start;
                Object next = content.get(index);
                if (!(next instanceof Text) && !(next instanceof EntityRef)) {
                    if (!firstNode) {
                        this.newline(out);
                    }

                    this.indent(out, level);
                    if (next instanceof Comment) {
                        this.printComment(out, (Comment) next);
                    } else if (next instanceof Element) {
                        this.printElement(out, (Element) next, level, namespaces);
                    } else if (next instanceof ProcessingInstruction) {
                        this.printProcessingInstruction(out, (ProcessingInstruction) next);
                    }

                    ++index;
                } else {
                    int first = this.skipLeadingWhite(content, index);
                    index = nextNonText(content, first);
                    if (first < index) {
                        if (!firstNode) {
                            this.newline(out);
                        }

                        this.indent(out, level);
                        this.printTextRange(out, content, first, index);
                    }
                }
            }

            return;
        }
    }

    private void printString(Writer out, String str) throws IOException {
        if (this.currentFormat.getTextMode() == Format.TextMode.NORMALIZE) {
            str = Text.normalizeString(str);
        } else if (this.currentFormat.getTextMode() == Format.TextMode.TRIM) {
            str = str.trim();
        }

        out.write(this.escapeElementEntities(str));
    }

    private void printTextRange(Writer out, List content, int start, int end) throws IOException {
        String previous = null;
        start = this.skipLeadingWhite(content, start);
        int size = content.size();
        if (start < size) {
            end = this.skipTrailingWhite(content, end);

            for (int i = start; i < end; ++i) {
                Object node = content.get(i);
                String next;
                if (node instanceof Text) {
                    next = ((Text) node).getText();
                } else {
                    if (!(node instanceof EntityRef)) {
                        throw new IllegalStateException(
                                "Should see only CDATA, Text, or EntityRef");
                    }

                    next = "&" + ((EntityRef) node).getValue() + ";";
                }

                if (next != null && !"".equals(next)) {
                    if (previous != null
                            && (this.currentFormat.getTextMode() == Format.TextMode.NORMALIZE
                                    || this.currentFormat.getTextMode() == Format.TextMode.TRIM)
                            && (this.endsWithWhite(previous) || this.startsWithWhite(next))) {
                        out.write(" ");
                    }

                    if (node instanceof CDATA) {
                        this.printCDATA(out, (CDATA) node);
                    } else if (node instanceof EntityRef) {
                        this.printEntityRef(out, (EntityRef) node);
                    } else {
                        this.printString(out, next);
                    }

                    previous = next;
                }
            }
        }

    }

    private boolean startsWithWhite(String str) {
        return str != null && str.length() > 0 && Verifier.isXMLWhitespace(str.charAt(0));
    }

    private boolean endsWithWhite(String str) {
        return str != null && str.length() > 0
                && Verifier.isXMLWhitespace(str.charAt(str.length() - 1));
    }

    @Override
    protected void printElement(Writer out, Element element, int level, NamespaceStack namespaces)
            throws IOException {
        List attributes = element.getAttributes();
        List content = element.getContent();
        String space = null;
        if (attributes != null) {
            space = element.getAttributeValue("space", Namespace.XML_NAMESPACE);
        }

        Format previousFormat = this.currentFormat;
        if ("default".equals(space)) {
            this.currentFormat = this.getFormat();
        } else if ("preserve".equals(space)) {
            this.currentFormat = preserveFormat;
        }

        out.write("<");
        this.printQualifiedName(out, element);
        int previouslyDeclaredNamespaces = namespaces.size();
        this.printElementNamespace(out, element, namespaces);
        this.printAdditionalNamespaces(out, element, namespaces);
        if (attributes != null) {
            this.printAttributes(out, attributes, element, namespaces);
        }

        int start = this.skipLeadingWhite(content, 0);
        int size = content.size();
        if (start >= size) {
            out.write(" />");
        } else {
            out.write(">");
            newline(out);
            if (nextNonText(content, start) < size) {
                this.newline(out);
                this.printContentRange(out, content, start, size, level + 1, namespaces);
                this.newline(out);
                this.indent(out, level);
            } else {
                this.printTextRange(out, content, start, size);
            }

            out.write("</");
            this.printQualifiedName(out, element);
            out.write(">");
        }

        while (namespaces.size() > previouslyDeclaredNamespaces) {
            namespaces.pop();
        }

        this.currentFormat = previousFormat;
        newline(out);
    }

    private void newline(Writer out) throws IOException {
        out.write(getFormat().getLineSeparator());
    }

    private void indent(Writer out, int level) throws IOException {
        for (int i = 0; i < level; ++i) {
            out.write(getFormat().getIndent());
        }
    }

    @Override
    protected void printAttributes(Writer writer, List attribs, Element parent, NamespaceStack ns)
            throws IOException {
        List<Attribute> attributes = new ArrayList<>();
        for (Object attribObj : attribs) {
            attributes.add((Attribute) attribObj);
        }

        Collections.sort(attributes, (a1, a2) -> {
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
                newline(writer);
                indent(writer, elementDepth(parent) - 1);
                writer.write(StringUtils.repeat(" ", attributeIndention));
            } else {
                writer.write(" ");
            }

            printQualifiedName(writer, attrib);
            writer.write("=");
            writer.write("\"");
            writer.write(escapeAttributeEntities(attrib.getValue()));
            writer.write("\"");
        }
    }

    private void printQualifiedName(Writer out, Attribute a) throws IOException {
        String prefix = a.getNamespace().getPrefix();
        if (prefix != null && !prefix.equals("")) {
            out.write(prefix);
            out.write(58);
            out.write(a.getName());
        } else {
            out.write(a.getName());
        }

    }

    private void printQualifiedName(Writer out, Element e) throws IOException {
        if (e.getNamespace().getPrefix().length() == 0) {
            out.write(e.getName());
        } else {
            out.write(e.getNamespace().getPrefix());
            out.write(58);
            out.write(e.getName());
        }

    }
}
