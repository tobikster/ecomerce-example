import dev.detekt.gradle.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.spring) apply false
	alias(libs.plugins.spring.boot) apply false
	alias(libs.plugins.spring.dependencyManagement) apply false
	alias(libs.plugins.kotlin.jpa) apply false

	alias(libs.plugins.detekt) apply false
	alias(libs.plugins.ktlint) apply false
}

subprojects {
	pluginManager.withPlugin(
		rootProject.libs.plugins.ktlint
			.get()
			.pluginId,
	) {
		configure<KtlintExtension> {
			android = false
		}
	}

	pluginManager.withPlugin(
		rootProject.libs.plugins.detekt
			.get()
			.pluginId,
	) {
		configure<DetektExtension> {
			buildUponDefaultConfig = true
		}
	}

	tasks.withType<Test>().configureEach {
		useJUnitPlatform()
	}
}
