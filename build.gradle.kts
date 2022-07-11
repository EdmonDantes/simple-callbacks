plugins {
    signing
    `java-library`
    `maven-publish`
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.0"
}

group = "io.github.edmondantes"
version = "0.1.1"

java {
    withSourcesJar()
}

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
    register<Jar>("dokkaJar") {
        from(dokkaHtml)
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

signing {
    val keyId = extra["signing.keyId"] as String?
    val signingKey = extra["signing.privateKey"] as String?
    val password = extra["signing.password"] as String?

    useInMemoryPgpKeys(keyId, signingKey, password)

    sign(publishing.publications)
}


publishing {
    repositories {
        maven {
            url = uri((extra["maven.repository.publish.url"] as String?).orEmpty().ifEmpty { "./build/repo" })
            credentials {
                username = extra["maven.repository.publish.username"] as String?
                password = extra["maven.repository.publish.password"] as String?
            }
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
                        name.set("Ilia Loginov")
                        email.set("dantes2104@gmail.com")
                        organization.set("github")
                        organizationUrl.set("https://www.github.com")
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
            version = "0.1.1"

            from(components["java"])
            artifact(tasks["dokkaJar"])
        }
    }
}