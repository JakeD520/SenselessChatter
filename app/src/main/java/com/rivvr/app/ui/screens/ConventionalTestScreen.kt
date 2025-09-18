package com.rivvr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rivvr.app.data.SupabaseClientProvider
import com.rivvr.ui.conventional.ConventionalChatScreen
import com.rivvr.ui.conventional.ConventionalChatViewModel
import com.rivvr.data.conventional.ConventionalChatRepository

// Test screen to prove conventional approach works
@Composable
fun ConventionalTestScreen(
    modifier: Modifier = Modifier
) {
    // Create repository with real Supabase client
    val repository = remember {
        ConventionalChatRepository(SupabaseClientProvider.client)
    }
    
    // Create ViewModel with repository
    val viewModel = remember {
        ConventionalChatViewModel(repository)
    }
    
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🎯 CONVENTIONAL APPROACH TEST",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            "Real-time Supabase chat using native subscriptions",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Conventional chat implementation
        ConventionalChatScreen(viewModel = viewModel)
    }
}