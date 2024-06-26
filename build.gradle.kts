import org.apache.tools.ant.taskdefs.condition.Os

// https://stackoverflow.com/questions/40696738/define-path-to-gradle-wrapper
tasks.wrapper {
    gradleVersion = "8.8"
    jarFile = file("${project.projectDir}/.infra/gradle-wrapper.jar")
    scriptFile = file("${project.projectDir}/.infra/gradlew")
}

plugins {
    idea
    java
}

allprojects {
    apply(plugin = "idea")

    group = "io.spbx.webby"
    version = "0.11.0"

    idea {
        module {
            outputDir = file("build/idea/main")
            testOutputDir = file("build/idea/test")
            isDownloadJavadoc = false
            isDownloadSources = true
        }
    }
}

subprojects {
    apply(plugin = "java")

    repositories {
        mavenCentral()
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:24.1.0")
        testCompileOnly("org.jetbrains:annotations:24.1.0")

        compileOnly("com.google.flogger:flogger:0.8")
        runtimeOnly("com.google.flogger:flogger-log4j2-backend:0.8")

        // Silence SLF4J: https://stackoverflow.com/questions/66386000/how-to-disable-slf4j-in-jetty
        runtimeOnly("org.slf4j:slf4j-nop:2.0.13")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
        testCompileOnly("org.junit.jupiter:junit-jupiter-params:5.10.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    }

    tasks.test {
        useJUnitPlatform()
    }
}

subprojects {
    repositories {
        maven("https://jitpack.io")
        maven("https://raw.githubusercontent.com/bulldog2011/bulldog-repo/master/repo/releases/")
    }

    configurations {
        create("common")
    }

    dependencies {
        // Google Flogger
        "common"("com.google.flogger:flogger:0.8")
        "common"("com.google.flogger:flogger-log4j2-backend:0.8")

        "common"("com.google.protobuf:protobuf-java:4.26.1")
        "common"("com.leansoft:bigqueue:0.7.3")
        "common"("org.jctools:jctools-core:4.0.5")

        // Json
        "common"("com.alibaba:fastjson:2.0.51")
        "common"("com.dslplatform:dsl-json-java8:1.10.0")
        "common"("com.fasterxml.jackson.core:jackson-databind:2.17.0")
        "common"("com.google.code.gson:gson:2.10.1")
        "common"("com.squareup.moshi:moshi:1.15.1")
        "common"("org.jodd:jodd-json:6.0.3")

        // Templates
        "common"("com.fizzed:rocker-runtime:1.4.0")
        "common"("com.github.jknack:handlebars:4.4.0")
        "common"("com.github.spullara.mustache.java:compiler:0.9.11")
        "common"("com.samskivert:jmustache:1.15")
        "common"("gg.jte:jte:3.1.12")
        "common"("io.pebbletemplates:pebble:3.2.2")
        "common"("org.apache.velocity:velocity:1.7")
        "common"("org.freemarker:freemarker:2.3.32")
        "common"("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
        "common"("org.trimou:trimou-core:2.5.1.Final")

        // Embedded Key-Values
        "common"("com.github.cojen:Tupl:1.5.3.1")
        "common"("com.github.yahoo:HaloDB:v0.5.6")
        "common"("com.linkedin.paldb:paldb:1.2.0")
        "common"("com.yahoo.oak:oak:0.2.3.1")
        "common"("io.swaydb:java_2.13:0.16.2")
        "common"("net.openhft:chronicle-map:3.25ea6")
        "common"("org.deephacks.lmdbjni:lmdbjni:0.4.6")
        "common"("org.deephacks.lmdbjni:lmdbjni-$detectOsForLevelDbJni:0.4.6")
        "common"("org.fusesource.leveldbjni:leveldbjni:1.8")
        "common"("org.fusesource.leveldbjni:leveldbjni-$detectOsForLevelDbJni:1.8")
        "common"("org.iq80.leveldb:leveldb:0.12")
        "common"("org.lmdbjava:lmdbjava:0.8.3")
        "common"("org.mapdb:mapdb:3.1.0")
        "common"("org.rocksdb:rocksdbjni:9.2.1")

        // External NoSQLs
        "common"("io.etcd:jetcd-core:0.7.6")
        "common"("redis.clients:jedis:5.1.3")

        // SQLs
        "common"("com.h2database:h2:1.4.200")
        "common"("com.zaxxer:HikariCP:5.1.0")
        "common"("mysql:mysql-connector-java:8.0.32")
        "common"("org.xerial:sqlite-jdbc:3.46.0.0")
        "common"("org.mariadb.jdbc:mariadb-java-client:3.4.0")

        // MimeType utils
        "common"("com.j256.simplemagic:simplemagic:1.17")
        "common"("eu.medsea.mimeutil:mime-util:2.1.3")
    }
}

private val detectOsForLevelDbJni get() = when {
    Os.isFamily(Os.FAMILY_UNIX) -> "linux64"
    Os.isFamily(Os.FAMILY_MAC) -> "osx64"
    Os.isFamily(Os.FAMILY_WINDOWS) -> "win64"
    else -> ""
}
