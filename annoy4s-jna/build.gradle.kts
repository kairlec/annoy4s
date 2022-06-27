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

val target = project(":annoy")
tasks.create("copyLibrary"){
    tasks.processResources{
        this.mustRunAfter(this@create)
    }
    dependsOn(target.tasks.getByName("assembleRelease"))
    copyLibrary(from = target)
}

java {
    sourceSets {
        main {
            resources.srcDir(buildDir.resolve("generated").resolve("resources"))
        }
    }
}