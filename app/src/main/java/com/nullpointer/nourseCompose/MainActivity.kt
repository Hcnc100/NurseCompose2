package com.nullpointer.nourseCompose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nullpointer.nourseCompose.inject.viewModel.measure.ViewModelFactoryProvider
import com.nullpointer.nourseCompose.models.types.MeasureType
import com.nullpointer.nourseCompose.ui.screens.temperature.TemperatureScreen
import com.nullpointer.nourseCompose.ui.theme.MyApplicationTheme
import com.nullpointer.nourseCompose.ui.viewModel.MeasureViewModel
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    TemperatureScreen()
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

