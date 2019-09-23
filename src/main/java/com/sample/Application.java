package com.sample;

import com.sample.config.WebServiceConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .initializers(new WebServiceConfig.CustomWsInitializer())
                .sources(Application.class)
                .run(args);
    }
}
