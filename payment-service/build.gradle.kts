plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependencyManagement)
	alias(libs.plugins.kotlin.jpa)

	alias(libs.plugins.detekt)
	alias(libs.plugins.ktlint)
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "payment-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

dependencies {
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.web)
	implementation(libs.kotlin.reflect)
	implementation(libs.jackson.kotlin)

	implementation(libs.spring.boot.starter.jpa)
	implementation(libs.spring.boot.starter.flyway)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.flyway.postgresql)
	runtimeOnly(libs.postgresql)

	implementation(libs.spring.boot.starter.kafka)

	implementation(libs.springdoc.openapi.webmvc.ui)

	testImplementation(platform(libs.testcontainers.bom))
	testImplementation(libs.testcontainers.junit)
	testImplementation(libs.testcontainers.postgresql)
	testImplementation(libs.testcontainers.kafka)
	testImplementation(libs.spring.kafka.test)
	testImplementation(libs.spring.boot.testcontainers)
	testImplementation(libs.spring.boot.starter.data.jpa.test)
	testImplementation(libs.spring.boot.starter.webmvc.test)
	testImplementation(libs.kotlin.junit5)
	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.mockito.kotlin)
	testRuntimeOnly(libs.junit.platform.launcher)
	testImplementation(libs.kotest.assertions)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll(
			"-Xjsr305=strict",
			"-Xannotation-default-target=param-property",
			"-Xconsistent-data-class-copy-visibility",
		)
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}
