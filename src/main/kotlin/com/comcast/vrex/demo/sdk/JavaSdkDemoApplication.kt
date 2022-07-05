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
package com.comcast.vrex.demo.sdk

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import kotlin.system.exitProcess

@SpringBootApplication
class Ja2vaSdkDemoApplication : CommandLineRunner {
    override fun run(vararg args: String?) {
        println("\t\t*********************************************")
        println("\t\t*  VREX-SPEECH-SDK - stream from file demo  *")
        println("\t\t*********************************************\n")

        runDemo()
        exitProcess(0)
    }
}

fun main(args: Array<String>) {
    runApplication<JavaSdkDemoApplication>(*args)
}
