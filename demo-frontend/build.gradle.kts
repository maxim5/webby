plugins {
    id("java")
    id("java-library")
    id("nu.studer.rocker") version "3.0.2"
}

dependencies {
    implementation(project(":demo-model"))
    implementation(project(":orm-api"))
}

configurations {
    implementation.get().extendsFrom(common.get())
}

// https://github.com/etiennestuder/gradle-rocker-plugin
rocker {
    version = "1.4.0"  // optional
    configurations {
        create("demo") {
            templateDir = file("$projectDir/src/main/resources/web/rocker")
            outputDir = file("$projectDir/build/generated/rocker/demo/java")
            optimize = true
        }
    }
}

sourceSets.main.get().java.srcDirs(listOf(rocker.configurations.named("demo").get().outputDir))
sourceSets.main.get().java.srcDirs(listOf("$projectDir/build/generated/sources/orm"))
