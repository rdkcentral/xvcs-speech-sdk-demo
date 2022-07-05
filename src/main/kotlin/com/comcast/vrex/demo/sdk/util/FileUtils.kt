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
package com.comcast.vrex.demo.sdk.util

import com.comcast.vrex.sdk.config.AuthConfig
import com.comcast.vrex.sdk.config.SpeechConfig
import com.comcast.vrex.sdk.message.send.TriggeredBy
import com.comcast.vrex.sdk.util.SpeechException
import java.io.BufferedInputStream
import java.util.*
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

fun openAudioFile(fileName: String, triggeredBy: TriggeredBy): AudioInputStream {
    val triggerPath = if (triggeredBy == TriggeredBy.WUW) "hf/" else "ptt/"
    return AudioSystem.getAudioInputStream(
        BufferedInputStream(
            object {}.javaClass.getResourceAsStream("/audio/$triggerPath$fileName.wav")
        )
    )
}

private fun readConfigFile(fileName: String): Properties {
    val properties = Properties()
    val stream = object {}.javaClass.getResourceAsStream("/$fileName")
    try {
        properties.load(stream)
    } catch (e: NullPointerException) {
        throw SpeechException("Unable to read $fileName")
    }
    return properties
}

fun getSpeechConfig(): SpeechConfig {
    val properties = readConfigFile("speech-config.properties")
    return SpeechConfig(
        speechUrl = properties["speechUrl"].toString(),
        appId = properties["appId"].toString(),
        deviceId = properties["deviceId"].toString(),
        accountId = properties["accountId"].toString(),
        customerId = properties["customerId"].toString(),
    )
}

fun getAuthConfig(): AuthConfig {
    val properties = readConfigFile("speech-secrets.properties")
    return AuthConfig(
        endpoint = properties["endpoint"].toString(),
        clientId = properties["clientId"].toString(),
        secret = properties["secret"].toString(),
        renewIntervalSeconds = (properties["renewIntervalSeconds"] as String).toLong(),
    )
}