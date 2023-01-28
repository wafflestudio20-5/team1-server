import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.21"
	kotlin("plugin.spring") version "1.7.21"
	kotlin("plugin.jpa") version "1.7.21"
	kotlin("plugin.allopen") version "1.3.71"
	kotlin("plugin.noarg") version "1.3.71"

	kotlin("kapt") version "1.7.10"

}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
}

noArg {
	annotation("jakarta.persistence.Entity")
}

group = "com.wafflytime"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// db
	runtimeOnly("mysql:mysql-connector-java")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.vladmihalcea:hibernate-types-60:2.20.0")
	// Swagger
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")

	// Local Auth
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
	implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// OAuth
	implementation("org.springframework.boot:spring-boot-starter-webflux")

	// s3
	implementation("org.springframework.cloud:spring-cloud-starter-aws:2.0.1.RELEASE")

	// QueryDSL
	val querydslVersion = "5.0.0"
	implementation("com.querydsl:querydsl-jpa:$querydslVersion:jakarta")
	implementation("com.querydsl:querydsl-core:$querydslVersion")
	kapt("com.querydsl:querydsl-apt:$querydslVersion:jakarta")
	kapt(group = "com.querydsl", name = "querydsl-apt", classifier = "jpa")

	// json
//	implementation("com.squareup.retrofit2:converter-gson:2.7.1")
	implementation("org.json:json:20211205")

	// coroutines
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")

	// redis
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	// websocket
	implementation("org.springframework.boot:spring-boot-starter-websocket")
}

// QueryDSL
sourceSets {
	named("main") {
		java.srcDir("$buildDir/generated/source/kapt/main")
	}
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.getByName<Jar>("jar") {
	enabled = false
}

allOpen {
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}