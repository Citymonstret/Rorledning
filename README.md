# Rörledning

This is a library that allows you to create services, that can have several different implementations.
A service in this case, is anything that takes in a context, and spits out some sort of result, achieving
some pre-determined task.

Examples of services would be generators and caches.

## Links

- Discord: https://discord.gg/KxkjDVg
- JavaDoc: https://plotsquared.com/docs/rörledning/

## Maven

Rörledning is available from [IntellectualSites](https://intellectualsites.com)' maven repository:

```xml
<repository>
    <id>intellectualsites-snapshots</id>
    <url>https://mvn.intellectualsites.com/content/repositories/snapshots</url>
</repository>
```

```xml
<dependency>
    <groupId>com.intellectualsites</groupId>
    <artifactId>Pipeline</artifactId>
    <version>1.2-SNAPSHOT</version>
</dependency>
```

## Usage

### ServicePipeline

All requests start in the `ServicePipeline`. To get an instance of the `ServicePipeline`, simply use
the service pipeline builder.

**Example:**

```java
final ServicePipeline servicePipeline = ServicePipeline.builder().build();
```

### Service

To implement a service, simply create an interface that extends `Service<Context, Result>`.
The context is the type that gets pumped into the service (i.e, the value you provide), and the result
is the type that gets produced by the service.

The pipeline will attempt to generate a result from each service, until a service produces a non-null result.
Thus, if a service cannot (or shouldn't) produce a result for a given context, it can simply return null.

However, there's a catch to this. At least one service must always provide a result for every input.
To ensure that this is the case, a default implementation of the service must be registered together
with the service type. This implementation is not allowed to return null.

**Examples:**

Example Service:

```java
public interface MockService extends Service<MockService.MockContext, MockService.MockResult> {

    class MockContext {

        private final String string;

        public MockContext(@Nonnull final String string) {
            this.string = string;
        }

        @Nonnull public String getString() {
            return this.string;
        }

    }

    class MockResult {

        private final int integer;

        public MockResult(final int integer) {
            this.integer = integer;
        }

        public int getInteger() {
            return this.integer;
        }

    }

}
```

Example Implementation:

```java
public class DefaultMockService implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(32);
    }

}
```

Example Registration:

```java
servicePipeline.registerServiceType(TypeToken.of(MockService.class), new DefaultMockService());
```

Example Usage:

```java
final int result = servicePipeline.pump(new MockService.MockContext("Hello"))
                                  .through(MockService.class)
                                  .getResult()
                                  .getInteger();
```

### SideEffectService

Some services may just alter the state of the incoming context, without generating any (useful) result.
These services should extend `SideEffectService`.

SideEffectService returns a State instead of a result. The service may either accept a context, in
which case the execution chain is interrupted. It can also reject the context, in which case the
other services in the execution chain will get a chance to consume it.

**Example:**

```java
public interface MockSideEffectService extends SideEffectService<MockSideEffectService.MockPlayer> {

    class MockPlayer {

        private int health;

        public MockPlayer(final int health) {
            this.health = health;
        }

        public int getHealth() {
            return this.health;
        }

        public void setHealth(final int health) {
            this.health = health;
        }

    }

}

public class DefaultSideEffectService implements MockSideEffectService {

    @Nonnull @Override public State handle(@Nonnull final MockPlayer mockPlayer) {
        mockPlayer.setHealth(0);
        return State.ACCEPTED;
    }

}
```

### Asynchronous Execution

The pipeline results can be evaluated asynchronously. Simple use `getResultAsynchronously()`
instead of `getResult()`. By default, a single threaded executor is used. A different executor
can be supplied to the pipeline builder.

### Filters

Sometimes you may not want your service to respond to certain contexts. Instead of always
returning null in those cases, filters can be used. These are simply predicates that take in your
context type, and should be registered together with your implementation.

**Example:**

Example Filter:
```java
public class FilteredMockService implements MockService, Predicate<MockService.MockContext> {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(999);
    }

    @Override public boolean test(final MockContext mockContext) {
        return mockContext.getString().equalsIgnoreCase("potato");
    }

}
```

Example Registration:

```java
final FilteredMockService service = new FilteredMockService();
final List<Predicate<MockService.MockContext>> predicates = Collections.singletonList(service);
servicePipeline.registerServiceImplementation(MockService.class, service, predicates);
```

### Forwarding

Sometimes it may be useful to use the result produced by a service as the context for another service.
To make this easier, the concept of forwarding was introduced. When using `getResult()`, one can instead
use `forward()`, to pump the result back into the pipeline.

**Examples:**

```java
servicePipeline.pump(new MockService.MockContext("huh"))
               .through(MockService.class)
               .forward()
               .through(MockResultConsumer.class)
               .getResult();
```

This can also be done asynchronously:

```java
servicePipeline.pump(new MockService.MockContext("Something"))
               .through(MockService.class)
               .forwardAsynchronously()
               .thenApply(pump -> pump.through(MockResultConsumer.class))
               .thenApply(ServiceSpigot::getResult)
               .get();
```

### Priority/Ordering

By default, all service implementations will be executed in first-in-last-out order. That is,
the earlier the implementation was registered, the lower the priority it gets in the execution chain.

This may not always be ideal, and it is therefore possibly to override the natural ordering
of the implementations by using the &#64;Optional annotation.

**Example:**

```java
@Order(ExecutionOrder.FIRST)
public class MockOrderedFirst implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(1);
    }

}

@Order(ExecutionOrder.LAST)
public class MockOrderedLast implements MockService {

    @Nullable @Override public MockResult handle(@Nonnull final MockContext mockContext) {
        return new MockResult(2);
    }

}
```

No matter in which order MockOrderedFirst and MockOrderedLast are added, MockOrderedFirst will be
handled before MockOrderedLast.

The default order for all services is `SOON`.

### Annotated Methods

You can also implement services by using instance methods, like such:

```java
@ServiceImplementation(MockService.class)
public MockService.MockResult handle(@Nonnull final MockService.MockContext context) {
    return new MockService.MockResult(context.getString().length());
}
```

The methods can also be annotated with the order annotation. Is is very important
that the method return type and parameter type match up wit the service context and
result types, or you will get runtime exceptions when using the pipeline. 

These methods are registered in ServicePipeline, using `registerMethods(yourClassInstance);`
