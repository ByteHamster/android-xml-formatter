package com.bytehamster.androidxmlformatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("file")
                .desc("File to re-format (in-place).")
                .hasArg().required().build());
        options.addOption(Option.builder().longOpt("indention")
                .desc("Indention.")
                .hasArg().build());
        options.addOption(Option.builder().longOpt("attribute-indention")
                .desc("Indention of attributes. Omit to keep attributes in one line.")
                .hasArg().build());
        options.addOption(Option.builder().longOpt("attribute-order")
                .desc("When ordering attributes by name, use this order. Separated by comma.")
                .hasArg().build());
        options.addOption(Option.builder().longOpt("attribute-sort")
                .desc("Sort attributes.")
                .build());
        options.addOption(Option.builder().longOpt("namespace-order")
                .desc("When ordering attributes by namespace, use this order. Separated by comma.")
                .hasArg().build());

        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());

            String jarPath = new File(Main.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation().toString()).getName();
            new HelpFormatter().printHelp(jarPath, options);
            System.exit(1);
            return;
        }

        Document doc = new SAXBuilder().build(new FileInputStream(cmd.getOptionValue("file")));
        XMLOutputter outputter = new AndroidXmlOutputter(
                Integer.parseInt(cmd.getOptionValue("indention", "4")),
                Integer.parseInt(cmd.getOptionValue("attribute-indention", "8")),
                cmd.getOptionValue("namespace-order", "").split(","),
                cmd.getOptionValue("attribute-order", "").split(","),
                cmd.hasOption("attribute-sort"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        outputter.output(doc, stream);
        stream.toString();
    }
}
