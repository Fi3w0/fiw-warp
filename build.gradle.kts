plugins {
	id("net.fabricmc.fabric-loom-remap") version "1.16-SNAPSHOT" apply false
	id("net.neoforged.moddev") version "2.0.141" apply false
	kotlin("jvm") version "2.4.0" apply false
}

allprojects {
	group = providers.gradleProperty("maven_group").get()
	version = providers.gradleProperty("mod_version").get()

	repositories {
		mavenCentral()
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
		maven("https://thedarkcolour.github.io/KotlinForForge/") { name = "KotlinForForge" }
	}
}
