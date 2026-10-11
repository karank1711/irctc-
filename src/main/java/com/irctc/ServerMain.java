package com.irctc;

import com.irctc.util.EnvironmentConfig;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/** Starts an embedded Tomcat server from the project root. */
public class ServerMain {
    public static void main(String[] args) throws Exception {
        EnvironmentConfig config = EnvironmentConfig.getInstance();
        int port = Integer.parseInt(config.getOrDefault("PORT", "8080"));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        File webapp = new File("src/main/webapp");
        if (!webapp.isDirectory()) {
            throw new IllegalStateException("Run the server from the project root so src/main/webapp can be found.");
        }

        Context context = tomcat.addWebapp("", webapp.getAbsolutePath());
        // Maven puts Tomcat on this class loader, not on the system class loader.
        context.setParentClassLoader(ServerMain.class.getClassLoader());

        StandardRoot resources = new StandardRoot(context);
        File classes = new File("target/classes");
        File libraries = new File("target/web-libs");
        if (classes.isDirectory()) {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", classes.getAbsolutePath(), "/"));
        }
        if (libraries.isDirectory()) {
            resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/lib", libraries.getAbsolutePath(), "/"));
        }
        context.setResources(resources);

        tomcat.start();
        System.out.println("IRCTC Admin Dashboard is running at http://localhost:" + port + "/login");
        tomcat.getServer().await();
    }
}
