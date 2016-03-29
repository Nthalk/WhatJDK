package com.nthalk.whatjdk;

import org.docopt.Docopt;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class WhatJDK {

    private static final String doc =
        "whatjdk\n"
            + "\n"
            + "Usage:\n"
            + "  whatjdk [JARFILE...]\n"
            + "  whatjdk (-h | --help)\n"
            + "  whatjdk (-v | --version)\n"
            + "\n"
            + "Options:\n"
            + "  -h --help          Show this screen.\n"
            + "  -v --version       Show version.\n"
            + "\n";

    public static void main(String[] args) {
        Map<String, Object> opts = new Docopt(doc)
            .withVersion("1.0-SNAPSHOT")
            .parse(args);

        @SuppressWarnings("unchecked")
        List<String> jarfiles = (List<String>) opts.get("JARFILE");
        if (jarfiles != null) {
            for (String fileName : jarfiles) {
                checkFileExtensionCompatibility(fileName);
                extractClassVersionInfoForJar(fileName);
            }
            System.exit(0);
        }
    }

    private static void extractClassVersionInfoForJar(String fileName) {
        try {
            FileInputStream fileInputStream = new FileInputStream(fileName);
            extractInternalClassOrLibVersion(fileName, fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractInternalClassOrLibVersion(String fileName, InputStream fileInputStream) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);
        ZipEntry entry;
        Set<String> versions = new HashSet<String>();
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
                extractClassVersionInfo(fileName + ":" + entry.getName(), zipInputStream, versions);
            } else if (entry.getName().endsWith(".jar")) {
                extractInternalClassOrLibVersion(fileName + ":" + entry.getName(), zipInputStream);
            }
        }
        if (!versions.isEmpty()) {
            StringBuilder versionsString = new StringBuilder();
            for (String version : versions) {
                versionsString.append(version);
                versionsString.append(", ");
            }
            System.out.println(fileName + " contains classes compatible with " + versionsString.substring(0, versionsString.length() - 2));
        }
    }

    private static void extractClassVersionInfo(String fileName,
                                                ZipInputStream zipInputStream,
                                                Set<String> versions) throws IOException {
        DataInputStream data = new DataInputStream(zipInputStream);
        if (0xCAFEBABE != data.readInt()) {
            System.err.println("Corrupt class file: " + fileName);
            return;
        }
        int minor = data.readUnsignedShort();
        int major = data.readUnsignedShort();
        switch (major) {
            case 45:
                versions.add("Java1.1");
                break;
            case 46:
                versions.add("Java1.2");
                break;
            case 47:
                versions.add("Java1.3");
                break;
            case 48:
                versions.add("Java1.4");
                break;
            case 49:
                versions.add("Java1.5");
                break;
            case 50:
                versions.add("Java1.6");
                break;
            case 51:
                versions.add("Java1.7");
                break;
            case 52:
                versions.add("Java1.8");
                break;
            default:
                versions.add(major + "." + minor);
        }
    }

    public static File checkFileExtensionCompatibility(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("File does not exist: " + fileName);
            System.exit(-1);
        }

        if (!file.isFile()) {
            System.err.println("Is not a file: " + fileName);
            System.exit(-1);
        }

        return file;
    }
}
