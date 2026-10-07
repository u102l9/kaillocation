package com.kail.location.views.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kail.location.R
import com.kail.location.auth.AuthManager
import com.kail.location.network.RuoYiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 仅允许的登录邮箱域名：QQ 邮箱与谷歌邮箱。 */
private val ALLOWED_EMAIL_DOMAINS = listOf("qq.com", "gmail.com")

/** 校验邮箱是否属于允许的域名（QQ / Gmail），大小写与首尾空格不敏感。 */
private fun isAllowedEmailDomain(email: String): Boolean {
    val normalized = email.trim().lowercase()
    val at = normalized.lastIndexOf('@')
    if (at <= 0 || at == normalized.length - 1) return false
    val domain = normalized.substring(at + 1)
    return ALLOWED_EMAIL_DOMAINS.contains(domain)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit) {
    var mail by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSendingCode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val errEmptyFields = stringResource(R.string.login_error_empty_fields)
    val errEmptyEmail = stringResource(R.string.login_error_empty_email)
    val errInvalidDomain = stringResource(R.string.login_error_invalid_email_domain)
    val errSendCodeFailed = stringResource(R.string.register_error_send_code_failed)
    val errUnknown = stringResource(R.string.login_error_unknown)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.login_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_screen_title),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_hint_text),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text(stringResource(R.string.login_email_hint)) },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text(stringResource(R.string.register_verification_code_hint)) },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                Button(
                    onClick = {
                        if (mail.isBlank()) {
                            errorMessage = errEmptyEmail
                            return@Button
                        }
                        if (!isAllowedEmailDomain(mail)) {
                            errorMessage = errInvalidDomain
                            return@Button
                        }

                        isSendingCode = true
                        errorMessage = null

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.IO) {
                                RuoYiClient.sendMailCode(mail, scene = 1)
                            }

                            result.fold(
                                onSuccess = { isSendingCode = false },
                                onFailure = { error ->
                                    isSendingCode = false
                                    errorMessage = error.message ?: errSendCodeFailed
                                }
                            )
                        }
                    },
                    enabled = !isLoading && !isSendingCode,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (isSendingCode) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.register_send_code))
                    }
                }
            }

            errorMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (mail.isBlank() || code.isBlank()) {
                        errorMessage = errEmptyFields
                        return@Button
                    }
                    if (!isAllowedEmailDomain(mail)) {
                        errorMessage = errInvalidDomain
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            RuoYiClient.loginByMail(mail, code)
                        }

                        result.fold(
                            onSuccess = { authResult ->
                                AuthManager.saveAuth(
                                    token = authResult.token,
                                    email = authResult.email,
                                    userId = authResult.id
                                )
                                isLoading = false
                                onBack()
                            },
                            onFailure = { error ->
                                isLoading = false
                                errorMessage = error.message ?: errUnknown
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.login_register_btn),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
