# Configuration Cache Serialization Contract

This repository contains some samples that demonstrate the changes to stabilize the configuration cache serialization
contracts.

## Java lambdas

A plugin can now use arbitrary lambdas in task state and these are serialized and deserialized correctly.
In previous versions, a plugin could only use lambdas for a small number of Gradle specific functional interfaces
(for example `TaskAction`).

To try this out, run the [`javaLamba` task](java-plugins/src/main/java/test/JavaPlugin.java#L11)

```shell
> ./gradlew javaLambda
> ./gradlew javaLambda
```

Compare this with the behavior with Gradle 7.6. The lambda is serialized in the cache miss build but cannot be
deserialized in the cache hit build:

```shell
> alias gradle76=# point this to a Gradle 7.6 installation or any recent 7.x version
> gradle76 javaLambda
# Fails with class not found exception
> gradle76 javaLambda
```

Java lambdas that capture unsupported values are now detected when serialized. In previous versions, this would fail
when deserialized in the cache hit build.

To try this out, run the [`brokenJavaLamba` task](java-plugins/src/main/java/test/JavaPlugin.java#L16). The cache
miss build should fail with an exception that says that the lambda cannot capture an unsupported type.

```shell
> ./gradlew brokenJavaLambda
```

Compare this with Gradle 7.6, where the cache miss build succeeds but should fail, The failure happens in the cache
hit build instead:

```shell
# Succeeds but should fail
> gradle76 brokenJavaLambda
# Fails with class cast exception
> gradle76 brokenJavaLambda
```

## Consistent handling of failures during serialization

Failures that happen during serialization are now consistently treated as fatal problems. When these happen, the configuration cache
entry is discarded and the build fails.

Previously, a "broken" value was serialized to the cache for certain types. This behaviour turned out to be confusing
for people, for example when a remote resource used during serialization happened to be temporarily unavailable during
the cache miss build.

### Providers that fail during serialization

To try this out, use the [`brokenProvider` task](broken-types/build.gradle.kts#L2). A cache miss build should
fail with an exception that says that a provider could not be written to the cache.

```shell
> ./gradlew brokenProvider
```

Compare this with Gradle 7.6, where serialization would complete with a problem and tasks would then run and fail
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

## Java serializable object that fail during serialization

You can also use the [`brokenJavaSerialization` task](broken-types/build.gradle.kts#L26) to see the behaviour for Java serialization failures:

```shell
> ./gradlew brokenJavaSerialization
```

Compare this with Gradle 7.6, where the cause of the problem is less clear:

```shell
> gradle76 brokenJavaSerialization
```

## Groovy closures

After reviewing the behavior for serializing Groovy closures, we decided to leave the behaviour unchanged for the stable
serialization contract.

In short, the dynamic nature of Groovy makes it very difficult to know whether the body of a closure will
dynamically use any of the implicit state that is automatically captured by a closure - specifically the
`delegate`, `owner` and `this` properties of a closure. For closures defined in Groovy DSL build scripts or in
plugins implemented in dynamic Groovy - the vast majority of closures to be serialized - these properties almost always
reference values that are not supported, for example the `Project` instance. Serializing these values will lead to 
unsupported types being reported regardless of whether the values are used or not.

Given this, we decided to continue to discard this implicit state when serializing and require build authors to refactor
their Groovy closures to extract local variables that explicitly reference whatever state from the enclosing scopes
that the closure requires.

We also improved error reporting to help discover these problems earlier, in a cache miss build. This was demonstrated
in the repository for the [load-after-store feature](https://github.com/adammurdoch/configuration-cache-load-after-store)

## Support for convention mapping

After reviewing the behavior for handling convention mappings during serialization, we decided to leave the
behaviour unchanged for the stable serialization contract.

Convention mappings are a very old internal feature that has leaked into many plugins. They have been replaced by
the public `Property` and `Provider` APIs. The configuration cache serialization currently provides an "80% solution"
that works fine for most usages of convention mappings. For edge cases that don't work with this solution, we intend to
encourage plugin authors to use the `Property` and `Provider` APIs instead of convention mappings. We also have options
to support more usages in a backwards compatible way, as required after stable configuration caching.
