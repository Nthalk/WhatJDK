package com.iodesystems.whatjdk;

import com.iodesystems.fn.Fn;
import com.iodesystems.fn.data.From;
import com.iodesystems.fn.logic.Condition;
import com.iodesystems.whatjdk.listeners.OnClassReferenceListener;
import com.iodesystems.whatjdk.listeners.OnJdkVersionListener;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import org.docopt.Docopt;

public class WhatJDK {


  private static final String doc =
      "whatjdk\n"
          + "\n"
          + "Usage:\n"
          + "  whatjdk [JARFILE...] [--max-jdk=jdk] [--uses-class=<class>]\n"
          + "  whatjdk (-h | --help)\n"
          + "  whatjdk (-v | --version)\n"
          + "\n"
          + "Options:\n"
          + "  --uses-class=<class>   Limit results to classes that reference this class.\n"
          + "  --max-jdk-=<jdk>       Limit results to classes that use this version.\n"
          + "  -h --help              Show this screen.\n"
          + "  -v --version           Show version.\n"
          + "\n";
  boolean success = true;
  private List<String> jarFiles;
  private JdkVersion maxJdkVersion;
  private String[] usesClasses;
  private OnJdkVersionListener onInvalidJdkVersionListener = new OnJdkVersionListener() {
    @Override
    public void onJdkVersion(ClassEntry classEntry, int version) {
      System.out.println(
          classEntry.getContainer() + ": " + classEntry.getFileName() + " is using java version: "
              + JdkVersion.parse(version));
    }
  };

  private OnClassReferenceListener onClassReferenceListener = new OnClassReferenceListener() {
    @Override
    public void onClassReferenced(ClassEntry classEntry, String name) {
      System.out.println(
          classEntry.getContainer() + ": " + classEntry.getFileName() + " uses class: " + name);
    }
  };
  private boolean scanInnerJarFiles;

  public static void main(String[] args) throws Exception {
    Map<String, Object> opts = new Docopt(doc)
        .withVersion("1.0-SNAPSHOT")
        .parse(args);

    final WhatJDK whatJDK = new WhatJDK();

    @SuppressWarnings("unchecked")
    List<String> jarfiles = (List<String>) opts.get("JARFILE");
    whatJDK.setJarFiles(jarfiles);
    whatJDK.setMaxJdkVersion(JdkVersion.parse(opts.get("--max-jdk")));
    whatJDK.setUsesClasses(parseUsesClass(opts.get("--uses-class")));
    if (whatJDK.execute()) {
      System.exit(-1);
    } else {
      System.exit(0);
    }

  }

  public static String[] parseUsesClass(Object o) {
    if (o == null) {
      return null;
    }

    int i = 0;
    String[] strings = o.toString().split(",");
    for (String s : strings) {
      strings[i] = "L" + s.replace('.', '/') + ";";
      i++;
    }

    return strings;
  }

  public boolean execute() {
    WhatJDKClassVisitor classVisitor = new WhatJDKClassVisitor();

    if (getUsesClasses() != null) {
      classVisitor.setOnClassReferenceListener(new OnClassReferenceListener() {
        @Override
        public void onClassReferenced(ClassEntry classEntry, String name) {
          String fixedName;
          if (name.charAt(name.length() - 1) != ';') {
            fixedName = "L" + name + ";";
          } else {
            fixedName = name;
          }

          for (String usesClass : usesClasses) {
            if (fixedName.contains(usesClass)) {
              success = false;
              onClassReferenceListener.onClassReferenced(classEntry, name);
            }
          }
        }
      });
    }

    if (getMaxJdkVersion() != null) {
      classVisitor.setOnJdkVersionListener(new OnJdkVersionListener() {
        @Override
        public void onJdkVersion(ClassEntry classEntry, int version) {
          if (JdkVersion.parse(version).getOrder() > getMaxJdkVersion().getOrder()) {
            success = false;
            onInvalidJdkVersionListener.onJdkVersion(classEntry, version);
          }
        }
      });
    }
    for (ClassEntry classEntry : getClassEntries(jarFiles)) {
      classVisitor.check(classEntry);
    }

    return success;
  }

  private Iterable<ClassEntry> getClassEntries(List<String> jarfiles) {
    return Fn.flatten(Fn.of(jarfiles).convert(new From<String, Iterable<ClassEntry>>() {
      @Override
      public Iterable<ClassEntry> from(String s) {
        try {
          return Fn.of(new ClassEntryExtractor(s, scanInnerJarFiles))
              .takeWhile(new Condition<ClassEntry>() {
                @Override
                public boolean is(ClassEntry classEntry) {
                  return classEntry != null;
                }
              });
        } catch (FileNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
    }));
  }


  public void setJarFiles(List<String> jarFiles) {
    this.jarFiles = jarFiles;
  }

  public JdkVersion getMaxJdkVersion() {
    return maxJdkVersion;
  }

  public void setMaxJdkVersion(JdkVersion maxJdkVersion) {
    this.maxJdkVersion = maxJdkVersion;
  }

  public void setOnInvalidJdkVersionListener(OnJdkVersionListener onInvalidJdkVersionListener) {
    this.onInvalidJdkVersionListener = onInvalidJdkVersionListener;
  }

  public void setOnClassReferenceListener(OnClassReferenceListener onClassReferenceListener) {
    this.onClassReferenceListener = onClassReferenceListener;
  }

  public String[] getUsesClasses() {
    return usesClasses;
  }

  public void setUsesClasses(String[] usesClasses) {
    this.usesClasses = usesClasses;
  }

  public boolean getSuccess() {
    return success;
  }

  public boolean isScanInnerJarFiles() {
    return scanInnerJarFiles;
  }

  public void setScanInnerJarFiles(boolean scanInnerJarFiles) {
    this.scanInnerJarFiles = scanInnerJarFiles;
  }
}
