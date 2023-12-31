package com.fiford;

import com.fiford.Abstractor.TypedActor;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class Greeter {

    public String greet() {
        return "Hello World!";
    }

    @Singleton
    TypedActor<Greeter> actor() {
        return new TypedActor<Greeter>(new Greeter());
    }
}