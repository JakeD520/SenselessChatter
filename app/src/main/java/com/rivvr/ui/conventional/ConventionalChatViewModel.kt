package com.rivvr.ui.conventional

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rivvr.data.conventional.ConventionalChatRepository
import com.rivvr.data.conventional.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Standard Compose patterns - following consultant report
class ConventionalChatViewModel(
    private val repository: ConventionalChatRepository
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadMessages()
        subscribeToMessages()
        // Auto-refresh every 10 seconds as interim solution
        startAutoRefresh()
    }
    
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10_000) // Wait 10 seconds
                try {
                    repository.refreshMessages() // Silent refresh
                } catch (e: Exception) {
                    // Silently ignore refresh errors
                }
            }
        }
    }
    
    fun updateMessageText(text: String) {
        _messageText.value = text.take(200) // Enforce character limit
    }
    
    fun sendMessage() {
        val text = _messageText.value.trim()
        if (text.isEmpty()) return
        
        viewModelScope.launch {
            try {
                repository.sendMessage(text)
                _messageText.value = "" // Clear input after sending
                _error.value = null // Clear any previous errors
            } catch (e: Exception) {
                _error.value = "Failed to send message: ${e.message}"
            }
        }
    }
    
    private fun loadMessages() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val loadedMessages = repository.getMessages()
                _messages.value = loadedMessages
            } catch (e: Exception) {
                _error.value = "Failed to load messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun subscribeToMessages() {
        viewModelScope.launch {
            try {
                repository.subscribeToMessages()
                
                // Listen for message refresh events
                repository.messagesRefresh.collect { refreshedMessages ->
                    _messages.value = refreshedMessages
                }
            } catch (e: Exception) {
                _error.value = "Failed to setup message updates: ${e.message}"
            }
        }
    }
    
    // Pull-to-refresh functionality
    fun refreshMessages() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.refreshMessages()
            } catch (e: Exception) {
                _error.value = "Failed to refresh messages: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}