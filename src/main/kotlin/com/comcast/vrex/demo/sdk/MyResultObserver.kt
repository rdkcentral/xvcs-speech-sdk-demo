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

import com.comcast.vrex.sdk.message.receive.CloseConnection
import com.comcast.vrex.sdk.message.receive.VrexResponse
import com.comcast.vrex.sdk.session.SpeechResultObserver


class MyResultObserver : SpeechResultObserver {
    override fun onListening() {
        println("RESULT OBSERVER: Server is listening for audio")
    }

    override fun onPartialTranscriptionReceived(s: String) {
        println("RESULT OBSERVER: Partial transcription received: $s")
    }

    override fun onFinalTranscriptionReceived(s: String) {
        println("RESULT OBSERVER: Final transcription received: $s")
    }

    override fun onCloseConnection(vrexResponse: CloseConnection) {
        println("RESULT OBSERVER: Server has closed the connection: $vrexResponse")
    }

    override fun onConnect() {
        println("RESULT OBSERVER: Successfully connected to the Speech server")
    }

    override fun onWakeUpWordVerificationSuccess(confidence: Int) {
        println("RESULT OBSERVER: received WUW PASSED verification message")
    }

    override fun onWakeUpWordVerificationFailure(confidence: Int) {
        println("RESULT OBSERVER: received WUW FAILED verification message")
    }

    override fun onFinalResponseSuccess(response: VrexResponse) {
        println("RESULT OBSERVER: received successful final message: $response")
    }

    override fun onFinalResponseFailure(response: VrexResponse) {
        println("RESULT OBSERVER: received unsuccessful final message: $response")
    }

    override fun onError(t: Throwable) {
        println("ERROR: $t")
    }
}