package com.nthalk.whatjdk.maven;

import com.nthalk.whatjdk.WhatJDK;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mojo(
    name = "verify",
    defaultPhase = LifecyclePhase.PACKAGE,
    requiresProject = true,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    threadSafe = true)
public class Plugin extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Parameter(required = true)
    private String allowedJdkVersions;

    public void execute() throws MojoExecutionException, MojoFailureException {
        WhatJDK whatJDK = new WhatJDK();

        final Set<String> allowedVersionsSet = new HashSet<String>();
        for (String allowedVersion : allowedJdkVersions.split(",")) {
            allowedVersionsSet.add(allowedVersion.trim());
        }


        final File file = project.getArtifact().getFile();
        String fileName = file.getAbsolutePath();
        whatJDK.checkFileExtensionCompatibility(fileName);
        try {
            whatJDK.extractClassVersionInfoForJar(fileName, new WhatJDK.Handler() {
                Map<String, Set<String>> violations = new HashMap<String, Set<String>>();

                public void handle(String fileName, Set<String> versions) {
                    for (String version : versions) {
                        if (!allowedVersionsSet.contains(version)) {
                            violations.put(fileName, versions);
                            getLog().error(fileName + " contains classes compiled with the following JDK " + versions);
                            break;
                        }
                    }
                }

                @Override
                public void onFinish() throws Exception {
                    if (!violations.isEmpty()) {
                        throw new MojoFailureException("UallowedJdkVersions found.");
                    }
                }
            });
        } catch (MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Error running WhatJDK", e);
        }
    }
}
