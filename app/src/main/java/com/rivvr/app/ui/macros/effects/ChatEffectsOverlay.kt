package com.rivvr.app.ui.macros.effects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.rivvr.app.ui.macros.MacroEffectType
import kotlinx.coroutines.delay

/**
 * Overlay system for displaying chat macro effects
 * Sits on top of chat UI without interfering with message flow
 */
@Composable
fun ChatEffectsOverlay(
    activeEffects: List<MacroEffectType>,
    modifier: Modifier = Modifier,
    onEffectComplete: (MacroEffectType) -> Unit = {}
) {
    // Debug logging
    LaunchedEffect(activeEffects) {
        if (activeEffects.isNotEmpty()) {
            println("🎆 ChatEffectsOverlay rendering ${activeEffects.size} effects: $activeEffects")
        }
    }
    
    // Try a popup-style approach that should definitely be visible
    if (activeEffects.isNotEmpty()) {
        androidx.compose.ui.window.Popup(
            alignment = Alignment.Center,
            onDismissRequest = { /* Don't dismiss automatically */ }
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                activeEffects.forEach { effect ->
                    when (effect) {
                        MacroEffectType.HEART_BURST -> {
                            HeartBurstAnimation(
                                onAnimationEnd = {
                                    onEffectComplete(MacroEffectType.HEART_BURST)
                                }
                            )
                        }
                MacroEffectType.LAUGH_RAIN -> {
                    // TODO: Implement laugh rain animation
                    LaunchedEffect(effect) {
                        delay(3000)
                        onEffectComplete(MacroEffectType.LAUGH_RAIN)
                    }
                }
                MacroEffectType.FIRE_EXPLOSION -> {
                    // TODO: Implement fire explosion animation  
                    LaunchedEffect(effect) {
                        delay(3000)
                        onEffectComplete(MacroEffectType.FIRE_EXPLOSION)
                    }
                }
                MacroEffectType.CELEBRATION -> {
                    // TODO: Implement celebration animation
                    LaunchedEffect(effect) {
                        delay(3000)
                        onEffectComplete(MacroEffectType.CELEBRATION)
                    }
                }
                MacroEffectType.SCREEN_SHAKE -> {
                    // TODO: Implement screen shake for earthquake macro
                    LaunchedEffect(effect) {
                        delay(2000)
                        onEffectComplete(MacroEffectType.SCREEN_SHAKE)
                    }
                }
            }
        }
            }
        }
    }
}

/**
 * State holder for managing active chat effects
 */
@Composable
fun rememberChatEffectsState(): ChatEffectsState {
    return remember { ChatEffectsState() }
}

class ChatEffectsState {
    private val _activeEffects = mutableStateListOf<MacroEffectType>()
    val activeEffects: List<MacroEffectType> = _activeEffects
    
    /**
     * Trigger a new effect
     */
    fun triggerEffect(effect: MacroEffectType) {
        // Prevent duplicate effects of the same type
        if (!_activeEffects.contains(effect)) {
            _activeEffects.add(effect)
        }
    }
    
    /**
     * Trigger multiple effects from a processed message
     */
    fun triggerEffects(effects: List<MacroEffectType>) {
        effects.forEach { effect ->
            triggerEffect(effect)
        }
    }
    
    /**
     * Remove an effect when animation completes
     */
    fun completeEffect(effect: MacroEffectType) {
        _activeEffects.remove(effect)
    }
    
    /**
     * Clear all active effects (useful for cleanup)
     */
    fun clearAllEffects() {
        _activeEffects.clear()
    }
    
    /**
     * Check if any effects are currently active
     */
    fun hasActiveEffects(): Boolean {
        return _activeEffects.isNotEmpty()
    }
}