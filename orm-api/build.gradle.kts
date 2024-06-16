plugins {
    id("java")
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    api(project(":base-util"))

    compileOnly("com.google.errorprone:error_prone_annotations:2.28.0")
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("com.google.mug:mug:7.2")
    implementation("com.carrotsearch:hppc:0.10.0")
}

dependencies {
    testImplementation(testFixtures(project(":webby-core")))
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("com.mockrunner:mockrunner-jdbc:2.0.7")
}

dependencies {
    testFixturesApi(testFixtures(project(":base-util")))
    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.google.truth:truth:1.4.2")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testFixturesImplementation("com.mockrunner:mockrunner-jdbc:2.0.7")
}
