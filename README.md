# Rörledning

Upcoming java service framework

## Usage

TODO: Write stuff here

### SideEffectService

Some services may just alter the state of the incoming context, without generating any (useful) result.
These services should implement `SideEffectService`.

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
