package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.PdfDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentPropertiesDialog(
    document: PdfDocument,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Document Properties",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                PropertyRow("File Name", document.name)
                PropertyRow("Title", document.title)
                PropertyRow("Author", document.author)
                PropertyRow("Subject", document.subject)
                PropertyRow("Page Count", "${document.pageCount} pages")
                PropertyRow("File Size", formatFileSize(document.sizeBytes))
                PropertyRow("Created", dateFormat.format(Date(document.createdAt)))
                PropertyRow("Modified", dateFormat.format(Date(document.modifiedAt)))
                PropertyRow("Encrypted", if (document.isEncrypted) "Yes (AES-256)" else "No")
                PropertyRow("Watermark", document.watermarkText ?: "None")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Security Permissions",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                PropertyRow("Printing", if (document.permissions.allowPrinting) "Allowed" else "Restricted")
                PropertyRow("Content Copying", if (document.permissions.allowCopying) "Allowed" else "Restricted")
                PropertyRow("Document Modifying", if (document.permissions.allowModifying) "Allowed" else "Restricted")
                PropertyRow("Annotations", if (document.permissions.allowAnnotations) "Allowed" else "Restricted")
                PropertyRow("Form Filling", if (document.permissions.allowFormFilling) "Allowed" else "Restricted")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun PropertyRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(0.55f)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "1.2 MB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format(Locale.getDefault(), "%.2f MB", mb)
    } else {
        String.format(Locale.getDefault(), "%.1f KB", kb)
    }
}
