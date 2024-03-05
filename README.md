# Abstractor

The purpose of Abstract Virtual Actors is to provide a very slim pure java implementation of actor based concurrency.

---

## Desgin goals
1. **No dependencies:** Only use java util imports, no funny stuff.
2. **Ultra Slim:** Keep all required source to [one file](https://github.com/gfiford/abstractor/blob/main/abstractor-core/src/main/java/com/fiford/Abstractor.java), with minimal length for easy analysis. This is so the code is easily auditable and can be included by copying one file into a project
3. **Typed:** Strong typing by design, to allow call heirarchy analysis, no suprise messages,and pure java actors
4. **Wireable:** Usable with standard runtime and pre compiled application contexts. In short spring support out of the box.
5. **Lightweight use:** No requirement to implement complex code to create or use actors.
6. **Anonymous & Functional**Message Actors can be created anonymously from a functionl interface, and typed actors are asked or told from clear functional blocks.
7. **Native Friendlly:** Actors ahould work within a native image compiled from graalvm
8. **Java Conventional:** No need for actor systems, registries or props, actors are instance objects, they can be called and the execution takes place within the actors context.
9. **Generic:** Just as the java collections library demonstrates the ability to sperate the behaviour of collections from their contents, message actors and typed actors are purely generic.
10. **Performant:** Java21 by default (native threads 8 compatible available too) with virtual threads consciously for the simplicity of memorybeing the limit on thread state, almost zero cost concurrency. No need for choices around actor execution environment.

## Easy
Anything can be made a strongly typed actor:

        //wrap a hashSet in an actor
        TypedActor<Set<Integer>> intSet = new TypedActor<>(new HashSet<Integer>());
        //add some stuff in parallel
        IntStream.range(0, 10000)
            .parallel()//safe to do to a hash set in an actor
            .forEach(i -> intSet.apply(s -> s.add(i)));
        //show that we have all our stuff
        System.out.println(intSet.ask(s -> s.size()).recieve().get());

Message based actors don't have to be verbose either:

        //Create a Message Actor with a lambda
        MessageActor<Double> squareRouter = new MessageActor<>(
            (msg, sender, self) -> sender.tell(Math.sqrt(msg), self));
        //send the message and get the reply
        System.out.println(squareRouter.ask(25.0).recieve().get());


## Origin
This library takes inspriation from previous actor systems, and leans heavily on work already done especially from many years using akka in a commercial context and being inspired by [Actr](https://github.com/zakgof/actr) as to what could be done differently. I have borrowed much of the test cases from actr.

## Examples
There are examples for a few major frameworks that lack any kind of actor like abstraction. Inside the test project are some examples of how it can be used.

## Go Play
It is a one file library, feel free to extend, by design it is intended to be extended and is designed to lend itself to abstraction, the name is a portmanteau of Abstractor and Actor.

### Framework Examples
There are framework simple examples (micronaut and spring) that can be run in the jvm or compiled to a native executable.