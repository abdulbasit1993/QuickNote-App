package com.abmapps.quicknote.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.abmapps.quicknote.ui.screens.NoteEditorScreen
import com.abmapps.quicknote.ui.screens.NotesListScreen
import com.abmapps.quicknote.viewmodel.NoteViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Single shared ViewModel scoped to the navigation graph
    val noteViewModel: NoteViewModel = viewModel()

    NavHost(navController = navController, startDestination = "notes_list") {
        composable("notes_list") {
            NotesListScreen(
                viewModel = noteViewModel,
                onAddNoteClick = { navController.navigate("note_editor") },
                onNoteClick = { noteId -> navController.navigate("note_editor?noteId=$noteId") }
            )
        }

        composable(
            route = "note_editor?noteId={noteId}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteEditorScreen(
                noteId = noteId,
                viewModel = noteViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }

}