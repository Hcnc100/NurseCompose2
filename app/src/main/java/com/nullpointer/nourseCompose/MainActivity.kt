package com.nullpointer.nourseCompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nullpointer.nourseCompose.inject.viewModel.measure.ViewModelFactoryProvider
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.state.rememberRootState
import com.nullpointer.nourseCompose.navigation.LocalRootNavController
import com.nullpointer.nourseCompose.ui.screens.NavGraphs
import com.nullpointer.nourseCompose.ui.screens.onboarding.MedicationIntroDialog
import com.nullpointer.nourseCompose.ui.screens.settings.viewModel.SettingsViewModel
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val settingsViewModel by viewModels<SettingsViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var showSplash = true
        installSplashScreen().setKeepOnScreenCondition { showSplash }
        lifecycleScope.launch { repeatOnLifecycle(Lifecycle.State.CREATED) { delay(1500); showSplash = false } }
        setContent {
            MyApplicationTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val settings by settingsViewModel.settingsData.collectAsState()
                    if (settings?.onboardingCompleted != true) {
                        MedicationIntroDialog(settingsViewModel::completeOnboarding)
                    } else {
                        val rootState = rememberRootState()
                        val (navController, rootActionsDestinations) = rootState
                        CompositionLocalProvider(LocalRootNavController provides navController) {
                            Scaffold { padding ->
                                DestinationsNavHost(
                                    navGraph = NavGraphs.root,
                                    navController = navController,
                                    modifier = Modifier.padding(padding),
                                    dependenciesContainerBuilder = {
                                        dependency(rootActionsDestinations)
                                        dependency(settingsViewModel)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun measureViewModelProvider(measureType: MeasureType): MeasureViewModel {
    val activity = LocalActivity.current ?: error("Activity is not available")
    val factory = EntryPointAccessors.fromActivity(activity, ViewModelFactoryProvider::class.java).measureViewModelFactory()
    return viewModel(factory = MeasureViewModel.provideMainViewModelFactory(factory, measureType))
}
