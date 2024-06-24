plugins {
    id("java")
    id("java-test-fixtures")
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")
    compileOnly("org.checkerframework:checker-qual:3.44.0")
    compileOnly("com.google.guava:guava:33.2.0-jre")
    implementation("com.carrotsearch:hppc:0.10.0")      // testing EasyHppc
    implementation("io.netty:netty-all:4.1.110.Final")  // testing EasyByteBuf
}

dependencies {
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("com.google.inject:guice:7.0.0")  // testing generics
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

dependencies {
    testFixturesCompileOnly("com.google.flogger:flogger:0.8")
    testFixturesCompileOnly("com.google.truth:truth:1.4.2")
    testFixturesCompileOnly("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testFixturesCompileOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    testFixturesCompileOnly("org.jetbrains:annotations:24.1.0")
    testFixturesCompileOnly("com.google.inject:guice:7.0.0")
    testFixturesCompileOnly("com.carrotsearch:hppc:0.10.0")
    testFixturesCompileOnly("io.netty:netty-all:4.1.110.Final")
    testFixturesCompileOnly("net.bytebuddy:byte-buddy:1.14.17")
    testFixturesCompileOnly("net.bytebuddy:byte-buddy-agent:1.14.17")
}

// Exports

project.extra["exportDir"] = "${project.layout.buildDirectory.get()}/exported/sources"
val exports = mapOf("*CharArray*.java" to "arrays")  // file-pattern -> directory

tasks.register<Copy>("exportSrc") {
    includeEmptyDirs = false
    exports.forEach { (files, dest) ->
        // https://stackoverflow.com/questions/40984658/using-a-wildcard-in-gradle-copy-task
        from(sourceSets.main.get().java.srcDirs)
        include("**/${files}")
        into("${project.extra["exportDir"]}/${dest}")
    }
    dependsOn(tasks.compileJava.get())
}
