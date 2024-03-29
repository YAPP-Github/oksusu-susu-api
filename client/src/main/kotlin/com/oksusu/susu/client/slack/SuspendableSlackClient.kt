package com.oksusu.susu.client.slack

import com.oksusu.susu.client.config.SlackConfig
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.extension.withMDCContext
import kotlinx.coroutines.Dispatchers
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SuspendableSlackClient(
    private val webclient: WebClient,
    private val slackWebhookConfig: SlackConfig.SlackWebhookConfig,
) : SlackClient {
    override suspend fun sendSummary(message: SlackMessageModel): String {
        return withMDCContext(Dispatchers.IO) {
            webclient
                .post()
                .uri("/${slackWebhookConfig.summaryToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .awaitBody()
        }
    }
}
