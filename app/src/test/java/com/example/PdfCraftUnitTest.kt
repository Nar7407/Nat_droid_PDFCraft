package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AnnotationType
import com.example.data.security.InvalidPdfPasswordException
import com.example.data.security.PdfKeystoreEncryptionEngine
import com.example.viewmodel.PdfCraftViewModel
import com.example.viewmodel.WorkbenchNavDestination
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PdfCraftUnitTest {

    @Test
    fun testInitialStateLoadsSampleDocuments() {
        val viewModel = PdfCraftViewModel()
        val recentDocs = viewModel.recentDocuments.value
        val activeDoc = viewModel.activeDocument.value

        assertTrue(recentDocs.isNotEmpty())
        assertNotNull(activeDoc)
        assertEquals(0, viewModel.currentPageIndex.value)
    }

    @Test
    fun testPageRotateAndDeepUndoRedo() {
        val viewModel = PdfCraftViewModel()
        val initialRotation = viewModel.activeDocument.value!!.pages[0].rotationDegrees

        // Rotate 90 degrees
        viewModel.rotatePage(0, 90)
        assertEquals((initialRotation + 90) % 360, viewModel.activeDocument.value!!.pages[0].rotationDegrees)
        assertEquals(1, viewModel.undoStack.value.size)

        // Undo
        viewModel.undo()
        assertEquals(initialRotation, viewModel.activeDocument.value!!.pages[0].rotationDegrees)
        assertEquals(0, viewModel.undoStack.value.size)
        assertEquals(1, viewModel.redoStack.value.size)

        // Redo
        viewModel.redo()
        assertEquals((initialRotation + 90) % 360, viewModel.activeDocument.value!!.pages[0].rotationDegrees)
        assertEquals(1, viewModel.undoStack.value.size)
        assertEquals(0, viewModel.redoStack.value.size)
    }

    @Test
    fun testNavigationAndTwoUpViewToggle() {
        val viewModel = PdfCraftViewModel()
        assertEquals(WorkbenchNavDestination.DASHBOARD, viewModel.currentDestination.value)

        viewModel.navigateTo(WorkbenchNavDestination.VIEWER)
        assertEquals(WorkbenchNavDestination.VIEWER, viewModel.currentDestination.value)

        val initialTwoUp = viewModel.isTwoUpView.value
        viewModel.toggleTwoUpView()
        assertEquals(!initialTwoUp, viewModel.isTwoUpView.value)
    }

    @Test
    fun testOpenDocumentFromUriFilePickerContract() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = PdfCraftViewModel()

        val sampleUri = Uri.parse("content://com.android.providers.media.documents/document/document%3A1000000001")
        viewModel.openDocumentFromUri(context, sampleUri).join()
        advanceUntilIdle()

        val activeDoc = viewModel.activeDocument.value
        assertNotNull(activeDoc)
        assertEquals(sampleUri.toString(), activeDoc!!.uriString)
        assertTrue(activeDoc.pages.isNotEmpty())
    }

    @Test
    fun testOnDeviceOcrAndSearchWithinScannedImagePdf() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = PdfCraftViewModel()

        // Find and select the scanned image-based document
        val scannedDoc = viewModel.recentDocuments.value.first { it.isScannedDocument }
        viewModel.openDocument(scannedDoc)

        // Before OCR: Querying for invoice terms yields zero matches in raw text layer
        viewModel.updateSearchQuery("Transceiver")
        assertEquals(0, viewModel.searchResults.value.size)

        // Run On-Device OCR text recognition
        viewModel.runOcrOnDocument(context)?.join()
        advanceUntilIdle()

        // Verify OCR completed and marked pages as processed
        val updatedDoc = viewModel.activeDocument.value!!
        assertTrue(updatedDoc.ocrProcessedPageCount > 0)
        assertTrue(updatedDoc.pages.all { it.isOcrProcessed })
        assertTrue(updatedDoc.pages.all { !it.ocrText.isNullOrBlank() })

        // Now searching for text inside scanned image PDF succeeds with OCR match flags!
        viewModel.updateSearchQuery("Transceiver")
        assertTrue(viewModel.searchResults.value.isNotEmpty())
        assertTrue(viewModel.searchResults.value.all { it.isOcrMatch })

        // Search for another recognized term
        viewModel.updateSearchQuery("Apex")
        assertTrue(viewModel.searchResults.value.isNotEmpty())
        assertEquals(true, viewModel.searchResults.value.first().isOcrMatch)

        // Verify OCR filter toggle can disable/enable OCR matching
        viewModel.toggleOcrSearchFilter() // turn off
        assertFalse(viewModel.ocrSearchFilterActive.value)
        assertEquals(0, viewModel.searchResults.value.size)

        viewModel.toggleOcrSearchFilter() // turn on
        assertTrue(viewModel.ocrSearchFilterActive.value)
        assertTrue(viewModel.searchResults.value.isNotEmpty())
    }

    @Test
    fun testDragDropPageOrganizerReorderDuplicateDelete() {
        val viewModel = PdfCraftViewModel()
        val doc = viewModel.activeDocument.value!!
        val initialCount = doc.pages.size
        val firstPageTitle = doc.pages[0].title
        val secondPageTitle = doc.pages[1].title

        // Reorder (Drag & Drop Move): Move Page 0 to Page 1
        viewModel.movePage(0, 1)
        val afterMoveDoc = viewModel.activeDocument.value!!
        assertEquals(secondPageTitle, afterMoveDoc.pages[0].title)
        assertEquals(firstPageTitle, afterMoveDoc.pages[1].title)

        // Duplicate: Duplicate Page 0
        viewModel.duplicatePage(0)
        val afterDupDoc = viewModel.activeDocument.value!!
        assertEquals(initialCount + 1, afterDupDoc.pages.size)
        assertTrue(afterDupDoc.pages[1].title.contains("Copy"))

        // Delete: Delete Page 1
        viewModel.deletePage(1)
        val afterDelDoc = viewModel.activeDocument.value!!
        assertEquals(initialCount, afterDelDoc.pages.size)

        // Reorder Undo test
        viewModel.movePage(0, 1)
        assertEquals(1, viewModel.undoStack.value.size)
        viewModel.undo()
        assertEquals(0, viewModel.undoStack.value.size)
    }

    @Test
    fun testAndroidKeystoreEncryptionAndDecryption() {
        val testData = "CONFIDENTIAL_FINANCIAL_AUDIT_REPORT_2026_DATA_BYTES".toByteArray(Charsets.UTF_8)
        val password = "SecureVaultPassword#2026!"

        val encryptedPackage = PdfKeystoreEncryptionEngine.encryptPdfData(
            documentId = "doc_test_101",
            documentName = "Financial_Audit.pdf",
            plainBytes = testData,
            password = password
        )

        assertNotNull(encryptedPackage)
        assertEquals("doc_test_101", encryptedPackage.documentId)
        assertEquals("Financial_Audit.pdf", encryptedPackage.documentName)
        assertTrue(encryptedPackage.cipherTextBase64.isNotBlank())
        assertTrue(encryptedPackage.ivBase64.isNotBlank())
        assertTrue(encryptedPackage.saltBase64.isNotBlank())
        assertEquals(PdfKeystoreEncryptionEngine.MASTER_KEY_ALIAS, encryptedPackage.keystoreAlias)

        // Decrypt with the exact correct password
        val decryptedBytes = PdfKeystoreEncryptionEngine.decryptPdfData(encryptedPackage, password)
        assertEquals(String(testData, Charsets.UTF_8), String(decryptedBytes, Charsets.UTF_8))
    }

    @Test
    fun testKeystorePasswordProtectionFailureOnWrongPassword() {
        val testData = "SECRET_PATENT_DOCUMENT_SPECIFICATION".toByteArray(Charsets.UTF_8)
        val encryptedPackage = PdfKeystoreEncryptionEngine.encryptPdfData(
            documentId = "doc_secret_999",
            documentName = "Secret_Patent.pdf",
            plainBytes = testData,
            password = "CorrectPassphrase123"
        )

        var failed = false
        try {
            PdfKeystoreEncryptionEngine.decryptPdfData(encryptedPackage, "WrongPassphrase999")
        } catch (e: InvalidPdfPasswordException) {
            failed = true
        }
        assertTrue("Decryption must fail when incorrect password is provided", failed)
    }

    @Test
    fun testPrivacyFirstVaultStorageSaveAndLoad() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testData = "VAULT_LOCAL_PRIVACY_PROTECTED_PDF".toByteArray(Charsets.UTF_8)
        val encryptedPackage = PdfKeystoreEncryptionEngine.encryptPdfData(
            documentId = "doc_vault_test",
            documentName = "Vault_Test.pdf",
            plainBytes = testData,
            password = "VaultPassword777"
        )

        val vaultFile = PdfKeystoreEncryptionEngine.saveEncryptedPdfToVault(context, encryptedPackage)
        assertTrue(vaultFile.exists())
        assertTrue(vaultFile.length() > 0)

        val loadedPackage = PdfKeystoreEncryptionEngine.loadEncryptedPdfFromVault(vaultFile)
        assertNotNull(loadedPackage)
        assertEquals(encryptedPackage.documentId, loadedPackage!!.documentId)
        assertEquals(encryptedPackage.documentName, loadedPackage.documentName)

        val decrypted = PdfKeystoreEncryptionEngine.decryptPdfData(loadedPackage, "VaultPassword777")
        assertEquals(String(testData), String(decrypted))
    }

    @Test
    fun testFreehandCanvasDrawingAnnotations() {
        val viewModel = PdfCraftViewModel()
        val doc = viewModel.activeDocument.value!!
        val pageIndex = 0
        val initialAnnCount = doc.pages[pageIndex].annotations.size

        val points = listOf(
            Pair(10f, 20f),
            Pair(15f, 25f),
            Pair(30f, 40f),
            Pair(50f, 60f)
        )

        viewModel.addDrawingStroke(
            pageIndex = pageIndex,
            points = points,
            colorHex = "#E11D48",
            strokeWidth = 8f
        )

        val updatedDoc = viewModel.activeDocument.value!!
        val page = updatedDoc.pages[pageIndex]
        assertEquals(initialAnnCount + 1, page.annotations.size)

        val drawingAnn = page.annotations.last()
        assertEquals(AnnotationType.DRAWING, drawingAnn.type)
        assertEquals("#E11D48", drawingAnn.colorHex)
        assertEquals(8f, drawingAnn.strokeWidth)
        assertEquals(points.size, drawingAnn.strokePoints.size)

        // Test undoing drawing annotation
        viewModel.removeLastAnnotation(pageIndex)
        val afterUndoDoc = viewModel.activeDocument.value!!
        assertEquals(initialAnnCount, afterUndoDoc.pages[pageIndex].annotations.size)

        // Add two drawings and clear page drawings
        viewModel.addDrawingStroke(pageIndex, points, "#2563EB", 4f)
        viewModel.addDrawingStroke(pageIndex, points, "#16A34A", 6f)
        assertEquals(initialAnnCount + 2, viewModel.activeDocument.value!!.pages[pageIndex].annotations.size)

        viewModel.clearDrawingsOnPage(pageIndex)
        val afterClearDoc = viewModel.activeDocument.value!!
        assertTrue(afterClearDoc.pages[pageIndex].annotations.none { it.type == AnnotationType.DRAWING })
    }

    @Test
    fun testViewModelKeystoreLockAndUnlock() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel = PdfCraftViewModel()

        // Encrypt with Keystore
        val success = viewModel.encryptDocumentWithKeystore(
            context = context,
            password = "MyMasterPassword456",
            hint = "Company Year"
        )
        assertTrue(success)
        val encryptedDoc = viewModel.activeDocument.value!!
        assertTrue(encryptedDoc.isEncrypted)
        assertTrue(encryptedDoc.keystoreProtected)
        assertEquals("Company Year", encryptedDoc.encryptionPasswordHint)

        // Lock document
        viewModel.lockActiveDocument()
        assertTrue(viewModel.activeDocument.value!!.isLocked)

        // Unlock with wrong password fails
        val wrongUnlock = viewModel.unlockDocumentWithPassword("IncorrectPassword")
        assertFalse(wrongUnlock)
        assertTrue(viewModel.activeDocument.value!!.isLocked)

        // Unlock with correct password succeeds
        val correctUnlock = viewModel.unlockDocumentWithPassword("MyMasterPassword456")
        assertTrue(correctUnlock)
        assertFalse(viewModel.activeDocument.value!!.isLocked)

        // Decrypt document with password
        val decryptSuccess = viewModel.decryptDocumentWithPassword("MyMasterPassword456")
        assertTrue(decryptSuccess)
        assertFalse(viewModel.activeDocument.value!!.isEncrypted)
        assertFalse(viewModel.activeDocument.value!!.keystoreProtected)
    }
}
