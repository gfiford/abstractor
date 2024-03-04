Abstrator
---
The purpose of Abstract Virtual Actors is to provide a very slim pure java implementation of actor based concurrency.
---

Desgin goals
1. No dependencies  - Only use native java imports
2. Ultra Slim - Keep all required source to one file, with minimal length for easy analysis. This is so the code is easily auditable and can be included by copying one file into w project
3. Typed - Strong typing by design, to allow call heirarchy analysis, no suprise messages,and pure java actors
4. Wireable - usable with standard runtime and pre compiled application contexts. In short spring support out of the box.
5. Lightweight use - No requirement to implement complex code to create or use actors.
6. Anonymous & Functional - Message Actors can be created anonymously from a functionl interface, and typed actors are asked or told from clear functional blocks.
7. Native Friendlly - Actors ahould work within a native image compiled from graalvm
8. Java Conventional - No need for actor systems, registries or props, actors are instance objects, they can be called and the execution takes place within the actors context.
9. Generic - Just as the java collections library demonstrates the ability to sperate the behaviour of collections from their contents, message actors and typed actors are purely generic.
10. Performant - Requiring java21 with virtual threads consciously for the simplicity of memorybeing the limit on thread state, almost zero cost concurrency. No need for choices around actor execution environment.

---
This library takes inspriation from previous actor systems, and leans heavily on work already done especially from many years using akka in a commercial context and being inspired by https://github.com/zakgof/actr as to what could be done. I have borrowed much of the test casese from actr.

There are examples for a few majoy frameworks that lack any kind of actor like abstraction.
s
---
It is a one file library, feel free to extend, by design it is intended to be extended and is designed to lend itself to abstraction, the name is a portmanteau of Abstractor and Actor.
