plugins {
    id("java-gradle-plugin")
    id("groovy")
}

gradlePlugin {
    plugins {
        create("groovyClosures") {
            id = "test.groovy"
            implementationClass = "test.groovy.GroovyPlugin"
        }
    }
}
