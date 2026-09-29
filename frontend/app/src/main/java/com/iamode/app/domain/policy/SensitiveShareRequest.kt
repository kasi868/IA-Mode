package com.iamode.app.domain.policy

/**
 * Detects requests that could disclose an identity or location. This intentionally uses a narrow,
 * explainable local rule rather than trusting an AI classification for a privacy decision.
 */
enum class SensitiveShareRequest { EMAIL, CURRENT_LOCATION, LIVE_LOCATION }

object SensitiveShareRequestDetector {
    fun detect(text: String): SensitiveShareRequest? {
        val normalized = text.lowercase().replace(Regex("\\s+"), " ")
        val asksEmail = Regex("\\b(email|e-mail|mail id|email id|professional mail|work email)\\b").containsMatchIn(normalized) &&
            Regex("\\b(send|share|give|what(?:'s| is)|provide|reach)\\b").containsMatchIn(normalized)
        if (asksEmail) return SensitiveShareRequest.EMAIL
        val asksLocation = Regex("\\b(location|where are you|where r you)\\b").containsMatchIn(normalized) &&
            Regex("\\b(send|share|give|current|live|pin|drop)\\b").containsMatchIn(normalized)
        if (!asksLocation) return null
        return if (Regex("\\blive\\b").containsMatchIn(normalized)) SensitiveShareRequest.LIVE_LOCATION
        else SensitiveShareRequest.CURRENT_LOCATION
    }
}
