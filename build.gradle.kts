plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.6.10"
}

group = "io.github.edmondantes"
version = "0.1.0"

java.sourceCompatibility = JavaVersion.VERSION_11
java.targetCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.slf4j:slf4j-api:1.7.36")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    javadoc {
        if (JavaVersion.current().isJava9Compatible) {
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri((extra["maven.repository.url"] as String?).orEmpty().ifEmpty { "./build/repo" })
        }
    }
    publications {
        create<MavenPublication>("maven") {
            pom {
                name.set("Simple kotlin callbacks")
                description.set("A small library for better use callbacks experience in kotlin language")
                url.set("https://github.com/EdmonDantes/simple-kotlin-callbacks")
                developers {
                    developer {
                        id.set((extra["maven.developer.id"] as String?).orEmpty())
                        name.set("Ilia Loginov")
                        email.set("dantes2104@gmail.com")
                        url.set("https://github.com/EdmonDantes")
                    }
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/EdmonDantes/simple-kotlin-callbacks.git")
                    developerConnection.set("scm:git:ssh://github.com:EdmonDantes/simple-kotlin-callbacks.git")
                    url.set("https://github.com/EdmonDantes/simple-kotlin-callbacks/tree/master")
                }
            }

            groupId = "io.github.edmondantes"
            artifactId = "simple-kotlin-callbacks"
            version = "0.1.0"

            from(components["java"])
        }
    }
}