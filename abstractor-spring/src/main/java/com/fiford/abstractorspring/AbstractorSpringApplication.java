package com.fiford.abstractorspring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fiford.Abstractor.MessageActor;
import com.fiford.Abstractor.TypedActor;

@RestController
@SpringBootApplication
public class AbstractorSpringApplication {

    @Autowired
    TypedActor<Greeter> greeterActor;

    @Autowired
    MessageActor<String> customGreeter;



	@RequestMapping("/hello")
    String hello() {
        return greeterActor.ask(g -> g.greet()).recieve().get();
    }


	@RequestMapping("/hello/{name}")
    String helloName(@PathVariable("name") String name) {
        return customGreeter.ask(name).recieve().get();
    }

	public static void main(String[] args) {
		SpringApplication.run(AbstractorSpringApplication.class, args);
	}

}
