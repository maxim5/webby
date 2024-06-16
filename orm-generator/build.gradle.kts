plugins {
    id("java")
    id("java-library")
    id("java-test-fixtures")
}

dependencies {
    api(project(":base-util"))
    api(project(":orm-api"))

    compileOnly("jakarta.inject:jakarta.inject-api:2.0.1.MR")
    implementation("com.google.guava:guava:33.2.0-jre")
    implementation("com.google.mug:mug:7.2")
}

dependencies {
    testImplementation("com.google.truth:truth:1.4.2")
}

dependencies {
    testFixturesApi(testFixtures(project(":orm-api")))
    testFixturesImplementation("org.jetbrains:annotations:24.1.0")
    testFixturesImplementation("com.google.guava:guava:33.2.0-jre")
}
