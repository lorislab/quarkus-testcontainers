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

import org.lorislab.quarkus.testcontainers.DockerTestEnvironment;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestGroupProperty extends TestProperty {

    MessageFormat message;

    List<TestProperty> testProperties = new ArrayList<>();

    @Override
    public String getValue(DockerTestEnvironment environment) {
        List<String> parameters = testProperties.stream().map(c -> c.getValue(environment)).collect(Collectors.toList());
        return message.format(parameters.toArray(new Object[]{}), new StringBuffer(), null).toString();
    }

    public static TestGroupProperty createTestProperty(String name, String data, List<TestProperty> testProperties) {
        TestGroupProperty r = new TestGroupProperty();
        r.name = name;
        r.message = new MessageFormat(data);
        r.testProperties = testProperties;
        return r;
    }
}
