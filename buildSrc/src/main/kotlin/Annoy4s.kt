import com.sun.jna.Platform
import com.sun.jna.Platform.*
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import java.io.File

val assembleReleaseTaskName: String
    get() {
        val osName = when (getOSType()) {
            WINDOWS,
            WINDOWSCE ->
                "Windows"

            LINUX ->
                "Linux"

            Platform.MAC ->
                "Mac"

            else -> error("not support os type")
        }
        val version = if (is64Bit()) {
            "X86-64"
        } else {
            "X86"
        }
        return "assembleRelease${osName}${version}"
    }

val TaskContainer.assembleRelease: Task get() = this.getByName(assembleReleaseTaskName)

val Project.libraryReleaseDir: Pair<File, String>
    get() {
        val (osName, suffix) = when (getOSType()) {
            WINDOWS,
            WINDOWSCE ->
                "windows" to ".dll"

            LINUX ->
                "linux" to ".so"

            Platform.MAC ->
                "mac" to ".dylib"

            else -> error("not support os type")
        }
        val version = if (is64Bit()) {
            "x86-64"
        } else {
            "x86"
        }
        return buildDir.resolve("lib").resolve("main").resolve("release").resolve(osName).resolve(version) to suffix
    }

fun Project.copyLibraryInto(from: Project, target: File) {
    copy {
        val (libraryReleaseDir, suffix) = from.libraryReleaseDir
        from(libraryReleaseDir)
        into(target)
        include {
            includeEmptyDirs = false
            if (it.isDirectory) {
                true
            } else {
                it.name.endsWith(suffix)
            }
        }
    }
}

fun Project.copyLibrary(from: Project) {
    copyLibraryInto(from, buildDir.resolve("generated").resolve("resources").resolve(RESOURCE_PREFIX))
}