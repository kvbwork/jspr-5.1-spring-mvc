package ru.netology;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args) throws IOException, LifecycleException {
        final var port = 8080;
        final var baseDir = Files.createTempDirectory("tomcat");
        baseDir.toFile().deleteOnExit();

        final var tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toAbsolutePath().toString());

        final var connector = new Connector();
        connector.setPort(port);
        tomcat.setConnector(connector);

        tomcat.getHost().setAppBase(".");
        tomcat.addWebapp("", ".");

        tomcat.start();
        tomcat.getServer().await();
    }

}
