package com.iodesystems.whatjdk.listeners;

import com.iodesystems.whatjdk.ClassEntry;

public interface OnJdkVersionListener {

  void onJdkVersion(ClassEntry classEntry, int version);
}
