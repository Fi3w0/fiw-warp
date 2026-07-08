import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	kotlin("jvm")
}

base { archivesName.set("${providers.gradleProperty("archives_name").get()}-common") }

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	mappings(loom.officialMojangMappings())
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach { options.release.set(21) }

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}
