# Inject

> The work on this software project is in no way associated with my employer nor with the role I'm having at my
> employer.
>
> I maintain this project alone and as much or as little as my **spare time** permits using my **personal** equipment.

Small [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) library for Java supporting
reflection-based objects creation (aka a [factory pattern](https://en.wikipedia.org/wiki/Factory_method_pattern)) using
slightly extended [JSR 330](https://jcp.org/en/jsr/detail?id=330) specification provided
by [Jakarta Inject library](https://mvnrepository.com/artifact/jakarta.inject/jakarta.inject-api).

The main goal of this library is to provide the most obvious path to register and reuse dependencies in your code, which
was inspired by other libraries such as:

- [Feather](https://github.com/zsoltherpai/feather) - with some features absent here, namely injection into the current
  class for testing.
- [Dagger 2](https://github.com/google/dagger) - the concept of `@Provides` is useful, code generation not so much.
- [Guice](https://github.com/google/guice) - without the bloat of additional annotations you might never use or support
  for Jakarta's `servlet,persistence`.

In addition to JSR 330 annotations, this library adds its own `@Provides` annotation to mark methods which produce
dependencies for other classes - something that is missing from the specification to let users denote which specific
methods outputs should be exposed to other classes.

## [How to add?](https://mvnrepository.com/artifact/io.github.suppierk/inject)

- **Maven**

```xml
<dependency>
    <groupId>io.github.suppierk</groupId>
    <artifactId>inject</artifactId>
    <version>1.2.0</version>
</dependency>
```

- **Gradle** (_works for both Groovy and Kotlin_)

```groovy
implementation("io.github.suppierk:inject:1.2.0")
```

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-orange.svg)](https://sonarcloud.io/summary/overall?id=SuppieRK_inject)

## Hello, World!

```java
import jakarta.inject.Inject;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.inject.Injector;

public class HelloWorld {
    static class Provider {
        @Provides
        String helloWorld() {
            return "Hello, World!";
        }
    }

    static class Consumer implements Runnable {
        private final String helloWorld;

        @Inject
        public Consumer(String helloWorld) {
            this.helloWorld = helloWorld;
        }

        @Override
        public void run() {
            System.out.println(helloWorld);
        }
    }

    public static void main(String[] args) {
        final Injector injector = Injector.injector()
                .add(Provider.class, Consumer.class)
                .build();

        injector.get(Consumer.class).run();
    }
}
```

## Known problems

- Do not invoke `Provider.get()` or `Supplier.get()` in the constructor of the object which is a part of the dependency
  cycle - this will result in an infinite instantiation loop and will cause your program to become stuck.

## More examples

- Visit our [Wiki](https://github.com/SuppieRK/inject/wiki) for more in-depth look at the features.
- Take a look at the complete stack proposal in [JIQS](https://github.com/SuppieRK/jiqs) repository.
