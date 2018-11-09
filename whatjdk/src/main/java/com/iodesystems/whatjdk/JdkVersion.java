package com.iodesystems.whatjdk;

public class JdkVersion {

  public static final JdkVersion J2SE_11 = new JdkVersion("Java11", 56, 12);
  public static final JdkVersion J2SE_10 = new JdkVersion("Java10", 55, 11);
  public static final JdkVersion J2SE_9 = new JdkVersion("Java9", 54, 10);
  public static final JdkVersion J2SE_8 = new JdkVersion("Java8", 52, 9);
  public static final JdkVersion J2SE_7 = new JdkVersion("Java7", 51, 8);
  public static final JdkVersion J2SE_6_0 = new JdkVersion("Java6", 50, 7);
  public static final JdkVersion J2SE_5_0 = new JdkVersion("Java5", 49, 6);
  public static final JdkVersion JDK_1_4 = new JdkVersion("Java1.4", 48, 5);
  public static final JdkVersion JDK_1_3 = new JdkVersion("Java1.3", 47, 4);
  public static final JdkVersion JDK_1_2 = new JdkVersion("Java1.2", 46, 3);
  public static final JdkVersion JDK_1_1 = new JdkVersion("Java1.1", 45, 2);
  public static final JdkVersion JDK_1 = new JdkVersion("Java1", 196653, 1);
  public static final JdkVersion[] KNOWN_VERSIONS = new JdkVersion[]{
      J2SE_11,
      J2SE_10,
      J2SE_9,
      J2SE_8,
      J2SE_7,
      J2SE_6_0,
      J2SE_5_0,
      JDK_1_4,
      JDK_1_3,
      JDK_1_2,
      JDK_1_1,
      JDK_1
  };

  private final String name;
  private final int code;
  private final int order;

  JdkVersion(String name, int code, int order) {
    this.name = name;
    this.code = code;
    this.order = order;
  }

  public static JdkVersion parse(Object maxJdkArgument) {
    if (maxJdkArgument == null) {
      return null;
    }

    if (maxJdkArgument instanceof Integer) {
      return getJdkVersion((Integer) maxJdkArgument);
    }

    if (maxJdkArgument instanceof String) {
      String maxJdkArgument1 = (String) maxJdkArgument;
      try {
        return getJdkVersion(Integer.parseInt(maxJdkArgument1));
      } catch (NumberFormatException e) {
        for (JdkVersion knownVersion : KNOWN_VERSIONS) {
          if (knownVersion.getName().equals(maxJdkArgument1)) {
            return knownVersion;
          }
        }
      }
    }

    throw new IllegalArgumentException("Unknown jdk version specifier: " + maxJdkArgument
        + " please use either an integer or the correct name");
  }

  private static JdkVersion getJdkVersion(int version) {
    for (JdkVersion knownVersion : KNOWN_VERSIONS) {
      if (knownVersion.getCode() == version) {
        return knownVersion;
      }
    }

    return new JdkVersion("UNKNOWN_" + version, version, Integer.MAX_VALUE);
  }

  @Override
  public String toString() {
    return name + " (" + code + ")";
  }

  public int getOrder() {
    return order;
  }

  public String getName() {
    return name;
  }

  public int getCode() {
    return code;
  }
}
