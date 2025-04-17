package org.example;

import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: java -jar playwright-groovy-launcher.jar PlaywrightEdgeGroovyDemo.groovy");
            System.exit(1);
        }
        GroovyShell shell = new GroovyShell();
        shell.run(new File(args[0]), args);
    }
}
