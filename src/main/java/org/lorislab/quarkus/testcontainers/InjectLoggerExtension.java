package org.lorislab.quarkus.testcontainers;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The JUnit5 inject logger extension
 */
public class InjectLoggerExtension implements BeforeEachCallback {

    /**
     * {@inheritDoc }
     */
    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        List<Field> fields = getAllLoggerFields(context.getRequiredTestClass());
        for (Field f : fields) {
            f.setAccessible(true);
            f.set(context.getRequiredTestInstance(), LoggerFactory.getLogger(context.getRequiredTestInstance().getClass()));
        }
    }

    /**
     * Gets all logger fields for the class.
     *
     * @param clazz the test class.
     * @return the corresponding list of fields.
     */
    private static List<Field> getAllLoggerFields(Class<?> clazz) {
        if (clazz == null) {
            return Collections.emptyList();
        }
        List<Field> result = new ArrayList<>(getAllLoggerFields(clazz.getSuperclass()));
        result.addAll(
                Arrays.stream(clazz.getDeclaredFields())
                        .filter(f -> f.getType().equals(Logger.class))
                        .filter(f -> f.getAnnotation(Inject.class) != null)
                        .collect(Collectors.toList())
        );
        return result;
    }

}
