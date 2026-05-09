package com.adars.aishim.provider

import com.adars.aishim.model.ModelParams
import com.adars.aishim.model.ProviderResult
import com.adars.aishim.provider.anthropic.AnthropicOcrService
import com.adars.aishim.provider.anthropic.AnthropicTextService
import com.adars.aishim.provider.azure.AzureOpenAiOcrService
import com.adars.aishim.provider.azure.AzureOpenAiTextService
import com.adars.aishim.provider.deepseek.DeepSeekTextService
import com.adars.aishim.provider.gemini.GeminiOcrService
import com.adars.aishim.provider.gemini.GeminiTextService
import com.adars.aishim.provider.openai.OpenAiOcrService
import com.adars.aishim.provider.openai.OpenAiTextService
import dev.langchain4j.data.message.ChatMessage
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

/**
 * Dispatches to the correct provider bean based on [AiProvider].
 *
 * Ollama support is currently disabled (dependency commented out in pom.xml).
 * To enable: uncomment the Ollama dependency in pom.xml, re-enable the Ollama service files,
 * and add the Ollama cases back to this resolver.
 */
@ApplicationScoped
class ProviderResolver @Inject constructor(
    private val openAiOcr: OpenAiOcrService,
    private val openAiText: OpenAiTextService,
    private val geminiOcr: GeminiOcrService,
    private val geminiText: GeminiTextService,
    private val deepSeekText: DeepSeekTextService,
    private val anthropicOcr: AnthropicOcrService,
    private val anthropicText: AnthropicTextService,
    private val azureOpenAiOcr: AzureOpenAiOcrService,
    private val azureOpenAiText: AzureOpenAiTextService,
) {
    @org.eclipse.microprofile.config.inject.ConfigProperty(name = "aishim.ollama.base-url", defaultValue = "http://localhost:11434/v1")
    lateinit var ollamaBaseUrl: String

    /**
     * Returns params with [knownUrl] injected as base_url if the caller didn't already supply one.
     * This lets the caller override the URL while named providers still work with zero config.
     */
    private fun withBaseUrl(params: ModelParams?, knownUrl: String): ModelParams =
        (params ?: ModelParams()).let { if (it.baseUrl != null) it else it.copy(baseUrl = knownUrl) }

    /**
     * Returns a function (prompt, imageBase64, mimeType) -> ProviderResult for vision-capable providers.
     * [systemPrompt] and [params] apply to the request; nulls use configured defaults.
     */
    fun visionFunction(
        provider: AiProvider,
        systemPrompt: String?,
        params: ModelParams?,
        apiKey: String? = null,
    ): (prompt: String, imageBase64: String, mimeType: String) -> ProviderResult {
        if (provider == AiProvider.OPENAI_COMPATIBLE && params?.baseUrl.isNullOrBlank()) {
            throw IllegalArgumentException("OPENAI_COMPATIBLE provider requires base_url in model_params.")
        }
        OpenAiCompatibleRegistry.providers[provider]?.let { meta ->
            if (!meta.supportsVision) {
                throw UnsupportedOperationException(
                    "${provider.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }} does not support vision/image input."
                )
            }
            return { prompt, b64, mime ->
                openAiOcr.invokeVision(prompt, b64, mime, systemPrompt, withBaseUrl(params, meta.baseUrl), apiKey)
            }
        }
        return when (provider) {
            AiProvider.OPENAI -> { prompt, b64, mime -> openAiOcr.invokeVision(prompt, b64, mime, systemPrompt, params, apiKey) }
            AiProvider.GEMINI -> { prompt, b64, mime -> geminiOcr.invokeVision(prompt, b64, mime, systemPrompt, params, apiKey) }
            AiProvider.ANTHROPIC -> { prompt, b64, mime -> anthropicOcr.invokeVision(prompt, b64, mime, systemPrompt, params, apiKey) }
            AiProvider.AZURE_OPENAI -> { prompt, b64, mime -> azureOpenAiOcr.invokeVision(prompt, b64, mime, systemPrompt, params, apiKey) }
            AiProvider.OPENAI_COMPATIBLE -> { prompt, b64, mime -> openAiOcr.invokeVision(prompt, b64, mime, systemPrompt, params, apiKey) }
            AiProvider.OLLAMA -> { prompt, b64, mime -> openAiOcr.invokeVision(prompt, b64, mime, systemPrompt, withBaseUrl(params, ollamaBaseUrl), apiKey ?: "ollama") }
            AiProvider.DEEPSEEK -> throw UnsupportedOperationException(
                "DeepSeek does not support vision/image input. Use OPENAI, GEMINI, or ANTHROPIC."
            )
            else -> throw UnsupportedOperationException(
                "${provider.name.lowercase()} does not support vision/image input."
            )
        }
    }

    /**
     * Returns a function (messages) -> ProviderResult for text-only providers.
     * [params] and [apiKey] apply to the request; nulls use configured defaults.
     * The [messages] list should already contain the system message (if any) as the first element.
     */
    fun textFunction(
        provider: AiProvider,
        params: ModelParams?,
        apiKey: String? = null,
    ): (messages: List<ChatMessage>) -> ProviderResult {
        if (provider == AiProvider.OPENAI_COMPATIBLE && params?.baseUrl.isNullOrBlank()) {
            throw IllegalArgumentException("OPENAI_COMPATIBLE provider requires base_url in model_params.")
        }
        OpenAiCompatibleRegistry.providers[provider]?.let { meta ->
            return { msgs -> openAiText.chat(msgs, withBaseUrl(params, meta.baseUrl), apiKey) }
        }
        return when (provider) {
            AiProvider.OPENAI -> { msgs -> openAiText.chat(msgs, params, apiKey) }
            AiProvider.GEMINI -> { msgs -> geminiText.chat(msgs, params, apiKey) }
            AiProvider.DEEPSEEK -> { msgs -> deepSeekText.chat(msgs, params, apiKey) }
            AiProvider.ANTHROPIC -> { msgs -> anthropicText.chat(msgs, params, apiKey) }
            AiProvider.AZURE_OPENAI -> { msgs -> azureOpenAiText.chat(msgs, params, apiKey) }
            AiProvider.OPENAI_COMPATIBLE -> { msgs -> openAiText.chat(msgs, params, apiKey) }
            AiProvider.OLLAMA -> { msgs -> openAiText.chat(msgs, withBaseUrl(params, ollamaBaseUrl), apiKey ?: "ollama") }
            else -> throw IllegalStateException("Unhandled provider routing for $provider")
        }
    }
}
