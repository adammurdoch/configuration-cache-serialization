# Configuration Cache Serialization Contract

## Java lambdas

Arbitrary lambdas:

```shell
> ./gradlew javaLambda
> ./gradlew javaLambda
```

Lambdas that capture unsupported values:

```shell
> ./gradlew brokenJavaLambda
> ./gradlew brokenJavaLambda
```

## Groovy closures

TODO - this is not finished yet.

## Providers that fail during serialization

```shell
> ./gradlew brokenProvider
> ./gradlew brokenProvider
```

Consistent with file collections that fail during serialization.

```shell
> ./gradlew brokenFileCollection
```

## Java serializable object that fail during serialization

```shell
> ./gradlew brokenJavaSerialization
> ./gradlew brokenJavaSerialization
```

## Support for convention mapping

No changes
