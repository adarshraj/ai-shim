package com.adars.aishim.provider

/**
 * Single source of truth for OpenAI-compatible provider metadata.
 *
 * Keeping base URLs and optional server-side API-key env vars here avoids repeating
 * the same constants across resolver, model listing, and health checks.
 */
data class OpenAiCompatibleProviderMeta(
    val baseUrl: String,
    val apiKeyEnvVar: String,
    val supportsVision: Boolean,
)

object OpenAiCompatibleRegistry {
    val providers: Map<AiProvider, OpenAiCompatibleProviderMeta> = mapOf(
        AiProvider.GROQ to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.groq.com/openai/v1",
            apiKeyEnvVar = "GROQ_API_KEY",
            supportsVision = true,
        ),
        AiProvider.OPENROUTER to OpenAiCompatibleProviderMeta(
            baseUrl = "https://openrouter.ai/api/v1",
            apiKeyEnvVar = "OPENROUTER_API_KEY",
            supportsVision = true,
        ),
        AiProvider.MISTRAL to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.mistral.ai/v1",
            apiKeyEnvVar = "MISTRAL_API_KEY",
            supportsVision = true,
        ),
        AiProvider.CEREBRAS to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.cerebras.ai/v1",
            apiKeyEnvVar = "CEREBRAS_API_KEY",
            supportsVision = false,
        ),
        AiProvider.XAI to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.x.ai/v1",
            apiKeyEnvVar = "XAI_API_KEY",
            supportsVision = false,
        ),
        AiProvider.COHERE to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.cohere.com/compatibility/v1",
            apiKeyEnvVar = "COHERE_API_KEY",
            supportsVision = false,
        ),
        AiProvider.ZAI to OpenAiCompatibleProviderMeta(
            baseUrl = "https://open.bigmodel.cn/api/paas/v4",
            apiKeyEnvVar = "ZAI_API_KEY",
            supportsVision = true,
        ),
        AiProvider.GITHUB_MODELS to OpenAiCompatibleProviderMeta(
            baseUrl = "https://models.inference.ai.azure.com",
            apiKeyEnvVar = "GITHUB_MODELS_API_KEY",
            supportsVision = true,
        ),
        AiProvider.NVIDIA_NIM to OpenAiCompatibleProviderMeta(
            baseUrl = "https://integrate.api.nvidia.com/v1",
            apiKeyEnvVar = "NVIDIA_NIM_API_KEY",
            supportsVision = true,
        ),
        AiProvider.OVHCLOUD_AI_ENDPOINTS to OpenAiCompatibleProviderMeta(
            baseUrl = "https://oai.endpoints.kepler.ai.cloud.ovh.net/v1",
            apiKeyEnvVar = "OVHCLOUD_AI_ENDPOINTS_API_KEY",
            supportsVision = true,
        ),
        AiProvider.LLM7 to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.llm7.io/v1",
            apiKeyEnvVar = "LLM7_API_KEY",
            supportsVision = true,
        ),
        AiProvider.SILICONFLOW to OpenAiCompatibleProviderMeta(
            baseUrl = "https://api.siliconflow.cn/v1",
            apiKeyEnvVar = "SILICONFLOW_API_KEY",
            supportsVision = true,
        ),
    )
}
