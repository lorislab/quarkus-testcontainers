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

import org.testcontainers.containers.output.BaseConsumer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.Slf4jLogConsumer;

/**
 * The system out container logger.
 */
public class ContainerLogger extends BaseConsumer<Slf4jLogConsumer> {

    /**
     * The line break pattern
     */
    private static final String LINE_BREAK_AT_END_REGEX = "((\\r?\\n)|(\\r))$";

    /**
     * The logger prefix
     */
    private final String prefix;

    /**
     * The builder method.
     *
     * @param prefix the logger prefix.
     * @return the system out container logger.
     */
    public static ContainerLogger create(String prefix) {
        return new ContainerLogger(prefix);
    }

    /**
     * The default constructor.
     *
     * @param prefix the logger prefix.
     */
    private ContainerLogger(String prefix) {
        this.prefix = "[" + prefix + "] ";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("squid:S106")
    public void accept(OutputFrame outputFrame) {
        OutputFrame.OutputType outputType = outputFrame.getType();

        String utf8String = outputFrame.getUtf8String();
        utf8String = utf8String.replaceAll(LINE_BREAK_AT_END_REGEX, "");
        switch (outputType) {
            case END:
                break;
            case STDOUT:
                System.out.println(prefix + utf8String);
                break;
            case STDERR:
                System.err.println(prefix + utf8String);
                break;
            default:
                throw new IllegalArgumentException("Unexpected outputType " + outputType);
        }
    }
}