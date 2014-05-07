package se.sll.rtjp.log;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@EnableAutoConfiguration
@Slf4j
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

    //
    public static void main(String[] args) throws Exception {
        SpringApplication.run(LogReaderApp.class, args);
    }
}
