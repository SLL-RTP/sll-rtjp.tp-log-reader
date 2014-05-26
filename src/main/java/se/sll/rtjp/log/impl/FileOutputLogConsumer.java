/**
 *  Copyright (c) 2014 SLL <http://sll.se/>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package se.sll.rtjp.log.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import reactor.event.Event;
import reactor.function.Consumer;
import se.sll.rtjp.log.api.LogEvent;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Transforms log events to JSON records and logs them to a consolidated log file. <p/>
 *
 * Log format and rotation is configured in the configuration file logback.xml, see appender FILE.
 */
@Component
@Slf4j
public class FileOutputLogConsumer implements Consumer<Event<LogEvent>> {

    private static final ObjectMapper mapper = new ObjectMapper();


    // Makes it possible to reuse string writers.
    private static ThreadLocal<StringWriter> writer = new ThreadLocal<StringWriter>() {
        @Override
        public StringWriter initialValue() {
            return new StringWriter();
        }
    };

    @Override
    public void accept(Event<LogEvent> event) {
        log.info(toJsonString(event.getData()));
    }

    /**
     * Transforms an output event to a JSON string.
     *
     * @param event the output event.
     * @return the JSON representation.
     */
    @SneakyThrows(IOException.class)
    protected String toJsonString(final LogEvent event){
        final StringWriter sw = writer.get();
        sw.getBuffer().setLength(0);
        mapper.writeValue(sw, event);
        return sw.toString();
    }

}
