package com.example.data.model

data class PdfDocument(
    val id: String,
    val name: String,
    val uriString: String? = null,
    val sizeBytes: Long = 0L,
    val pageCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val author: String = "Author",
    val title: String = "Untitled Document",
    val subject: String = "General",
    val keywords: List<String> = emptyList(),
    val isEncrypted: Boolean = false,
    val encryptionPasswordHint: String? = null,
    val encryptedPayloadHex: String? = null,
    val isLocked: Boolean = false,
    val keystoreProtected: Boolean = false,
    val permissions: DocumentPermissions = DocumentPermissions(),
    val bookmarks: List<PdfBookmark> = emptyList(),
    val pages: List<PdfPage> = emptyList(),
    val formFields: List<PdfFormField> = emptyList(),
    val watermarkText: String? = null,
    val isScannedDocument: Boolean = false,
    val ocrProcessedPageCount: Int = 0
)

data class DocumentPermissions(
    val allowPrinting: Boolean = true,
    val allowCopying: Boolean = true,
    val allowModifying: Boolean = true,
    val allowAnnotations: Boolean = true,
    val allowFormFilling: Boolean = true
)

data class PdfBookmark(
    val id: String,
    val title: String,
    val pageIndex: Int,
    val level: Int = 0
)

data class PdfPage(
    val pageIndex: Int,
    val rotationDegrees: Int = 0,
    val width: Int = 595,
    val height: Int = 842,
    val title: String = "Page ${pageIndex + 1}",
    val textSnippet: String = "",
    val ocrText: String? = null,
    val isOcrProcessed: Boolean = false,
    val isScannedImage: Boolean = false,
    val annotations: List<PdfAnnotation> = emptyList(),
    val isSelected: Boolean = false
)

enum class AnnotationType {
    HIGHLIGHT,
    DRAWING,
    NOTE,
    SIGNATURE
}

data class PdfAnnotation(
    val id: String,
    val type: AnnotationType,
    val pageIndex: Int,
    val colorHex: String = "#FFEB3B",
    val content: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 100f,
    val height: Float = 40f,
    val strokeWidth: Float = 4f,
    val strokePoints: List<Pair<Float, Float>> = emptyList()
)

enum class FormFieldType {
    TEXT,
    CHECKBOX,
    SIGNATURE,
    DROPDOWN
}

data class PdfFormField(
    val id: String,
    val name: String,
    val type: FormFieldType,
    val pageIndex: Int,
    val value: String = "",
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val x: Float = 50f,
    val y: Float = 100f
)
