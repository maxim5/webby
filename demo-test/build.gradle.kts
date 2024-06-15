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

val core = sourceSets.named("core").get()
val demo = sourceSets.named("demo").get()
val tables = sourceSets.named("tables").get()
val testFixtures = sourceSets.named("testFixtures").get()

// https://stackoverflow.com/questions/70396466/gradle-how-to-declare-a-dependency-to-a-specific-configuration-of-a-project-pro
private fun SourceSet.implementation(dependencyNotation: Any) =
    dependencies.add("${this.name}Implementation", dependencyNotation)

private val SourceSet.implementation
    get() = object {
        fun extendsFrom(superConfig: Configuration) =
            configurations["${name}Implementation"].extendsFrom(superConfig)
    }

// Useful:
// https://leanmind.es/en/blog/test-fixtures-with-gradle/
// https://remonsinnema.com/2016/05/09/how-to-manage-dependencies-in-a-gradle-multi-project-build/
// See also:
// https://newbedev.com/multi-project-test-dependencies-with-gradle
// https://stackoverflow.com/questions/32122363/gradle-sourceset-depends-on-another-sourceset
dependencies {
    core.implementation("com.volkhart.memory:measurer:0.1.1")
    core.implementation("org.openjdk.jmh:jmh-core:1.37")
    core.implementation("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    listOf(core, demo, tables).forEach {
        it.implementation(project(":demo-model"))
        it.implementation(project(":demo-frontend"))
        it.implementation(testFixtures(project))
        it.implementation(testFixtures(project(":webby-core")))
        it.implementation.extendsFrom(configurations.common.get())
    }

    testFixtures.implementation(testFixtures(project(":webby-core")))
    testFixtures.implementation(project(":demo-frontend"))  // DevPaths
    testFixtures.implementation.extendsFrom(configurations.common.get())
}
