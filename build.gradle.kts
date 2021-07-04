import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
val kspVersion: String by project

plugins {
    kotlin("jvm")
}

group = "me.admqueiroga"
version = "0.9.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}