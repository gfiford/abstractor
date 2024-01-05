package com.fiford.abstractorspring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fiford.Abstractor.TypedActor;


@Configuration
public class Greeter {

    public String greet() {
        return "Hello World!";
    }

    
    @Bean
    TypedActor<Greeter> actor() {
        return new TypedActor<Greeter>(new Greeter());
    }
}