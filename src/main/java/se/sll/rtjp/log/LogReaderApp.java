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
package se.sll.rtjp.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Application entry and configuration class. <p/>
 *
 * Please note: this is the main start-class declared in the maven properties section (see also pom.xml)
 */
@Configuration
@ComponentScan
@PropertySources(value = {
        @PropertySource(value = "classpath:log-reader.properties"),
        @PropertySource(value = "file://${log-reader-config}", ignoreResourceNotFound = true)
})
public class LogReaderApp implements CommandLineRunner {

    @Autowired
    private LogProducer logProducer;

    @Override
    public void run(String... args) throws Exception {
        logProducer.run();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    /**
     * Launches application utilizing spring boot.
     *
     * @param args all are ignored.
     * @throws Exception on any bootstrap error.
     */
    public static void main(String[] args) throws Exception {
        final SpringApplication app = new SpringApplication(LogReaderApp.class);
        app.setShowBanner(false);
        app.run(args);
    }
}
