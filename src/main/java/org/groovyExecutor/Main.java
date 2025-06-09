package org.groovyExecutor;

import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java -jar simplews.jar myScript.groovy [externalJarsFolder] [scriptArgs...]");
            System.exit(1);
        }

        // Determine external jar folder: from argument, or default to ./lib
        String extJarFolder = (args.length >= 2) ? args[1] : "lib";
        File extDir = new File(extJarFolder);

        // Only try to load jars if the folder exists
        if (extDir.exists() && extDir.isDirectory()) {
            File[] jarFiles = extDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null && jarFiles.length > 0) {
                System.out.println("Loading external jars from: " + extDir.getAbsolutePath());
                URL[] urls = new URL[jarFiles.length];
                for (int i = 0; i < jarFiles.length; i++) {
                    urls[i] = jarFiles[i].toURI().toURL();
                    System.out.println("  -> " + jarFiles[i].getAbsolutePath());
                }
                ClassLoader parent = Thread.currentThread().getContextClassLoader();
                URLClassLoader urlClassLoader = new URLClassLoader(urls, parent);
                Thread.currentThread().setContextClassLoader(urlClassLoader);
            } else {
                System.out.println("No jars found in: " + extDir.getAbsolutePath());
            }
        }

        // Compute script args: skip [scriptFile, jarFolder] if present
        int scriptArgStart = (args.length >= 2) ? 2 : 1;
        String[] scriptArgs = (args.length > scriptArgStart)
            ? java.util.Arrays.copyOfRange(args, scriptArgStart, args.length)
            : new String[0];

        GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader());
        shell.run(new File(args[0]), scriptArgs);
    }
}
