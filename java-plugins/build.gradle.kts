plugins {
    id("java-gradle-plugin")
}

gradlePlugin {
    plugins {
        create("javaLambdas") {
            id = "test.java"
            implementationClass = "test.JavaPlugin"
        }
    }
}