plugins {
    kotlin("jvm")
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val sourcesJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().kotlin)
    archiveClassifier.set("sources")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val asmVersion = "9.2"

dependencies {
    implementation(gradleApi())
    implementation("org.javassist:javassist:3.20.0-GA")
    implementation("commons-io:commons-io:2.11.0")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation("commons-codec:commons-codec:1.15")
    implementation("com.android.tools.build:gradle:4.2.1")
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-analysis:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    implementation("org.ow2.asm:asm-tree:$asmVersion")
    implementation("org.ow2.asm:asm-util:$asmVersion")
}

val GROUP = "com.ca.router_gradle_plugin"
val VERSION = "1.0.0.0"

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(sourcesJar)
                artifactId = "injector"
                groupId = GROUP
                version = VERSION
            }
        }
        repositories {
            mavenLocal()
            maven {
                url = uri("./router_gradle_plugin")
            }
        }
    }
}
