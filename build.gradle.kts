import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "pw.aru"
version = "1.0-SNAPSHOT"

repositories {
    jcenter()
    maven {
        setUrl("https://dl.bintray.com/adriantodt/maven/")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("reflect"))
    testImplementation("org.json:json:20180813")
    testImplementation("pw.aru.hungergames:hg-loader:1.0")
    testImplementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.8")
    testImplementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = "2.9.8")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}