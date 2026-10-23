@file:Suppress("UnstableApiUsage")

rootProject.name = "ecommerce-system"

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		mavenCentral()
	}
}

fun includeIfExists(vararg projectPaths: String) {
	projectPaths.forEach { projectPath ->
		val dirPath = projectPath.removePrefix(":").replace(":", File.separator)
		if (file(dirPath).isDirectory) {
			include(projectPath)
		}
	}
}

includeIfExists(":order-service")
includeIfExists(":payment-service")
