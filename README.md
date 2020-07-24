# Rörledning

Upcoming java service framework

## Usage

TODO: Write stuff here

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
