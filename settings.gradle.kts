@file:Suppress("UnstableApiUsage")

rootProject.name = "ecommerce-system"

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		mavenCentral()
	}
}

include(":order-service")
