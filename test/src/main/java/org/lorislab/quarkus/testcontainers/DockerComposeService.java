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

import org.lorislab.quarkus.testcontainers.properties.TestProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.MountableFile;

import java.util.*;
import java.util.stream.Collectors;

public class DockerComposeService {

    private static final Logger log = LoggerFactory.getLogger(DockerComposeService.class);

    private GenericContainer<?> container;

    private ContainerConfig config;

    private DockerComposeService(Network network, ContainerConfig config) {
        this.config = config;
        this.container = createContainer(network, config);
    }

    public static DockerComposeService createDockerComposeService(Network network, ContainerConfig config) {
        return new DockerComposeService(network, config);
    }

    public String getName() {
        return config.name;
    }

    public ContainerConfig getConfig() {
        return config;
    }

    public GenericContainer<?> getContainer() {
        return container;
    }

    public void start(DockerTestEnvironment environment) {
        if (container == null) {
            return;
        }
        // update environment variables
        Map<String, String> env = createValues(environment, config.refEnvironments);
        log.info("Service: '{}' add test environment variables: {}", config.name, env);
        container.withEnv(env);

        // start container
        container.start();

        // update properties
        Map<String, String> prop = createValues(environment, config.properties);
        System.out.println("Service: '{}' update test properties: {}");
        prop.forEach(System::setProperty);
    }

    private static Map<String, String> createValues(DockerTestEnvironment environment, List<TestProperty> properties) {
        return properties.stream().collect(Collectors.toMap(p -> p.name, p -> p.getValue(environment)));
    }

    public void stop() {
        // clear system properties
        config.properties.forEach(p -> System.clearProperty(p.name));

        // stop container
        container.stop();
    }

    public Integer getPort(int port) {
        return getPort(container, port);
    }

    public static Integer getPort(GenericContainer<?> container, int port) {
        return container.getMappedPort(port);
    }

    public String getHost() {
        return getHost(container);
    }

    public static String getHost(GenericContainer<?> container) {
        return container.getContainerIpAddress();
    }

    public String getUrl(int port) {
        return getUrl(container, port);
    }

    public static String getUrl(GenericContainer<?> container, int port) {
        return "http://" + getHost(container) + ":" + getPort(container, port);
    }

    private GenericContainer<?> createContainer(Network network, ContainerConfig config) {

        try (GenericContainer<?> result = new GenericContainer<>(config.image)) {
            result.withNetwork(network).withNetworkAliases(config.name);

            // image pull policy
            switch ( config.imagePull) {
                case ALWAYS:
                    result.withImagePullPolicy(PullPolicy.alwaysPull());
                    break;
                case MAX_AGE:
                    result.withImagePullPolicy(PullPolicy.ageBased(config.imagePullDuration));
                    break;
                case DEFAULT:
                    result.withImagePullPolicy(PullPolicy.defaultPolicy());
            }

            // wait log rule
            if (config.waitLogRegex != null) {
                result.waitingFor(Wait.forLogMessage(config.waitLogRegex, config.waitLogTimes));
            }

            // update log flag
            if (config.log) {
                result.withLogConsumer(ContainerLogger.create(config.name));
            }

            // environments
            config.environments.forEach(result::withEnv);

            // volumes
            config.volumes.forEach((k, v) -> {
                String key = k;
                if (key.startsWith("./")) {
                    key = key.substring(1);
                }
                result.withCopyFileToContainer(MountableFile.forClasspathResource(key), v);
            });

            // ports
            config.ports.values().stream().map(Integer::parseInt).forEach(result::addExposedPort);

            return result;
        }
    }

}
