plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0"
}

group = "cx.ctt.skom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.10.11-1.21.10")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25)) // Minestom has a minimum Java version of 25
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "cx.ctt.skom.Main" // Change this to your main class
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // Prevent the -all suffix on the shadowjar file.
    }
}

/*
dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
 */