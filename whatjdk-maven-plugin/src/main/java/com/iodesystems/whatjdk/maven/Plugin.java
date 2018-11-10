package com.iodesystems.whatjdk.maven;

import com.iodesystems.whatjdk.ClassEntry;
import com.iodesystems.whatjdk.JdkVersion;
import com.iodesystems.whatjdk.WhatJDK;
import com.iodesystems.whatjdk.listeners.OnClassReferenceListener;
import com.iodesystems.whatjdk.listeners.OnJdkVersionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
    name = "verify",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    threadSafe = true)
public class Plugin extends AbstractMojo {

  @Component
  private MavenProject project;

  @Parameter
  private String maxJdkVersion;

  @Parameter
  private String usesClasses;

  public void execute() throws MojoExecutionException {
    WhatJDK whatJDK = new WhatJDK();

    // Don't bother testing poms!
    if (project.getPackaging().equalsIgnoreCase("pom")) {
      return;
    }

    final File file = project.getArtifact().getFile();
    if (file == null) {
      throw new MojoExecutionException("Cannot verify before the package has been built");
    }

    List<String> jarFiles = new ArrayList<String>();
    jarFiles.add(file.getAbsolutePath());
    for (Artifact artifact : project.getDependencyArtifacts()) {
      if ("compile".equalsIgnoreCase(artifact.getScope())) {
        jarFiles.add(artifact.getFile().getAbsolutePath());
      }
    }

    whatJDK.setJarFiles(jarFiles);
    whatJDK.setScanInnerJarFiles(false);
    whatJDK.setUsesClasses(WhatJDK.parseUsesClass(usesClasses));
    whatJDK.setMaxJdkVersion(JdkVersion.parse(maxJdkVersion));
    whatJDK.setOnClassReferenceListener(new OnClassReferenceListener() {
      @Override
      public void onClassReferenced(ClassEntry classEntry, String name) {
        getLog().error(classEntry.getContainer() + ":" + classEntry.getFileName()
            + " contains classes with references to a blacklisted class: " + name);
      }
    });

    whatJDK.setOnInvalidJdkVersionListener(new OnJdkVersionListener() {
      @Override
      public void onJdkVersion(ClassEntry classEntry, int version) {
        getLog().error(classEntry.getContainer() + ":" + classEntry.getFileName()
            + " is compiled with an invalid JDK: " + JdkVersion.parse(version).toString());
      }
    });

    if (!whatJDK.execute()) {
      throw new MojoExecutionException("Package violates configured whatJDK specification");
    }

  }
}
