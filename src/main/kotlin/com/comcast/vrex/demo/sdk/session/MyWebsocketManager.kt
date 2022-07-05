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
package com.comcast.vrex.demo.sdk.session

import com.comcast.vrex.demo.sdk.util.client
import com.comcast.vrex.sdk.auth.SatAuthenticator
import com.comcast.vrex.sdk.message.common.EventMessage
import com.comcast.vrex.sdk.session.SpeechSession
import com.comcast.vrex.sdk.session.WebSocketManager
import com.comcast.vrex.sdk.util.SpeechConstants
import io.ktor.client.features.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import okio.BufferedSource
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

/**
 * MyWebsocketManager extends WebSocketManager
 */
class MyWebsocketManager(session: SpeechSession, satAuthenticator: SatAuthenticator) : WebSocketManager(
    session,
    satAuthenticator
) {

    lateinit var bufferedSource: BufferedSource
    lateinit var wsSession: DefaultClientWebSocketSession

    // here we invoke the connection to the speech server. Use any websocket library to make the connection
    override fun connect() {
        runBlocking {
            launch(Dispatchers.Default) {
                val speechConfig = session.speechConfig
                var url = Url(speechConfig.speechUrl)
                client.webSocket( //using ktor library to make the WS connection
                    method = HttpMethod.Get,
                    host = url.host,
                    path = "${url.encodedPath}$wsUrlPath",
                    request = {
                        header( // sending token in the header
                            "Authorization",
                            "${satAuthenticator.satResponse?.tokenType} ${satAuthenticator.satResponse?.token}"
                        )
                    }
                ) {
                    wsSession = this
                    handleOnConnect() //if connection is successful...
                }
            }
        }
    }

    override fun handleOnConnect() {
        super.handleOnConnect()
        runBlocking {
            launch(Dispatchers.Default) {
                handleMessages() // handle incoming messages in a new thread
            }
        }
    }

    private suspend fun handleMessages() {
        wsSession.send(getInitMessage()) // sending init as soon as we connect
        try {
            while (true) { //loop that listens to incoming messages
                val newMsg: Frame.Text = wsSession.incoming.receive() as Frame.Text
                handleIncomingMessage(newMsg.readText()) //delegating handling to a function in parent class
            }
        } catch (e: ClosedReceiveChannelException) {
            //Do Nothing
        } catch (e: Exception) {
            throw e
        }
    }


    //after we send init, we receive a listening message from the server
    override fun handleListening(eventMessage: EventMessage) {
        super.handleListening(eventMessage)
        sendStreamingAudio()
    }

    private fun sendStreamingAudio() {
        runBlocking { //sending streaming audio in a new thread
            launch(Dispatchers.IO) {
                while (!bufferedSource.exhausted()) {
                    val bufferSize = min(SpeechConstants.STANDARD_BUFFER_SIZE, bufferedSource.buffer.size)
                    wsSession.send(bufferedSource.readByteArray(bufferSize))
                }
                sendEndOfAudio() // sending end of audio message
            }
        }
    }

    private suspend fun sendEndOfAudio() {
        val endOfStreamMessageStr: String = getEndOfAudio()
        wsSession.send(endOfStreamMessageStr)
    }
}