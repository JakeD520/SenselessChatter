package com.rivvr.app.ui.macros

/**
 * Chat macro system for ephemeral visual effects
 * Macros are processed client-side from message text
 */

// Basic chat macros with emoji replacements
enum class ChatMacro(
    val trigger: String,
    val emoji: String,
    val effectType: MacroEffectType
) {
    LOVE("<love>", "💖", MacroEffectType.HEART_BURST),
    LOL("<lol>", "😂", MacroEffectType.LAUGH_RAIN),
    FIRE("<fire>", "🔥", MacroEffectType.FIRE_EXPLOSION),
    PARTY("<party>", "🎉", MacroEffectType.CELEBRATION)
}

// Types of visual effects
enum class MacroEffectType {
    HEART_BURST,
    LAUGH_RAIN, 
    FIRE_EXPLOSION,
    CELEBRATION,
    SCREEN_SHAKE // For future earthquake macro
}

// Processed message with macros replaced and effects identified
data class ProcessedMessage(
    val originalText: String,
    val displayText: String, // Text with macros replaced by emojis
    val effects: List<MacroEffectType> = emptyList(),
    val shouldTriggerEffects: Boolean = effects.isNotEmpty()
)

/**
 * Process a message to detect macros and prepare effects
 */
fun processMessageMacros(messageText: String): ProcessedMessage {
    var processedText = messageText
    val detectedEffects = mutableListOf<MacroEffectType>()
    
    // Check each macro
    ChatMacro.values().forEach { macro ->
        if (messageText.contains(macro.trigger, ignoreCase = true)) {
            // Replace macro with emoji
            processedText = processedText.replace(
                macro.trigger, 
                macro.emoji, 
                ignoreCase = true
            )
            
            // Add effect to trigger
            detectedEffects.add(macro.effectType)
        }
    }
    
    return ProcessedMessage(
        originalText = messageText,
        displayText = processedText,
        effects = detectedEffects.distinct(), // Remove duplicates
        shouldTriggerEffects = detectedEffects.isNotEmpty()
    )
}

/**
 * Check if a message contains any macros (quick check)
 */
fun containsMacros(messageText: String): Boolean {
    return ChatMacro.values().any { macro ->
        messageText.contains(macro.trigger, ignoreCase = true)
    }
}