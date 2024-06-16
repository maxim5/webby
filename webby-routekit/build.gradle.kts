plugins {
    id("java")
}

version = "0.3.0"

// Include exported util sources
tasks.compileJava {
    dependsOn(":base-util:exportSrc")
}
sourceSets {
    main.get().java.srcDirs(listOf("${project(":base-util").extra["exportDir"]}/arrays"))
}

dependencies {
    implementation(project(":base-util"))
    testImplementation(testFixtures(project(":base-util")))

    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("org.openjdk.jmh:jmh-core:1.37")
    testImplementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}
