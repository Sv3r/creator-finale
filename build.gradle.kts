plugins {
    id("java")
}

class Plugin {
    val name = property("plugin.name").toString()
    val version = property("plugin.version").toString()
    val author = property("plugin.author").toString()
    val group = property("plugin.group").toString()
}

class Dependency {
    operator fun get(name: String) = property("dep.$name").toString()
}

val plugin = Plugin()
val dep = Dependency()

group = plugin.group
version = "${plugin.version}+${dep["minecraft"]}"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:${dep["minecraft"]}-${dep["paper_api"]}")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.processResources {
    val properties = mapOf(
        "name" to plugin.name,
        "version" to plugin.version,
        "author" to plugin.author,
        "minecraft" to dep["minecraft"]
    )

    properties.forEach { prop ->
        inputs.property(prop.key, prop.value)
    }

    filesMatching("paper-plugin.yml") {
        expand(properties)
    }
}