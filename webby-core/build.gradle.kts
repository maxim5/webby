plugins {
    id("java")
    id("java-library")
    id("java-test-fixtures")
}

// Prod
dependencies {
    api(project(":base-util"))
    api(project(":orm-api"))
    api(project(":orm-generator"))
    api(project(":webby-routekit"))

    // API
    api("com.google.guava:guava:33.2.0-jre")
    api("com.google.inject:guice:7.0.0")
    api("com.google.inject.extensions:guice-assistedinject:7.0.0")
    api("com.google.mug:mug:7.2")
    api("io.netty:netty-all:4.1.110.Final")
    api("com.carrotsearch:hppc:0.10.0")

    implementation("org.jetbrains:annotations:24.1.0")
}

configurations {
    compileOnly.get().extendsFrom(common.get())
}

// Testing
dependencies {
    testFixturesApi(testFixtures(project(":base-util")))

    // JUnit, Truth, Mockito
    testFixturesApi("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testFixturesApi("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testFixturesApi("com.google.truth:truth:1.4.2")
    testFixturesApi("com.mockrunner:mockrunner-jdbc:2.0.7")
    testFixturesApi("org.mockito:mockito-core:5.12.0")
    // Utilities for Unit tests
    testFixturesApi("org.jetbrains:annotations:24.1.0")
    testFixturesApi("com.google.code.gson:gson:2.10.1")
    testFixturesApi("com.google.flogger:flogger-log4j2-backend:0.8")
    testFixturesApi("com.squareup.okhttp3:okhttp:4.12.0")
    testFixturesApi("com.squareup.okio:okio:3.9.0")
    // SQLs for Unit tests
    testFixturesApi("com.h2database:h2:1.4.200")
    testFixturesApi("com.zaxxer:HikariCP:5.1.0")
    testFixturesApi("com.mysql:mysql-connector-j:8.4.0")
    testFixturesApi("org.xerial:sqlite-jdbc:3.46.0.0")
    testFixturesApi("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    testFixturesApi("ch.vorburger.mariaDB4j:mariaDB4j:3.1.0")
    // NoSQLs for Integration tests
    testFixturesApi("io.etcd:jetcd-test:0.8.0")
    testFixturesApi("it.ozimov:embedded-redis:0.7.3")

    // Core tests.
    testImplementation("com.google.jimfs:jimfs:1.3.0")
    testImplementation("com.j256.simplemagic:simplemagic:1.17")
    testImplementation("eu.medsea.mimeutil:mime-util:2.1.3")
    testImplementation("org.jodd:jodd-json:6.0.3")
}
