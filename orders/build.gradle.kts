import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.1"
	id("io.spring.dependency-management") version "1.1.4"
	id ("org.jetbrains.kotlin.kapt") version "2.0.0-Beta1"
	kotlin("jvm") version "1.9.21"
	kotlin("plugin.spring") version "1.9.21"
	kotlin("plugin.jpa") version "1.9.21"

	id("com.palantir.docker") version "0.33.0"
	id("com.palantir.docker-run") version "0.33.0"


}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.apache.httpcomponents:httpclient:4.5.14")
	implementation("org.projectlombok:lombok:1.18.20")

	implementation("org.mapstruct:mapstruct:1.5.5.Final")
	kapt ("org.mapstruct:mapstruct-processor:1.5.5.Final")

	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("io.jsonwebtoken:jjwt-api:0.12.3")
	implementation ("io.jsonwebtoken:jjwt-impl:0.12.3")
	implementation ("io.jsonwebtoken:jjwt-jackson:0.12.3")

	//runtimeOnly "com.h2database:h2"
	runtimeOnly ("org.postgresql:postgresql")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.security:spring-security-test")

	//logging
	implementation ("io.github.microutils:kotlin-logging-jvm:2.0.11")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
}

docker{
	name = "${project.name}:${project.version}"

}

dockerRun {
	name = "${project.name}"
	image = "${project.name}:${project.version}"
	ports("8080:8080")
	clean = true
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

