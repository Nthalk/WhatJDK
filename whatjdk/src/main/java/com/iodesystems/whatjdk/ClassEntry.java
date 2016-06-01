package com.iodesystems.whatjdk;

import org.objectweb.asm.ClassReader;

public class ClassEntry {
    private final String container;
    private final String fileName;
    private final ClassReader classReader;

    public ClassEntry(String container, String fileName, ClassReader classReader) {
        this.container = container;
        this.fileName = fileName;
        this.classReader = classReader;
    }

    public String getContainer() {
        return container;
    }

    public String getFileName() {
        return fileName;
    }

    public ClassReader getClassReader() {
        return classReader;
    }
}
