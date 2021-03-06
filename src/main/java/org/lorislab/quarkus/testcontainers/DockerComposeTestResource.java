package org.lorislab.quarkus.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The docker compose test resource.
 */
public class DockerComposeTestResource implements QuarkusTestResourceLifecycleManager{

    private static final Logger log = LoggerFactory.getLogger(DockerComposeTestResource.class);

    public static final String PROP_DISABLE = "lorislab.testcontainers.disable";

    /**
     * The docker test environment.
     */
    protected DockerTestEnvironment environment = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> start() {
        if (System.getProperty(PROP_DISABLE) == null) {
            environment = new DockerTestEnvironment();
            environment.start();
        } else {
            log.info("Quarkus test containers extension is disabled. '{}'", PROP_DISABLE);
        }
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        if (environment != null) {
            environment.stop();
        }
    }

    /**
     * Inject all {@link org.lorislab.quarkus.testcontainers.DockerComposeService} in the test class.
     * @param testInstance the test instance
     */
    @Override
    public void inject(Object testInstance) {
        List<Field> fields = getDockerComposeServiceFields(testInstance.getClass());
        for (Field f : fields) {
            f.setAccessible(true);
            try {
                DockerComposeService s = environment.getService(f.getAnnotation(DockerService.class).value());
                f.set(testInstance, s);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Gets all docker compose fields for the class.
     *
     * @param clazz the test class.
     * @return the corresponding list of fields.
     */
    private static List<Field> getDockerComposeServiceFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Field> result = new ArrayList<>(getDockerComposeServiceFields(clazz.getSuperclass()));
        result.addAll(
                Arrays.stream(clazz.getDeclaredFields())
                        .filter(f -> DockerComposeService.class.isAssignableFrom(f.getType()))
                        .filter(f -> f.getAnnotation(DockerService.class) != null)
                        .filter(f -> !f.getAnnotation(DockerService.class).value().isEmpty())
                        .collect(Collectors.toList())
        );
        return result;
    }

}
