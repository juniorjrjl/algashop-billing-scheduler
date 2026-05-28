plugins {
	java
	idea
	jacoco
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
}

group = "com.algaworks.algashop"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

val mockitoAgent = configurations.create("mockitoAgent")
configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {

	annotationProcessor(libs.lombok)
	annotationProcessor(libs.lombok.mapstruct.binding)
	annotationProcessor(libs.mapstruct.processor)

	compileOnly(libs.lombok)

	implementation(libs.mapstruct)
	implementation(libs.spring.boot.starter.jdbc)
	implementation(libs.spring.boot.starter.validation)
	implementation(libs.spring.boot.starter.webmvc)

	mockitoAgent(libs.mockito.core) { isTransitive = false }

	runtimeOnly(libs.postgresql)

	testAnnotationProcessor(libs.lombok)

	testCompileOnly(libs.lombok)

	testImplementation(libs.assertj.core)
	testImplementation(libs.datafaker)
	testImplementation(libs.rest.assured)
	testImplementation(libs.rest.assured.spring.mock.mvc)
	testImplementation(libs.spring.boot.starter.webmvc.test)

	testRuntimeOnly(libs.junit.platform.launcher)

}

dependencyManagement {
	imports {
		mavenBom(libs.spring.cloud.dependencies.get().toString())
	}
}

tasks.withType<Test>().configureEach {
	jvmArgs(
		"-javaagent:${configurations.getByName("mockitoAgent").asPath}"
	)
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs("-javaagent:${mockitoAgent.asPath}")
	finalizedBy(tasks.jacocoTestReport)
	systemProperty("test.seed", System.getProperty("test.seed") ?: "")
}

tasks.register<Test>("integrationTest"){
	description = "Run unit tests."
	group = "verification"

	jvmArgs("-javaagent:${mockitoAgent.asPath}")

	testClassesDirs = tasks.test.get().testClassesDirs
	classpath = tasks.test.get().classpath

	useJUnitPlatform{
		includeTags("IntegrationTest")
	}
	systemProperty("test.seed", System.getProperty("test.seed") ?: "")
}

tasks.register<Test>("unitTest"){
	description = "Run unit tests."
	group = "verification"

	jvmArgs("-javaagent:${mockitoAgent.asPath}")

	testClassesDirs = tasks.test.get().testClassesDirs
	classpath = tasks.test.get().classpath

	useJUnitPlatform{
		includeTags("UnitTest")
	}
	systemProperty("test.seed", System.getProperty("test.seed") ?: "")
}

tasks.jacocoTestReport {
	reports {
		xml.required = false
		csv.required = false
		html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
	}
}

tasks.bootJar {
	archiveFileName.set("billing-scheduler.jar")
}

tasks.register<Exec>("dockerbuild"){
	description = "Builds a multi-platform Docker image using Buildx"
	group = "build"

	dependsOn("bootJar")

	workingDir = project.rootDir

	commandLine(
		"docker",
		"buildx",
		"build",
		"--platform",
		"linux/arm64/v8,linux/amd64",
		"--tag",
		"algashop/billing-scheduler:dev",
		"."
	)
}
