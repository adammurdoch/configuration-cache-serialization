
tasks.register("brokenProvider") {
    val provider = provider {
        throw RuntimeException("this provider is broken")
    }
    doLast {
        println("value = ${provider.get()}")
    }
}

tasks.register("brokenFileCollection") {
    val files = files({
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
    val bean = BrokenBean()
    doLast {
        println("bean = $bean")
    }
}
