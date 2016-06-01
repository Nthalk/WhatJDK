package com.iodesystems.whatjdk;

import com.iodesystems.fn.data.Generator;
import org.objectweb.asm.ClassReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ClassEntryExtractor implements Generator<ClassEntry> {

    private final String fileName;
    private ClassEntryExtractor delegate = null;
    private final ZipInputStream zipInputStream;

    public ClassEntryExtractor(String fileName) throws FileNotFoundException {
        this.fileName = fileName;
        FileInputStream fileInputStream = new FileInputStream(fileName);
        zipInputStream = new ZipInputStream(fileInputStream);
    }

    public ClassEntryExtractor(String fileName, ZipInputStream zipInputStream) throws FileNotFoundException {
        this.fileName = fileName;
        this.zipInputStream = zipInputStream;
    }

    @Override
    public ClassEntry next() {
        ZipEntry entry;
        ClassEntry next;

        if (delegate != null) {
            next = delegate.next();
            if (next != null) {
                return next;
            }
        }

        try {
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".class")) {
                    return new ClassEntry(fileName, entry.getName(), new ClassReader(zipInputStream));
                } else if (entry.getName().endsWith(".jar")) {
                    delegate = new ClassEntryExtractor(fileName + ":" + entry.getName(), new ZipInputStream(zipInputStream));
                    next = delegate.next();
                    if (next != null) {
                        return next;
                    }
                }
            }

            return null;
        } catch (IOException e) {
            return null;
        }
    }
}
