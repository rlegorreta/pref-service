import org.gradle.internal.classpath.Instrumented.systemProperty
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.ir.backend.js.compile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage

plugins {
	id("org.springframework.boot") version "3.1.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
}

group = "com.ailegorreta"
version = "2.0.0"
description = "Server repository for Neo4j IAM"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://repo.spring.io/snapshot") }

	maven {
		name = "GitHubPackages"
		url = uri("https://maven.pkg.github.com/" +
		project.findProperty("registryPackageUrl") as String? ?:
			System.getenv("URL_PACKAGE") ?:
			"rlegorreta/ailegorreta-kit")
		credentials {
			username = project.findProperty("registryUsername") as String? ?:
					System.getenv("USERNAME") ?:
					"rlegorreta"
			password = project.findProperty("registryToken") as String? ?: System.getenv("TOKEN")
		}
	}
}

extra["springCloudVersion"] = "2022.0.3"
extra["testcontainersVersion"] = "1.17.3"
extra["otelVersion"] = "1.26.0"
extra["ailegorreta-kit-version"] = "2.0.0"

dependencies {
	implementation("org.springframework.cloud:spring-cloud-starter-config")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	// ^ do not use spring-boot-starter-security because we use reactive resource server. Instead, use starter-webflux
	// see: https://stackoverflow.com/questions/76217964/the-bean-springsecurityfilterchain-defined-in-class-path-resource-could-not-be

	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	implementation("org.springframework.boot:spring-boot-starter-data-neo4j") // this includeas reactive version

	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client") {
		exclude(group = "org.springframework.cloud", module = "spring-cloud-starter-ribbon")
		exclude(group = "com.netflix.ribbon", module = "ribbon-eureka")
	}
	// ^ This library work just for docker container. Kubernetes ignores it (setting eureka.client.registerWithEureka
	// property to false

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	// implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")		// Reactive version
	// implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")       // Reactive version
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8")

	implementation("com.ailegorreta:ailegorreta-kit-commons-utils:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-data-neo4j:${property("ailegorreta-kit-version")}")
	implementation("com.ailegorreta:ailegorreta-kit-resource-server-security:${property("ailegorreta-kit-version")}")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
	testImplementation("org.springframework.security:spring-security-test")
	// testImplementation("io.projectreactor:reactor-test")				// this is for web-flux testing
	testImplementation("com.squareup.okhttp3:mockwebserver")

	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("org.testcontainers:neo4j:1.17.6")
	testImplementation("org.neo4j.driver:neo4j-java-driver:5.6.0")
	// ^ see hint in https://java.testcontainers.org/modules/databases/neo4j/
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
		mavenBom("org.testcontainers:testcontainers-bom:${property("testcontainersVersion")}")
	}
}

tasks.named<BootBuildImage>("bootBuildImage") {
	environment.set(environment.get() + mapOf("BP_JVM_VERSION" to "17.*"))
	imageName.set("ailegorreta/${project.name}")
	docker {
		publishRegistry {
			username.set(project.findProperty("registryUsername").toString())
			password.set(project.findProperty("registryToken").toString())
			url.set(project.findProperty("registryUrl").toString())
		}
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

configure<SourceSetContainer> {
	named("main") {
		java.srcDir("src/main/kotlin")
	}
}