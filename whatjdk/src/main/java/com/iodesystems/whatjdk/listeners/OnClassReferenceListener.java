package com.iodesystems.whatjdk.listeners;

import com.iodesystems.whatjdk.ClassEntry;

public interface OnClassReferenceListener {
    void onClassReferenced(ClassEntry classEntry, String name);
}
