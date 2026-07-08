import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.neoforged.moddev")
	kotlin("jvm")
}

val modId = providers.gradleProperty("mod_id").get()

base { archivesName.set("${providers.gradleProperty("archives_name").get()}-neoforge") }

evaluationDependsOn(":common")
val commonMain = project(":common").extensions.getByType<SourceSetContainer>()["main"]

neoForge {
	version = providers.gradleProperty("neoforge_version").get()

	runs {
		register("server") {
			server()
		}
	}

	mods {
		register(modId) {
			sourceSet(sourceSets.main.get())
		}
	}
}

dependencies {
	// Kotlin runtime + @Mod language adapter for NeoForge.
	implementation("thedarkcolour:kotlinforforge-neoforge:${providers.gradleProperty("kotlin_for_forge_version").get()}")

	// Shared code from :common (same Mojang mappings as NeoForge runtime).
	compileOnly(commonMain.output)
}

tasks.named<Jar>("jar") {
	from(commonMain.output)
}

tasks.named<ProcessResources>("processResources") {
	val props = mapOf("version" to project.version)
	inputs.properties(props)
	filesMatching("META-INF/neoforge.mods.toml") { expand(props) }
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach { options.release.set(21) }

kotlin {
	compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
}
