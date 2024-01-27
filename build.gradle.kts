import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.9.21"

    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    idea

    /** ktlint **/
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"

    /** jacoco **/
    id("jacoco")

    /** sonarqube **/
    id("org.sonarqube") version "4.3.1.3277"
}

group = "com.oksusu"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}

/**
 * https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
 * automatically supported annotation
 * @Component, @Async, @Transactional, @Cacheable, @SpringBootTest,
 * @Configuration, @Controller, @RestController, @Service, @Repository.
 * jpa meta-annotations not automatically opened through the default settings of the plugin.spring
 */
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

springBoot.buildInfo { properties { } }

object DependencyVersion {
    const val QUERYDSL_VERSION = "5.0.0"
    const val ARROW_FX_VERSION = "1.1.5"
    const val JWT_VERSION = "4.1.0"
    const val SPRINGDOC_VERSION = "2.2.0"
    const val JAVADOC_SCRIBE_VERSION = "0.15.0"
    const val KOTLIN_LOGGING_VERSION = "6.0.3"
    const val LOGBACK_ENCODER_VERSION = "7.3"
    const val KOTEST_VERSION = "5.7.2"
    const val KOTEST_EXTENSION_VERSION = "1.1.2"
    const val MOCKK_VERSION = "1.13.4"
    const val FASTEXCEL_VERSION = "0.16.5"
    const val SPRING_CLOUD_AWS_VERSION = "3.1.0"
    const val AWS_SDK_VERSION = "1.12.238"
    const val AWS_SDK_V2_VERSION = "2.17.107"
}

dependencies {
    /** kotlin */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    /** spring starter */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    /** querydsl */
    implementation("com.querydsl:querydsl-jpa:${DependencyVersion.QUERYDSL_VERSION}:jakarta")
    kapt("com.querydsl:querydsl-apt:${DependencyVersion.QUERYDSL_VERSION}:jakarta")

    /** arrow-kt */
    implementation("io.arrow-kt:arrow-fx-coroutines:${DependencyVersion.ARROW_FX_VERSION}")
    implementation("io.arrow-kt:arrow-fx-stm:${DependencyVersion.ARROW_FX_VERSION}")

    /** jwt */
    implementation("com.auth0:java-jwt:${DependencyVersion.JWT_VERSION}")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${DependencyVersion.SPRINGDOC_VERSION}")
    runtimeOnly("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE_VERSION}")
    kapt("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE_VERSION}")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")

    /** logger */
    implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING_VERSION}")
    implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER_VERSION}")

    /** test **/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${DependencyVersion.MOCKK_VERSION}")

    /** kotest */
    testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST_VERSION}")
    testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST_VERSION}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION_VERSION}")

    /** thymeleaf */
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

    /** fastexcel */
    implementation("org.dhatim:fastexcel:${DependencyVersion.FASTEXCEL_VERSION}")

    /** aws v1 */
    implementation(platform("com.amazonaws:aws-java-sdk-bom:${DependencyVersion.AWS_SDK_VERSION}"))
    implementation("com.amazonaws:aws-java-sdk-sts")

    /** aws v2 */
    implementation(platform("software.amazon.awssdk:bom:${DependencyVersion.AWS_SDK_V2_VERSION}"))
    implementation("software.amazon.awssdk:sts")

    /** ssm */
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${DependencyVersion.SPRING_CLOUD_AWS_VERSION}"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")

    /** etc */
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

defaultTasks("bootRun")

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.5"
}

when {
    project.hasProperty("prod") -> {
        println("Profile: prod")
        apply {
            from("profile_prod.gradle")
        }
    }

    project.hasProperty("staging") -> {
        println("Profile: staging")
        apply {
            from("profile_staging.gradle")
        }
    }

    else -> {
        println("Profile: dev")
        apply {
            from("profile_dev.gradle")
        }
    }
}

val Project.isSnapshotVersion: Boolean get() = version.toString().endsWith("SNAPSHOT")

/** build시 ktlint 미적용 */
gradle.taskGraph.whenReady {
    if (hasTask(":build")) {
        allTasks.forEach { task ->
            if (task.name.contains("ktlint") || task.name.contains("Ktlint")) {
                task.enabled = false
            }
        }
    }
}

/** jacoco **/
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    /** when finished test-all */
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(File("$buildDir/reports/jacoco.xml"))
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/*Config*",
                        "**/*Dto*",
                        "**/*Request*",
                        "**/*Response*",
                        "**/*Interceptor*",
                        "**/*Exception*",
                        "**/Q*.class"
                    )
                }
            }
        )
    )
}

/** sonarqube **/
sonarqube {
    properties {
        property("sonar.projectKey", "YAPP-Github_23rd-Android-Team-1-BE")
        property("sonar.organization", "yapp-github")
        property("sonar.host.url", "https://sonarcloud.io")

        /** sonar additional settings */
        property("sonar.sources", "src")
        property("sonar.language", "Kotlin")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.test.inclusions", "**/*Test.java")
        property(
            "sonar.exclusions",
            "**/test/**, **/Q*.kt, **/*Doc*.kt, **/resources/** ,**/*Application*.kt , **/*Config*.kt, " +
                "**/*Dto*.kt, **/*Request*.kt, **/*Response*.kt ,**/*Exception*.kt ,**/*ErrorCode*.kt"
        )
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.java.binaries", "$buildDir/classes")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco.xml")
    }
}
