package org.test;

import org.test.annotation.Bean;
import org.test.annotation.Configuration;
import org.test.carParts.Engine;

@Configuration
public class CustomConfig {

    @Bean
    public Engine engine(){
        return new Engine();
    }

    @Bean
    public String carName(){
        return "Ferrari";
    }

}
