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
package com.comcast.vrex.demo.sdk.auth

import com.comcast.vrex.sdk.auth.SatAuthenticator
import com.comcast.vrex.sdk.auth.SatResponse
import com.comcast.vrex.sdk.config.AuthConfig
import com.comcast.vrex.sdk.util.SpeechException
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class MySatAuthenticator(authConfig: AuthConfig) : SatAuthenticator(authConfig) {

    private var SAT_EXECUTOR: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var client: OkHttpClient = OkHttpClient()
    private var rwLock: ReadWriteLock = ReentrantReadWriteLock()
    private var readLock: Lock = rwLock.readLock()
    private var writeLock: Lock = rwLock.writeLock()

    private fun buildRequest(): Request? {
        return Request.Builder()
            .url(authConfig.endpoint)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Client-Id", authConfig.clientId)
            .addHeader("X-Client-Secret", authConfig.secret)
            .post(RequestBody.create(null, byteArrayOf()))
            .build()
    }

    // retrieve the auth response in a thread safe manner
    override fun getAuthResponse(): SatResponse? {
        readLock.lock()
        return try {
            satResponse
        } finally {
            readLock.unlock()
        }
    }

    // schedule the process of fetching a new token to prevent usage of expired tokens
    fun scheduleTokenFetching() {
        val satRunnable = Runnable {
            try {
                updateToken()
            } catch (e: IOException) {
                throw SpeechException("SAT Error", e)
            }
        }
        val renewInterval: Long  = authConfig.renewIntervalSeconds
        val minRenewalInterval: Long = Math.min(renewInterval, satResponse?.expiresInSeconds ?: renewInterval)
        SAT_EXECUTOR.scheduleAtFixedRate(satRunnable, minRenewalInterval, minRenewalInterval, TimeUnit.SECONDS)
        println("Auth token fetching scheduled every $minRenewalInterval seconds")
    }

    override fun updateToken() {
        val request = buildRequest()
        val call = client.newCall(request!!)
        val response = call.execute()
        if (response.isSuccessful) {
            val responseBody = response.body
            val satResponse: SatResponse = Json.decodeFromStream(responseBody!!.byteStream())
            writeLock.lock()
            try {
                this.satResponse = satResponse // updating the sat response in a thread safe manner
                println("Auth token updated...")
            } finally {
                writeLock.unlock()
            }
        } else {
            throw SpeechException("SAT Response was not successful. response=$response")
        }
    }
}