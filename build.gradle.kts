plugins {
    kotlin("jvm") version "1.4.10"
}

repositories {
    jcenter()
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("junit:junit:4.12")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.0")
}
