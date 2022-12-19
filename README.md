# Configuration Cache Serialization Contract

This repository contains some samples that demonstrate the changes to serialization in Gradle 8.0 and 8.1.

## Java lambdas

A plugin can now use arbitrary lambdas in task state and these are serialized and deserialized correctly.
In previous versions, a plugin could only use lambdas for a small number of Gradle specific functional interfaces
(for example `TaskAction`).

To try this out, run the [`javaLamba` task](java-plugins/src/main/java/test/JavaPlugin.java)

```shell
> ./gradlew javaLambda
> ./gradlew javaLambda
```

Compare this with the behavior with Gradle 7.6. The lambda is serialized in the cache miss build but cannot be
deserialized in the cache hit build:

```shell
> alias gradle76=# a Gradle 7.6 installation or any recent 7.x version
> gradle76 javaLambda
# Fails with class not found exception
> gradle76 javaLambda
```

Lambdas that capture unsupported values:

```shell
> ./gradlew brokenJavaLambda
```

```shell
# Succeeds but should fail
> gradle76 brokenJavaLambda
# Fails with class cast exception
> gradle76 brokenJavaLambda
```

## Providers that fail during serialization

```shell
> ./gradlew brokenProvider
```

```shell
# Duplicate exceptions reported
> gradle76 brokenProvider
```

Consistent with file collections that fail during serialization.

```shell
> ./gradlew brokenFileCollection
> gradle76 brokenFileCollection
```

## Java serializable object that fail during serialization

```shell
> ./gradlew brokenJavaSerialization
```

```shell
> gradle76 brokenJavaSerialization
```

## Groovy closures

No changes

## Support for convention mapping

No changes
