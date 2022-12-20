# Configuration Cache Serialization Contract

This repository contains some samples that demonstrate the changes to finalize the configuration cache serialization
contracts and make them stable. These changes will be included in Gradle 8.1.

## Java lambdas

A plugin can now use arbitrary lambdas in task state and these are serialized and deserialized correctly.
In previous versions, a plugin could only use lambdas for specific Gradle functional interfaces, such as
`TaskAction`.

To try this out, run the [`javaLamba`](java-plugins/src/main/java/test/JavaPlugin.java#L11) task.

This [task type](java-plugins/src/main/java/test/GreetingTask.java) uses a custom functional interface
and the plugin provides an implementation of this using a Java lambda. 

```shell
> ./gradlew javaLambda
> ./gradlew javaLambda
```

Compare this with the behavior in Gradle 7.6. The lambda is serialized in the cache miss build but cannot be
deserialized in the cache hit build:

```shell
> alias gradle76=# point this to a Gradle 7.6 installation or any recent 7.x version

# Succeeds
> gradle76 javaLambda

# Fails with class not found exception
> gradle76 javaLambda
```

Java lambdas that capture unsupported values are now detected when serialized in the cache miss build.
In previous versions, this would fail when deserialized in the cache hit build.

To try this out, run the [`brokenJavaLamba`](java-plugins/src/main/java/test/JavaPlugin.java#L16) task.

The cache miss build should fail with an exception that informs the user that the lambda cannot capture an unsupported type.

```shell
> ./gradlew brokenJavaLambda
```

Compare this with Gradle 7.6, where the cache miss build succeeds but should fail. The failure happens in the cache
hit build instead:

```shell
# Succeeds but should fail
> gradle76 brokenJavaLambda

# Fails with class cast exception
> gradle76 brokenJavaLambda
```

## Consistent handling of failures during serialization

During serialization, code is run to calculate the values that are serialized to the cache. For example,
the value of a `Provider` might be calculated or a dependency graph resolved. This might also include running user code,
for example a user function to map a `Provider` value or determine the default version of a dependency.

Failures in this code mean the work graph is not fully available and should cause the configuration cache entry to
be discarded. These failures are now consistently treated as fatal problems and when they happen the configuration cache
entry is discarded, task execution is skipped, and the build fails.

Previously, a "broken" value was serialized to the cache for failures in certain types and task execution attempted.
This behaviour turned out to be confusing for people, for example when a remote resource used during dependency
resolution happens to be temporarily unavailable during the cache miss build.

### Providers that fail during serialization

To try this out, use the [`brokenProvider`](broken-types/build.gradle.kts#L2) task.

The cache miss build should fail with an exception that informs the user that the provider could not be written to the cache,
along with the exception thrown by the user code.

```shell
> ./gradlew brokenProvider
```

Compare this with Gradle 7.6, where serialization would complete with a problem and then tasks would run and fail
with the same problem:

```shell
# Duplicate exceptions reported
> gradle76 brokenProvider
```

This behaviour is consistent with how, for example, file collections that fail during serialization are handled:

```shell
> ./gradlew brokenFileCollection
> gradle76 brokenFileCollection
```

### Java Serializable objects that fail during serialization

You can also use the [`brokenJavaSerialization`](broken-types/build.gradle.kts#L28) task to see the behaviour for custom 
Java serialization failures:

```shell
> ./gradlew brokenJavaSerialization
```

Compare this with Gradle 7.6, where the cause of the problem is less clear:

```shell
> gradle76 brokenJavaSerialization
```

## Groovy closures

After reviewing the behavior for Groovy closure serialization, we decided to leave the behaviour unchanged for the stable
serialization contract.

In short, the dynamic nature of Groovy makes it very difficult to know whether the body of a closure will
dynamically use any of the implicit state that is automatically captured for a closure - specifically the
`delegate`, `owner` and `this` properties of a closure. For closures defined in Groovy DSL build scripts or in
Gradle plugins implemented in dynamic Groovy these properties almost always
reference values that are not supported, for example the `Project` instance. Serializing these values will lead to 
unsupported types being reported regardless of whether the values are used or not. These closures represent the 
vast majority of closures serialized to the configuration cache.

Given this, we decided to continue to discard the implicit state when serializing and require build authors to refactor
their Groovy closures to extract local variables that explicitly reference whatever state the closure requires from the enclosing scopes.
We may consider adding some backwards compatible lenient behavior in later Gradle releases to
help reduce the need for this refactoring for closures in Groovy DSL build scripts.

We also improved error reporting to help discover these problems earlier, in the cache miss build.
This was demonstrated for the [load-after-store feature](https://github.com/adammurdoch/configuration-cache-load-after-store)

## Support for convention mapping

After reviewing the behavior for handling convention mappings during serialization, we decided to leave the
behaviour unchanged for the stable serialization contract.

Convention mappings are an ancient internal feature that has leaked into many plugins. They have been replaced by
the public `Property` and `Provider` APIs. The configuration cache serialization currently provides an "80% solution"
that works fine for most usages of convention mappings. For edge cases that don't work with this solution, we intend to
encourage plugin authors to migrate to the `Property` and `Provider` APIs. We can also support more usages in a backwards 
compatible way in later releases, if required.