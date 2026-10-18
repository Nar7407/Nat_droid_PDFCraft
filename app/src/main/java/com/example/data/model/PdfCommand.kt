package com.example.data.model

enum class CommandType {
    ROTATE_PAGE,
    DELETE_PAGE,
    REORDER_PAGE,
    DUPLICATE_PAGE,
    ADD_ANNOTATION,
    REMOVE_ANNOTATION,
    UPDATE_FORM_FIELD,
    SET_WATERMARK,
    SET_ENCRYPTION
}

data class PdfCommand(
    val id: String = java.util.UUID.randomUUID().toString(),
    val documentId: String,
    val type: CommandType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val targetPageIndex: Int? = null,
    val payloadString: String = ""
)
