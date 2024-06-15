plugins {
    id("java")
    id("java-test-fixtures")
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")
    compileOnly("org.checkerframework:checker-qual:3.44.0")
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("com.carrotsearch:hppc:0.10.0")
    implementation("io.netty:netty-all:4.1.110.Final")
}

dependencies {
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("com.google.inject:guice:7.0.0")  // testing generics
}

dependencies {
    testFixturesImplementation("com.google.truth:truth:1.4.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")

    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.google.inject:guice:7.0.0")
    testFixturesImplementation("com.carrotsearch:hppc:0.10.0")
    testFixturesImplementation("io.netty:netty-all:4.1.110.Final")
}

// Exports


extra["exportDir"] = "${project.layout.buildDirectory.get()}/exported/sources"
val exports = mapOf("*CharArray*.java" to "arrays")

tasks.register<Copy>("exportSrc") {
    includeEmptyDirs = false
    exports.forEach { (files, dest) ->
        // https://stackoverflow.com/questions/40984658/using-a-wildcard-in-gradle-copy-task
        from(sourceSets.main.get().java.srcDirs)
        include("**/${files}")
        into("${extra["exportDir"]}/${dest}")
    }
    dependsOn(tasks.compileJava.get())
}
