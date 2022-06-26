@file:Suppress("UnstableApiUsage")

plugins {
    `cpp-library`
}

library {
    linkage.set(listOf(Linkage.SHARED))

    source.from(file("src"))
    privateHeaders.from(file("src"))
    publicHeaders.from(file("include"))

    targetMachines.set(
        listOf(
            machines.windows.x86,
            machines.windows.x86_64,
            machines.macOS.x86_64,
            machines.linux.x86_64
        )
    )
}

tasks.withType<CppCompile>().configureEach {
    isOptimized = true
    isDebuggable = false
    macros["NDEBUG"] = null
    compilerArgs.addAll(toolChain.map { toolChain ->
        when (toolChain) {
            is Gcc, is Clang -> {
                listOf("-Os")
            }

            is VisualCpp -> listOf("/Os")
            else -> listOf()
        }
    })
}

afterEvaluate {
    tasks.create("assembleRelease").dependsOn(tasks.assembleRelease)
}