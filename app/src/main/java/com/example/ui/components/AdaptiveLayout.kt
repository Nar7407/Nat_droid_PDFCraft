package com.example.ui.components

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.AnnotationsScreen
import com.example.ui.screens.FormsScreen
import com.example.ui.screens.MergeSplitScreen
import com.example.ui.screens.PageOrganizerScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.screens.ToolsMatrixScreen
import com.example.ui.screens.WorkbenchDashboardScreen
import com.example.viewmodel.PdfCraftViewModel
import com.example.viewmodel.WorkbenchNavDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveLayout(
    viewModel: PdfCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context: Context = LocalContext.current
    val currentDestination by viewModel.currentDestination.collectAsState()
    val activeDoc by viewModel.activeDocument.collectAsState()
    val undoStack by viewModel.undoStack.collectAsState()
    val redoStack by viewModel.redoStack.collectAsState()
    val statusMsg by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMsg) {
        statusMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Storage Access Framework File Picker Launcher using ActivityResultContracts.OpenDocument
    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.openDocumentFromUri(context, it) }
    }

    // Handle Android system Back button
    BackHandler(enabled = currentDestination != WorkbenchNavDestination.DASHBOARD) {
        viewModel.navigateTo(WorkbenchNavDestination.DASHBOARD)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row {
                        Text(
                            text = if (currentDestination == WorkbenchNavDestination.DASHBOARD) {
                                "PdfCraft Studio"
                            } else {
                                activeDoc?.name ?: "PdfCraft"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    if (currentDestination != WorkbenchNavDestination.DASHBOARD) {
                        IconButton(
                            onClick = { viewModel.navigateTo(WorkbenchNavDestination.DASHBOARD) },
                            modifier = Modifier.testTag("nav_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Dashboard"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.setShowCommandPalette(true) },
                            modifier = Modifier.testTag("app_logo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PdfCraft Studio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Open Document File Picker Intent button
                    IconButton(
                        onClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.testTag("top_bar_open_doc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Open PDF from device storage"
                        )
                    }

                    // Undo button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.testTag("top_bar_undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo action"
                        )
                    }

                    // Redo button
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = redoStack.isNotEmpty(),
                        modifier = Modifier.testTag("top_bar_redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo action"
                        )
                    }

                    // Audit trail history
                    IconButton(
                        onClick = { viewModel.setShowAuditTrailDialog(true) },
                        modifier = Modifier.testTag("top_bar_audit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Audit History"
                        )
                    }

                    // Command Palette
                    IconButton(
                        onClick = { viewModel.setShowCommandPalette(true) },
                        modifier = Modifier.testTag("top_bar_search_palette_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Command Palette"
                        )
                    }

                    // Document Properties Inspector
                    if (activeDoc != null) {
                        IconButton(
                            onClick = { viewModel.setShowPropertiesDialog(true) },
                            modifier = Modifier.testTag("top_bar_properties_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Document Properties"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentDestination == WorkbenchNavDestination.DASHBOARD,
                    onClick = { viewModel.navigateTo(WorkbenchNavDestination.DASHBOARD) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
                NavigationBarItem(
                    selected = currentDestination == WorkbenchNavDestination.VIEWER,
                    onClick = { viewModel.navigateTo(WorkbenchNavDestination.VIEWER) },
                    icon = { Icon(Icons.Default.PictureAsPdf, contentDescription = "Viewer") },
                    label = { Text("Viewer") },
                    modifier = Modifier.testTag("nav_item_viewer")
                )
                NavigationBarItem(
                    selected = currentDestination == WorkbenchNavDestination.PAGE_ORGANIZER,
                    onClick = { viewModel.navigateTo(WorkbenchNavDestination.PAGE_ORGANIZER) },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Organizer") },
                    label = { Text("Organize") },
                    modifier = Modifier.testTag("nav_item_organizer")
                )
                NavigationBarItem(
                    selected = currentDestination == WorkbenchNavDestination.TOOLS_MATRIX,
                    onClick = { viewModel.navigateTo(WorkbenchNavDestination.TOOLS_MATRIX) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Tools") },
                    label = { Text("Tools") },
                    modifier = Modifier.testTag("nav_item_tools")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                WorkbenchNavDestination.DASHBOARD -> WorkbenchDashboardScreen(viewModel = viewModel, onOpenPdfIntent = {
                    openPdfLauncher.launch(arrayOf("application/pdf"))
                })
                WorkbenchNavDestination.VIEWER -> PdfViewerScreen(viewModel = viewModel)
                WorkbenchNavDestination.PAGE_ORGANIZER -> PageOrganizerScreen(viewModel = viewModel)
                WorkbenchNavDestination.MERGE_SPLIT -> MergeSplitScreen(viewModel = viewModel)
                WorkbenchNavDestination.SECURITY -> SecurityScreen(viewModel = viewModel)
                WorkbenchNavDestination.ANNOTATIONS -> AnnotationsScreen(viewModel = viewModel)
                WorkbenchNavDestination.FORMS -> FormsScreen(viewModel = viewModel)
                WorkbenchNavDestination.TOOLS_MATRIX -> ToolsMatrixScreen(viewModel = viewModel)
            }
        }
    }

    // Modal Dialog Overlays
    val showCommandPalette by viewModel.showCommandPalette.collectAsState()
    if (showCommandPalette) {
        CommandPaletteDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.setShowCommandPalette(false) },
            onOpenPdfClick = {
                viewModel.setShowCommandPalette(false)
                openPdfLauncher.launch(arrayOf("application/pdf"))
            }
        )
    }

    val showPropertiesDialog by viewModel.showPropertiesDialog.collectAsState()
    if (showPropertiesDialog && activeDoc != null) {
        DocumentPropertiesDialog(
            document = activeDoc!!,
            onDismiss = { viewModel.setShowPropertiesDialog(false) }
        )
    }

    val showBookmarksDialog by viewModel.showBookmarksDialog.collectAsState()
    if (showBookmarksDialog && activeDoc != null) {
        BookmarksDrawerDialog(
            document = activeDoc!!,
            currentPageIndex = viewModel.currentPageIndex.collectAsState().value,
            onSelectBookmark = { pageIdx ->
                viewModel.setPageIndex(pageIdx)
                viewModel.setShowBookmarksDialog(false)
            },
            onDismiss = { viewModel.setShowBookmarksDialog(false) }
        )
    }

    val showAuditTrailDialog by viewModel.showAuditTrailDialog.collectAsState()
    if (showAuditTrailDialog) {
        AuditTrailDialog(
            undoCommands = undoStack,
            redoCommands = redoStack,
            onUndo = { viewModel.undo() },
            onRedo = { viewModel.redo() },
            onDismiss = { viewModel.setShowAuditTrailDialog(false) }
        )
    }

    val showOcrDialog by viewModel.showOcrResultsDialog.collectAsState()
    val lastOcrSummary by viewModel.lastOcrSummary.collectAsState()
    if (showOcrDialog && lastOcrSummary != null) {
        OcrResultsDialog(
            summary = lastOcrSummary!!,
            onSearchText = { query ->
                viewModel.navigateTo(WorkbenchNavDestination.VIEWER)
                viewModel.updateSearchQuery(query)
            },
            onDismiss = { viewModel.setShowOcrResultsDialog(false) }
        )
    }

    val isPasswordUnlockVisible by viewModel.isPasswordUnlockDialogVisible.collectAsState()
    if ((activeDoc?.isLocked == true || isPasswordUnlockVisible) && activeDoc != null) {
        PasswordUnlockDialog(
            documentName = activeDoc!!.name,
            passwordHint = activeDoc?.encryptionPasswordHint,
            onUnlock = { pwd ->
                viewModel.unlockDocumentWithPassword(pwd)
            },
            onDismiss = {
                viewModel.setPasswordUnlockDialogVisible(false)
            }
        )
    }
}
