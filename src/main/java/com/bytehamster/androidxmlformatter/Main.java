package com.bytehamster.androidxmlformatter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("indention")
                .desc("Indention.")
                .hasArg().build());
        options.addOption(Option.builder().longOpt("attribute-indention")
                .desc("Indention of attributes.")
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
        options.addOption(Option.builder().longOpt("namespace-sort")
                .desc("Sort namespaces.")
                .build());

        CommandLine cmd;
        try {
            cmd = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());

            String jarPath = new File(Main.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation().toString()).getName();
            new HelpFormatter().printHelp(jarPath + " [OPTIONS] <FILES>", options);
            System.exit(1);
            return;
        }

        if (cmd.getArgList().isEmpty()) {
            System.out.println("Empty list of files to re-format");
        }

        for (String filename : cmd.getArgList()) {
            Document doc = new SAXBuilder().build(new FileInputStream(filename));
            AndroidXmlOutputter outputter = new AndroidXmlOutputter(
                    Integer.parseInt(cmd.getOptionValue("indention", "4")),
                    Integer.parseInt(cmd.getOptionValue("attribute-indention", "4")),
                    cmd.getOptionValue("namespace-order", "android").split(","),
                    cmd.getOptionValue("attribute-order", "id,layout_width,layout_height")
                            .split(","),
                    cmd.hasOption("attribute-sort"),
                    cmd.hasOption("namespace-sort"));
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            outputter.output(doc, stream);
            byte[] content = stream.toByteArray();
            try (FileOutputStream fos = new FileOutputStream(filename)) {
                fos.write(content, 0, content.length);
            }
        }
    }
}
