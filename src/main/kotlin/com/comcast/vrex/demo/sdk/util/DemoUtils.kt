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

import com.comcast.vrex.sdk.config.SpeechConfig
import com.comcast.vrex.sdk.message.common.LanguageType
import com.comcast.vrex.sdk.message.send.*
import com.comcast.vrex.sdk.message.send.Unit
import com.comcast.vrex.sdk.util.audioFromDefaultPttConfig
import io.ktor.client.*
import io.ktor.client.features.websocket.*
import java.io.BufferedInputStream
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

fun displayOptions(message: String?, options: List<String>): Int {
    var selectedOption: Int
    do {
        println(message)
        var i = 1
        options.forEach { fileName -> println(i++.toString() + ". " + fileName) }
        selectedOption = readln().toInt()
    } while (selectedOption < 1 || selectedOption > options.size)
    return selectedOption - 1
}

fun selectTrigger(): TriggeredBy {
    val selectedOption = displayOptions("Select a trigger:", listOf("Push To Talk", "Hands Free"))
    return if (selectedOption == 0) TriggeredBy.PTT else TriggeredBy.WUW
}

/***** PAYLOAD UTILS *****/

val testPayload = fromEmptyInitPayload()
    .withRoles(Roles.INPUT, Roles.ENVOY, Roles.AV, Roles.RENDER)
    .withCapabilities(Capability.WBW, Capability.UHD, Capability.TEST, Capability.FETCH_ARS)
    .withExecuteResponse()
    .withLanguage(LanguageType.ENG_USA)
    .withVrexModes(VrexMode.SR, VrexMode.NLP, VrexMode.AR, VrexMode.EXEC)
    .buildMessage()

fun getPttPayload(): InitPayload {
    return copyFromExistingInitPayload(testPayload)
        .withAudio(audioFromDefaultPttConfig())
        .buildMessage()
}

fun getHfPayload(start: Int, end: Int): InitPayload {
    val wakeUpWord: WakeUpWord = WakeUpWord(start, end, Unit.MS, null)
    val audio = Audio(
        audioProfile = "XR19",
        triggeredBy = TriggeredBy.WUW,
        wuw = wakeUpWord,
        envoyCodec = Codec.PCM_16_16K,
    )
    return copyFromExistingInitPayload(testPayload)
        .withAudio(audio)
        .buildMessage()
}

/***** AUDIO FILE UTILS *****/

fun selectPttFile(): String {
    val pttFileNames: List<String> = listOf("HBO", "show_me_all_kids_movies")
    val selectedFile = displayOptions("Select an audio file to stream:", pttFileNames)
    return pttFileNames[selectedFile]
}

fun selectHfFile(): HfFile {
    val hfFileNames: List<HfFile> = listOf(
        HfFile("hx-netflix", 100, 835),
        HfFile("hx-show-me-the-guide", 100, 790)
    )
    val fileNames: List<String> = hfFileNames.map { f -> f.fileName }
    val selectedFile = displayOptions("Select an audio file to stream:", fileNames)
    return hfFileNames[selectedFile]
}

data class HfFile(
    val fileName: String,
    val start: Int = 0,
    val end: Int = 0
)

val client = HttpClient() {
    install(WebSockets) {
    }
}