
tasks.register("brokenProvider") {
    val provider = provider {
        // A provider whose value cannot be calculated
        throw RuntimeException("this provider is broken")
    }
    doLast {
        println("value = ${provider.get()}")
    }
}

tasks.register("brokenFileCollection") {
    val files = files({
        // A file collection whose value cannot be calculated
        throw RuntimeException("this file collection is broken")
    })
    doLast {
        println("files = ${files.files.map { it.name }}")
    }
}

class BrokenBean: java.io.Serializable {
    private fun writeObject(stream: java.io.ObjectOutputStream) {
        throw RuntimeException("this bean is broken")
    }
}

tasks.register("brokenJavaSerialization") {
    // A bean whose Java serialization is broken
    val bean = BrokenBean()
    doLast {
        println("bean = $bean")
    }
}
