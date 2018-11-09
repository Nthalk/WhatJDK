package com.iodesystems.whatjdk;

import static org.objectweb.asm.Opcodes.ASM5;

import com.iodesystems.whatjdk.listeners.OnClassReferenceListener;
import com.iodesystems.whatjdk.listeners.OnJdkVersionListener;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

class WhatJDKClassVisitor extends ClassVisitor {

  private final FieldVisitor fieldVisitor;
  private final MethodVisitor methodVisitor;
  private ClassEntry classEntry;
  private OnClassReferenceListener onClassReferenceListener;
  private OnJdkVersionListener onJdkVersionListener;

  public WhatJDKClassVisitor() {
    super(ASM5);

    fieldVisitor = new FieldVisitor(api) {
    };
    methodVisitor = new MethodVisitor(api) {
      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start,
          Label end, int index) {
        checkClassUsage(desc);
        super.visitLocalVariable(name, desc, signature, start, end, index);
      }


      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        checkClassUsage(desc);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        checkClassUsage(desc);
        super.visitFieldInsn(opcode, owner, name, desc);
      }
    };

  }

  private String checkClassUsage(String name) {
    if (onClassReferenceListener != null) {
      onClassReferenceListener.onClassReferenced(classEntry, name);
    }
    return name;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName,
      String[] interfaces) {
    if (onJdkVersionListener != null) {
      onJdkVersionListener.onJdkVersion(classEntry, version);
    }
    checkClassUsage(superName);
    checkClassUsage(interfaces);
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public FieldVisitor visitField(int access, String name, String desc, String signature,
      Object value) {
    checkClassUsage(desc);
    return fieldVisitor;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature,
      String[] exceptions) {
    checkClassUsage(desc);
    return methodVisitor;
  }

  public String[] checkClassUsage(String[] classes) {
    for (String anInterface : classes) {
      checkClassUsage(anInterface);
    }
    return classes;
  }

  public void check(ClassEntry classEntry) {
    this.classEntry = classEntry;
    classEntry.getClassReader().accept(this, 0);
  }

  public void setOnClassReferenceListener(OnClassReferenceListener onClassReferenceListener) {
    this.onClassReferenceListener = onClassReferenceListener;
  }

  public void setOnJdkVersionListener(OnJdkVersionListener onJdkVersionListener) {
    this.onJdkVersionListener = onJdkVersionListener;
  }

}
