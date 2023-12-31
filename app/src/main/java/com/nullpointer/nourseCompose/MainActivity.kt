package com.nullpointer.nourseCompose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nullpointer.nourseCompose.inject.viewModel.measure.ViewModelFactoryProvider
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.state.rememberRootState
import com.nullpointer.nourseCompose.ui.screens.NavGraphs
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                delay(1500)
                showSplash = false
            }
        }
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    val rootState = rememberRootState()

                    val (scaffoldState, navController, rootActionsDestinations) = rootState

                    Scaffold(
                        scaffoldState = scaffoldState
                    ) {
                        DestinationsNavHost(
                            navGraph = NavGraphs.root,
                            navController = navController,
                            modifier = Modifier.padding(it),
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

@Composable
fun measureViewModelProvider(measureType: MeasureType): MeasureViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    ).measureViewModelFactory()

    return viewModel(factory = MeasureViewModel.provideMainViewModelFactory(factory, measureType))
}

