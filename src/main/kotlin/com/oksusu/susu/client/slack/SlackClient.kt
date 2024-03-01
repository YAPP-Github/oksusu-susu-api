package com.oksusu.susu.client.slack

import com.oksusu.susu.client.slack.model.SlackMessageModel

interface SlackClient {
    suspend fun sendSummary(message: SlackMessageModel): String
}
