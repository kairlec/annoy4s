plugins {
    kotlin("jvm") version "1.7.0"
}

dependencies {
    implementation("net.java.dev.jna:jna:5.11.0")
    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.3.1")
    testImplementation("io.kotest:kotest-assertions-core:5.3.1")
    testImplementation("io.kotest:kotest-property:5.3.1")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    val target = project(":annoy")
    dependsOn(target.tasks.getByName("assembleRelease"))
    doFirst {
        copyLibrary(from = target)
    }
}

java {
    sourceSets {
        main {
            resources.srcDir(buildDir.resolve("generated").resolve("resources"))
        }
    }
}