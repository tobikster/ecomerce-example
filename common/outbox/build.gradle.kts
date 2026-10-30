plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependencyManagement)

	alias(libs.plugins.detekt)
	alias(libs.plugins.ktlint)
}

tasks.bootJar {
	enabled = false
}

tasks.jar {
	enabled = true
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

dependencies {
	implementation(libs.spring.boot.starter.jpa)
	implementation(libs.spring.boot.starter.kafka)

	testImplementation(platform(libs.testcontainers.bom))
	testImplementation(libs.testcontainers.junit)
	testImplementation(libs.testcontainers.postgresql)
	testImplementation(libs.testcontainers.kafka)
	testImplementation(libs.spring.kafka.test)
	testImplementation(libs.spring.boot.testcontainers)
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.kotest.assertions)
}
