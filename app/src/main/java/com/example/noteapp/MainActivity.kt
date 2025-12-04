package com.example.noteapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.noteapp.data.NoteDatabase
import com.example.noteapp.data.NoteRepository
import com.example.noteapp.data.SettingsManager
import com.example.noteapp.ui.screens.CreateNoteScreen
import com.example.noteapp.ui.screens.EditNoteScreen
import com.example.noteapp.ui.screens.NoteListScreen
import com.example.noteapp.ui.screens.SettingsScreen
import com.example.noteapp.ui.theme.NoteAppTheme
import com.example.noteapp.ui.viewmodel.NoteViewModel
import com.example.noteapp.utils.LocaleManager





class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingsManager = SettingsManager(this)

        setContent {
            val context = LocalContext.current

            // Правильная подписка на StateFlow
            val currentLanguage by settingsManager.language.collectAsState()
            val currentTheme by settingsManager.theme.collectAsState()

            val updatedContext = remember(currentLanguage) {
                LocaleManager.setLocale(context, currentLanguage)
            }

            CompositionLocalProvider(
                LocalContext provides updatedContext
            ) {
                // Определяем, использовать ли тёмную тему
                val darkTheme = when (currentTheme) {
                    "dark" -> true
                    "light" -> false
                    else -> isSystemInDarkTheme()
                }

                NoteAppTheme(
                    darkTheme = darkTheme,
                    dynamicColor = false
                ) {
                    val database = NoteDatabase.getInstance(this)
                    val repository = NoteRepository(database.noteDao())
                    val viewModel = remember { NoteViewModel(repository) }
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "noteList"
                    ) {
                        composable("noteList") {
                            NoteListScreen(
                                viewModel = viewModel,
                                onCreateNote = {
                                    navController.navigate("createNote")
                                },
                                onEditNote = { note ->
                                    navController.navigate("editNote/${note.id}")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable("createNote") {
                            CreateNoteScreen(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            "editNote/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1
                            val notes by viewModel.notes.collectAsState()
                            val note = notes.find { it.id == noteId }

                            if (note != null) {
                                EditNoteScreen(
                                    viewModel = viewModel,
                                    note = note,
                                    onBack = { navController.popBackStack() }
                                )
                            } else {
                                androidx.compose.material3.Text(
                                    text = stringResource(R.string.note_not_found),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .wrapContentSize()
                                )
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            }
                        }

                        composable("settings") {
                            SettingsScreen(
                                settingsManager = settingsManager,
                                currentLanguage = currentLanguage,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}