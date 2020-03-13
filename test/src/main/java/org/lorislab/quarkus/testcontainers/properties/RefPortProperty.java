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

import org.lorislab.quarkus.testcontainers.DockerComposeService;
import org.lorislab.quarkus.testcontainers.DockerTestEnvironment;

public class RefPortProperty extends TestProperty {

    String service;

    String port;

    @Override
    public String getValue(DockerTestEnvironment environment) {
        DockerComposeService dcs = environment.getService(service);
        return "" + dcs.getPort(Integer.parseInt(port));
    }

    public static RefPortProperty createTestProperty(String name, String[] data) {
        RefPortProperty r = new RefPortProperty();
        r.name = name;
        r.service = data[1];
        r.port = data[2];
        return r;
    }
}
