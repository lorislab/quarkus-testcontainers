/*
 * Copyright 2019 lorislab.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lorislab.quarkus.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Network;
import org.testcontainers.shaded.org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The docker test environment.
 */
public class DockerTestEnvironment {

    private static final Logger log = LoggerFactory.getLogger(DockerTestEnvironment.class);

    private Map<String, DockerComposeService> containers = new HashMap<>();

    private Map<Integer, List<DockerComposeService>> containerProperties = new HashMap<>();

    private Network network;

    public DockerTestEnvironment() {
        this(System.getProperty("test.docker.compose.file", "./src/test/resources/docker-compose.yml"));
    }

    public DockerTestEnvironment(String dockerComposeFile) {
        load(new File(dockerComposeFile));
    }

    public DockerComposeService getService(String name) {
        return containers.get(name);
    }

    public Network getNetwork() {
        return network;
    }

    public void load(File dockerComposeFile) {
        network = Network.newNetwork();

        boolean integrationTest = Boolean.getBoolean("test.integration");

        Yaml yaml = new Yaml();

        try (InputStream fileInputStream = Files.newInputStream(dockerComposeFile.toPath())) {
            Map<String, Object> map = yaml.load(fileInputStream);
            Object services = map.get("services");
            if (services instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) services;
                data.forEach((k, v) -> {
                    ContainerConfig config = ContainerConfig.createContainerProperties(k, (Map<String, Object>) v);
                    if ((integrationTest && config.integrationTest) || (!integrationTest && config.unitTest)) {
                        DockerComposeService service = DockerComposeService.createDockerComposeService(network, config);
                        containerProperties.computeIfAbsent(service.getConfig().priority, x -> new ArrayList<>()).add(service);
                        containers.put(k, service);
                    }
                });
            }
        } catch (IOException e) {
            log.warn("Failed to read YAML from {}", dockerComposeFile.getAbsolutePath(), e);
        }
    }

    public void start() {
        List<Integer> priorities = new ArrayList<>(containerProperties.keySet());
        Collections.sort(priorities);

        priorities.forEach(p -> {
            List<DockerComposeService> services = containerProperties.get(p);
            List<String> names = services.stream().map(DockerComposeService::getName).collect(Collectors.toList());
            DockerTestSystemLogger.log("Starting ...\n------------------------------\nStart test containers\npriority: " + p + "\nServices: " + names + "\n------------------------------");
            services.parallelStream().forEach(s -> s.start(this));
        });
    }

    public void stop() {
        containers.values().parallelStream().forEach(DockerComposeService::stop);
    }

}
