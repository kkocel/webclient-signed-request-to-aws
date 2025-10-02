import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

val awsSdkVersion = "2.34.8"

plugins {
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.osdetector") version "1.7.3"
    val kotlinVersion = "2.2.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
}

group = "com.sample"
version = "0.0.1-SNAPSHOT"

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    if (osdetector.arch.equals("aarch_64")) {
        implementation("io.netty:netty-all")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation(platform("software.amazon.awssdk:bom:$awsSdkVersion"))
    implementation("software.amazon.awssdk:auth")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
