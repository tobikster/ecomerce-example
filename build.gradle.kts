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
description = "order-service"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.web)
	implementation(libs.kotlin.reflect)
	implementation(libs.jackson.kotlin)

	implementation(libs.spring.boot.starter.jpa)
	implementation(libs.spring.boot.starter.flyway)
	implementation(libs.flyway.postgresql)
	runtimeOnly(libs.postgresql)

	testImplementation(libs.spring.boot.starter.web.test)
	testImplementation(libs.kotlin.junit5)
	testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

ktlint {
	android = false
}

detekt {
	buildUponDefaultConfig = true
}

tasks.withType<Test> {
	useJUnitPlatform()
}
