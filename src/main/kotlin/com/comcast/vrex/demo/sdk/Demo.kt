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

import com.comcast.vrex.demo.sdk.auth.MySatAuthenticator
import com.comcast.vrex.demo.sdk.session.MyWebsocketManager
import com.comcast.vrex.demo.sdk.util.*
import com.comcast.vrex.sdk.message.send.InitPayload
import com.comcast.vrex.sdk.message.send.TriggeredBy
import com.comcast.vrex.sdk.session.SpeechResultObserver
import com.comcast.vrex.sdk.session.SpeechSession
import com.comcast.vrex.sdk.util.SpeechConstants
import com.comcast.vrex.sdk.util.generateTrx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.math.min

/**
 * Demo starts here
 */
fun runDemo() {

    val triggeredBy = selectTrigger() // user menu handling

    //Create Init payload
    val initPayload: InitPayload

    //Here we select a user selected file name and an appropriate predefined init payload based on trigger type.
    val fileName: String
    if (triggeredBy == TriggeredBy.WUW) {
        val hfFile = selectHfFile()
        initPayload = getHfPayload(hfFile.start, hfFile.end)
        fileName = hfFile.fileName
    } else {
        fileName = selectPttFile()
        initPayload = getPttPayload()
    }

    // These configurations are defined in .properties files under resources for this demo
    val speechConfig = getSpeechConfig()
    val authConfig = getAuthConfig()

    val audioInputStream = openAudioFile(fileName, triggeredBy)

    // Custom result observer used by WebsocketManager
    val observer: SpeechResultObserver = MyResultObserver()

    // Implement a custom authenticator. See 'MySatAuthenticator' class.
    // Note that 'MySatAuthenticator' extends 'SatAuthenticator' abstract class from the SDK
    val satAuthenticator = MySatAuthenticator(authConfig)
    satAuthenticator.updateToken() // Implement a custom authenticator,
    satAuthenticator.scheduleTokenFetching() //schedule token fetching based on a time interval

    // Speech session holds metadata required for a single websocket call
    val speechSession = SpeechSession(generateTrx(), speechConfig, initPayload, observer)

    // These are Java only classes used for steaming audio through the websocket connection
    val pipedOutputStream = PipedOutputStream()
    val pipedInputStream = PipedInputStream(pipedOutputStream)

    // Implement a custom websocket manager. See 'MyWebsocketManager' class.
    // Note that 'MyWebsocketManager' extends 'WebSocketManager' abstract class from the SDK
    val wsManager = MyWebsocketManager(speechSession, satAuthenticator)

    wsManager.bufferedSource = pipedInputStream.source().buffer()

    runBlocking {
        // reading from audio file in a separate thread in order to stream it
        launch(Dispatchers.IO) {
            val src = audioInputStream.source().buffer();
            try {
                while (!src.exhausted()) {
                    delay(80) // delay to simulate streaming
                    val bufferSize = min(SpeechConstants.STANDARD_BUFFER_SIZE, src.buffer.size)
                    pipedOutputStream.write(src.readByteArray(bufferSize))
                    pipedOutputStream.flush()
                }
                pipedOutputStream.close()
            } catch (e: Exception) {
                println(e)
            }
        }

        // in another thread we handle the websocket connection
        launch(Dispatchers.Default) {
            wsManager.connect()
        }
    }
}