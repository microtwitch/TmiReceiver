plugins {
    id("org.springframework.boot") version "2.6.6"
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    id("org.sonarqube") version "3.3"
}

group "de.com.fdm"
version "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter:2.6.7")
    implementation("com.github.twitch4j:twitch4j:1.9.0")
    implementation("org.redisson:redisson:3.17.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

sonarqube {
    properties {
        property("sonar.projectKey", "microtwitch_dukebot")
        property("sonar.organization", "microtwitch")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}
