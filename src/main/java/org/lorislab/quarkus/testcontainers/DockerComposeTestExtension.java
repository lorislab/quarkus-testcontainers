/*
 * Copyright 2020 lorislab.org.
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

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DockerComposeTestExtension implements BeforeAllCallback, BeforeEachCallback {

    private static final Logger log = LoggerFactory.getLogger(DockerComposeTest.class);

    @Override
    public void beforeAll(ExtensionContext context) {
        log.debug("Start docker compose test extension");
        context.getRoot()
                .getStore(ExtensionContext.Namespace.GLOBAL)
                .getOrComputeIfAbsent(DockerComposeStore.class);
    }


    @Override
    public void beforeEach(ExtensionContext context) {
        DockerComposeStore store = context.getRoot()
                .getStore(ExtensionContext.Namespace.GLOBAL)
                .getOrComputeIfAbsent(DockerComposeStore.class);
        store.inject(context.getRequiredTestInstance());
    }

    static class DockerComposeStore extends DockerComposeTestResource implements ExtensionContext.Store.CloseableResource {

        public DockerComposeStore() {
            start();
        }

        @Override
        public void close() {
            environment.stop();
        }
    }
}


