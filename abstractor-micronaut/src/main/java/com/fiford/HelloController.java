package com.fiford;

import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import java.util.Collections;
import java.util.Map;

import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.TypedActor;

@Controller("/hello")
class HelloController {

    @Inject
    TypedActor<Greeter> greeterActor;

    @Inject
    MessageActor<String> customGreeter;

    @Get
    Map<String, String> index() {
        return Collections.singletonMap("message",
                greeterActor.ask(g -> g.greet()).recieve().get());
    }

    @Get("/{name}")
    Map<String, String>  greetName(String name) {
        return Collections.singletonMap("message", customGreeter.ask(name).recieve().get());
    }

}
