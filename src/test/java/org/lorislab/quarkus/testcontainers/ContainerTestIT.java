package org.lorislab.quarkus.testcontainers;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ContainerTestIT extends AbstractContainerTest {


    @Test
    public void test() {
        Assertions.assertTrue(true);
    }

}

@QuarkusTestcontainers
@QuarkusTestResource(DockerComposeTestResource.class)
class AbstractContainerTest {

}