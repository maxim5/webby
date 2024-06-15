plugins {
    id("java")
    id("java-test-fixtures")
}

sourceSets {
    listOf("core", "demo", "tables").forEach {
        create(it) {
            java.srcDir("src/${this.name}/java")
            resources.srcDir("src/${this.name}/resources")
        }
    }
    // Drop default `java` sourceSets as they are not used.
    main.get().java.setSrcDirs(listOf<String>())
    test.get().java.setSrcDirs(listOf<String>())
}

// Useful:
// https://leanmind.es/en/blog/test-fixtures-with-gradle/
// https://remonsinnema.com/2016/05/09/how-to-manage-dependencies-in-a-gradle-multi-project-build/
// See also:
// https://newbedev.com/multi-project-test-dependencies-with-gradle
// https://stackoverflow.com/questions/32122363/gradle-sourceset-depends-on-another-sourceset
dependencies {
    sourceSets.named("core") {
        "coreImplementation"("com.volkhart.memory:measurer:0.1.1")
        // JMH
        "coreImplementation"("org.openjdk.jmh:jmh-core:1.37")
        "coreImplementation"("org.openjdk.jmh:jmh-generator-annprocess:1.37")
    }

    listOf("core", "demo", "tables").forEach {
        sourceSets.named(it) {
            "${this.name}Implementation"(project(":demo-model"))
            "${this.name}Implementation"(project(":demo-frontend"))
            "${this.name}Implementation"(testFixtures(project))
            "${this.name}Implementation"(testFixtures(project(":webby-core")))
            configurations["${this.name}Implementation"].extendsFrom(configurations.common.get())
        }
    }

    testFixturesImplementation(testFixtures(project(":webby-core")))
    testFixturesImplementation(project(":demo-frontend"))  // DevPaths
    configurations["testFixturesImplementation"].extendsFrom(configurations.common.get())
}
