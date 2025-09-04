package com.rivvr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthed: () -> Unit,
    signIn: suspend (String, String) -> Result<Unit>,
    signUp: suspend (String, String) -> Result<Unit>
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Rivvr Login", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = email, 
                onValueChange = { email = it }, 
                label = { Text("Email") }, 
                singleLine = true, 
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = password, 
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                enabled = !isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row {
                Button(
                    onClick = {
                        if (!isLoading && email.trim().isNotEmpty() && password.isNotEmpty()) {
                            scope.launch {
                                try {
                                    isLoading = true
                                    error = null
                                    val result = signIn(email.trim(), password)
                                    if (result.isSuccess) {
                                        onAuthed()
                                    } else {
                                        error = result.exceptionOrNull()?.message ?: "Sign in failed"
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "An error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.trim().isNotEmpty() && password.isNotEmpty()
                ) { 
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Sign In") 
                    }
                }
                
                Spacer(Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = {
                        if (!isLoading && email.trim().isNotEmpty() && password.isNotEmpty()) {
                            scope.launch {
                                try {
                                    isLoading = true
                                    error = null
                                    val result = signUp(email.trim(), password)
                                    if (result.isSuccess) {
                                        onAuthed()
                                    } else {
                                        error = result.exceptionOrNull()?.message ?: "Sign up failed"
                                    }
                                } catch (e: Exception) {
                                    error = e.message ?: "An error occurred"
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    enabled = !isLoading && email.trim().isNotEmpty() && password.isNotEmpty()
                ) { 
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Sign Up") 
                    }
                }
            }
            
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
