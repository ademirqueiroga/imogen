plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":processor"))
    ksp(project(":processor"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
