import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom-remap")
	kotlin("jvm")
}

val modId = providers.gradleProperty("mod_id").get()

base { archivesName.set("${providers.gradleProperty("archives_name").get()}-fabric") }

evaluationDependsOn(":common")
val commonMain = project(":common").extensions.getByType<SourceSetContainer>()["main"]

loom {
	mods {
		register(modId) {
			sourceSet(sourceSets.main.get())
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	mappings(loom.officialMojangMappings())
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("fabric_loader_version").get()}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
	modImplementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")

	// Shared code from :common (compiled against Mojang mappings, remapped on build).
	compileOnly(commonMain.output)
}

tasks.named<Jar>("jar") {
	from(commonMain.output)
}

tasks.named<ProcessResources>("processResources") {
	val props = mapOf("version" to project.version)
	inputs.properties(props)
	filesMatching("fabric.mod.json") { expand(props) }
}

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach { options.release.set(21) }

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}
