package org.lorislab.quarkus.testcontainers;

public class DockerTestSystemLogger {

    static String PREFIX = "[testcontainers] ";

    private DockerTestSystemLogger() {}

    public static void log(String message) {
        System.out.println(PREFIX + message);
    }
}
