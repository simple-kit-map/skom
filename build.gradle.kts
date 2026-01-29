plugins {
    java
    idea
    id("com.gradleup.shadow") version "8.3.0"
    id("org.graalvm.buildtools.native") version "0.11.1"
//    kotlin("jvm")
}
println("Gradle uses Java ${org.gradle.internal.jvm.Jvm.current()}")
group = "cx.ctt.skom"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://repo.smolder.cloud/public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.slf4j:slf4j-api:2.0.17")
    implementation("ch.qos.logback:logback-classic:1.5.23")

    implementation("redis.clients:jedis:7.2.0")

    implementation("net.minestom:minestom:2025.12.20-1.21.11")
    implementation("fr.ghostrider584:axiom-minestom:0.0.3")
//    implementation(project(":minestom-mechanics-parent:minestom-mechanics-lib"))
//    implementation("com.minestom:minestom-mechanics-lib:1.0-SNAPSHOT")
//    implementation(":minestom-mechanics-parent:minestom-mechanics-lib")
    implementation(files(
        "${projectDir}/../minestom-mechanics-lib-maven/minestom-mechanics-lib/target/minestom-mechanics-lib-1.0-SNAPSHOT.jar"))


//    implementation("com.github.TogAr2:MinestomPvP:56a831b41c")
    implementation(files("${projectDir}/../MinestomPvP/build/libs/MinestomPvP.jar"))
//    implementation(project(":MinestomPvP"))
//    implementation(project(":VanillaReimplementation"))
//    implementation("com.github.Minestom:VanillaReimplementation:-SNAPSHOT")
//    implementation("com.github.Minestom:VanillaReimplementation:a79a599b27")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "cx.ctt.skom.Main"
        }
    }

    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        mergeServiceFiles()
        archiveClassifier.set("") // prevent the -all suffix on the shadowjar file.
    }

    named("generateResourcesConfigFile") {
        dependsOn(shadowJar)
    }
}
graalvmNative {
    binaries {
        named("main") {
            imageName.set("marathon")
            mainClass.set("cx.ctt.skom.Main")

//            buildArgs.add("-march=native")
            buildArgs.add("-Os")
            buildArgs.add("-march=x86-64-v3")
            quickBuild.set(true)
            buildArgs.add("--enable-url-protocols=https")
            buildArgs.add("--gc=G1")
            javaLauncher.set(javaToolchains.launcherFor {
                languageVersion.set(JavaLanguageVersion.of(25))
            })

            verbose.set(true)
            fallback.set(false)
            quickBuild.set(true)
        }

        all {
            resources.autodetect()
        }
    }
}
