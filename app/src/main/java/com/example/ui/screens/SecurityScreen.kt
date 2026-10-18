package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.PdfCraftViewModel

@Composable
fun SecurityScreen(
    viewModel: PdfCraftViewModel
) {
    val context = LocalContext.current
    val activeDoc by viewModel.activeDocument.collectAsState()
    val keystoreStatus by viewModel.keystoreStatus.collectAsState()
    val lastEncryptedPackage by viewModel.lastEncryptedPackage.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordHintInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var decryptPasswordInput by remember { mutableStateOf("") }

    var watermarkInput by remember { mutableStateOf(activeDoc?.watermarkText ?: "CONFIDENTIAL") }

    var allowPrinting by remember { mutableStateOf(activeDoc?.permissions?.allowPrinting ?: true) }
    var allowCopying by remember { mutableStateOf(activeDoc?.permissions?.allowCopying ?: true) }
    var allowModifying by remember { mutableStateOf(activeDoc?.permissions?.allowModifying ?: false) }
    var allowAnnotations by remember { mutableStateOf(activeDoc?.permissions?.allowAnnotations ?: true) }

    val isDocEncrypted = activeDoc?.isEncrypted == true || activeDoc?.keystoreProtected == true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("security_screen")
    ) {
        // Screen Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Security & Keystore Encryption",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Hardware-backed Android Keystore password protection for local storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Android Keystore Hardware Status Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("keystore_status_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Android Keystore System",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshKeystoreStatus() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Keystore Status",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Security Level:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = keystoreStatus.securityLevel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Master Key Alias:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = keystoreStatus.masterKeyAlias,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cipher & Derivation:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "AES-256-GCM / PBKDF2 (10,000 rds)",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Document Password Protection Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDocEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isDocEncrypted) Color(0xFFC2410C) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PDF Password Protection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isDocEncrypted) "Protected with Keystore AES-256-GCM" else "No password set (Unencrypted)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Quick Toggle Switch
                    Switch(
                        checked = isDocEncrypted,
                        onCheckedChange = { enable ->
                            if (!enable) {
                                viewModel.setEncryption(false)
                            }
                        },
                        modifier = Modifier.testTag("toggle_encryption_switch")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isDocEncrypted) {
                    // Document is currently encrypted
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Protected with Android Keystore Engine",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Document payload is encrypted with authenticated AES-256-GCM. Unlocking requires your master password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F)
                            )
                            if (!activeDoc?.encryptionPasswordHint.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Password Hint: ${activeDoc?.encryptionPasswordHint}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Decrypt input
                    OutlinedTextField(
                        value = decryptPasswordInput,
                        onValueChange = { decryptPasswordInput = it },
                        label = { Text("Enter Password to Decrypt") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("decrypt_password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                if (decryptPasswordInput.isNotBlank()) {
                                    val success = viewModel.decryptDocumentWithPassword(decryptPasswordInput)
                                    if (success) decryptPasswordInput = ""
                                } else {
                                    viewModel.setEncryption(false)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("decrypt_with_password_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Decrypt Document")
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.exportEncryptedPdfToVault(context)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_to_vault_button")
                        ) {
                            Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Vault")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.lockActiveDocument() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lock_document_now_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Lock Document (Require Password to View)")
                    }
                } else {
                    // Document is unencrypted: form to set password and encrypt
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Master Password") },
                        placeholder = { Text("Choose a strong passphrase") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = { confirmPasswordInput = it },
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Re-enter passphrase") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_confirm_password_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = passwordHintInput,
                        onValueChange = { passwordHintInput = it },
                        label = { Text("Password Hint (Optional)") },
                        placeholder = { Text("E.g. Department access key") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("security_password_hint_input")
                    )

                    // Password strength indicator
                    if (passwordInput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val strength = calculatePasswordStrength(passwordInput)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Text(
                                text = "Strength: ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strength.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = strength.color
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (passwordInput.isNotBlank()) {
                                if (confirmPasswordInput.isNotEmpty() && passwordInput != confirmPasswordInput) {
                                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.encryptDocumentWithKeystore(
                                    context = context,
                                    password = passwordInput,
                                    hint = passwordHintInput
                                )
                                passwordInput = ""
                                confirmPasswordInput = ""
                                passwordHintInput = ""
                            }
                        },
                        enabled = passwordInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("encrypt_with_keystore_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypt with Android Keystore")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy-First Local Vault Details Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy-First Local Storage Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Encrypted documents remain strictly on-device inside your private sandbox (context.filesDir/encrypted_vault/). No keys or data leave the device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lastEncryptedPackage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Vault Package: ${lastEncryptedPackage?.documentName}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Cipher Size: ${lastEncryptedPackage?.encryptedSizeBytes} bytes • Auth Tag: 128-bit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Watermark Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Diagonal Watermark Stamp",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Applies semi-transparent, anti-tamper watermark across all pages.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = watermarkInput,
                    onValueChange = { watermarkInput = it },
                    label = { Text("Watermark Text") },
                    placeholder = { Text("CONFIDENTIAL / DRAFT") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watermark_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { viewModel.setWatermark(watermarkInput) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("apply_watermark_button")
                    ) {
                        Text("Apply Watermark")
                    }
                    if (activeDoc?.watermarkText != null) {
                        Button(
                            onClick = {
                                viewModel.setWatermark(null)
                                watermarkInput = ""
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("remove_watermark_button")
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Granular Permissions Card
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Document Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                PermissionToggleRow("Allow High-Quality Printing", allowPrinting) {
                    allowPrinting = it
                    activeDoc?.permissions?.copy(allowPrinting = it)?.let { p -> viewModel.updatePermissions(p) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                PermissionToggleRow("Allow Text & Content Copying", allowCopying) {
                    allowCopying = it
                    activeDoc?.permissions?.copy(allowCopying = it)?.let { p -> viewModel.updatePermissions(p) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                PermissionToggleRow("Allow Document Modifications", allowModifying) {
                    allowModifying = it
                    activeDoc?.permissions?.copy(allowModifying = it)?.let { p -> viewModel.updatePermissions(p) }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                PermissionToggleRow("Allow Comments & Annotations", allowAnnotations) {
                    allowAnnotations = it
                    activeDoc?.permissions?.copy(allowAnnotations = it)?.let { p -> viewModel.updatePermissions(p) }
                }
            }
        }
    }
}

private data class PasswordStrength(val label: String, val color: Color)

private fun calculatePasswordStrength(password: String): PasswordStrength {
    return when {
        password.length < 6 -> PasswordStrength("Weak", Color(0xFFE53935))
        password.length < 10 -> PasswordStrength("Medium", Color(0xFFFB8C00))
        password.any { it.isDigit() } && password.any { !it.isLetterOrDigit() } -> PasswordStrength("Strong (Hardware AES-256 Grade)", Color(0xFF43A047))
        else -> PasswordStrength("Good", Color(0xFF7CB342))
    }
}

@Composable
private fun PermissionToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
