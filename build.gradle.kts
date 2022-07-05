/*
 * Copyright 2021 Comcast Cable Communications Management, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.6.3"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.6.10"
	kotlin("plugin.spring") version "1.6.10"
}

group = "com.comcast.vrex.demo.sdk"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
	mavenLocal()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.comcast.vrex.sdk:vrex-speech-sdk-core-jvm:1.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.2")
	implementation("com.squareup.okio:okio-jvm:3.0.0")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.13.2")
	implementation("io.ktor:ktor-client-websockets-jvm:1.6.7")
	implementation("io.ktor:ktor-client-core-jvm:1.6.7")
	implementation("io.ktor:ktor-client-cio-jvm:1.6.7")
	implementation("com.squareup.okhttp3:okhttp:4.9.3")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.6.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "11"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
