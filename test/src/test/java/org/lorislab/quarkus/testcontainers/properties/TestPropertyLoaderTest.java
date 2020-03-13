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
package org.lorislab.quarkus.testcontainers.properties;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.lorislab.quarkus.testcontainers.DockerTestEnvironment;

public class TestPropertyLoaderTest {

    @Test
    public void testValueProperty() {
        DockerTestEnvironment env = new DockerTestEnvironment();
        String value = "123";
        String name = "name";
        TestProperty property = TestPropertyLoader.createTestProperty(name, value);
        Assertions.assertNotNull(property);
        Assertions.assertTrue(property instanceof TestValueProperty);
        TestValueProperty tv = (TestValueProperty) property;
        Assertions.assertEquals(name, tv.name);
        Assertions.assertEquals(value, tv.value);
        Assertions.assertEquals(value, tv.getValue(env));
    }

    @Test
    public void testGroupProperty() {
        String value1 = "123456";
        System.setProperty("value1", value1);

        DockerTestEnvironment env = new DockerTestEnvironment();
        String value = "test $${prop:value1} should be $${prop:value1}";
        String name = "name";
        TestProperty property = TestPropertyLoader.createTestProperty(name, value);
        Assertions.assertNotNull(property);
        Assertions.assertTrue(property instanceof TestGroupProperty);
        TestGroupProperty tg = (TestGroupProperty) property;
        Assertions.assertEquals(name, tg.name);
        Assertions.assertEquals(2, tg.testProperties.size());
        String output = tg.getValue(env);
        Assertions.assertEquals("test " + value1 + " should be " + value1, output);
    }
}
