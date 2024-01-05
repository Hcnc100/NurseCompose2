package com.nullpointer.nourseCompose.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.nullpointer.nourseCompose.R
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.data.MeasureError
import com.nullpointer.nourseCompose.models.types.MeasureType
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class MeasureViewModel @AssistedInject constructor(
    private val measureRepository: MeasureRepository,
    @Assisted private val measureType: MeasureType
) : ViewModel() {


    private val _message = Channel<MeasureError>()
    val message = _message.receiveAsFlow()


    val measureSelected = mutableStateMapOf<Int, MeasureData>()

    var measureSelectedCount by mutableIntStateOf(0)
        private set

    val listPagingMeasure = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 30,
            prefetchDistance = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            measureRepository.getPagingMeasureByType(measureType)
        }
    ).flow.map { list ->
        list.map(MeasureData::fromMeasureEntity)
    }
        .catch {
            emit(PagingData.empty())
            _message.send(MeasureError(R.string.error_paginate_measure, measureType.titleMeasure))
            Timber.e("Error get paginate in $measureType $it")
            Firebase.crashlytics.setCustomKeys {
                key("Error get paginate in ", measureType.toString())
            }
            Firebase.crashlytics.recordException(it)
        }
        .flowOn(Dispatchers.IO).cachedIn(viewModelScope)


    val lastMeasureList =
        measureRepository
            .getListMeasureByType(measureType)
            .flowOn(Dispatchers.IO)
            .catch {
                emit(emptyList())
                _message.send(
                    MeasureError(
                        R.string.error_get_last_measure,
                        measureType.titleMeasure
                    )
                )
                Timber.e("Error get last measure in $measureType")
                Firebase.crashlytics.setCustomKeys {
                    key("Error get paginate in ", measureType.toString())
                }
                Firebase.crashlytics.recordException(it)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun addMeasureData(value1: Float, value2: Float? = null) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            measureRepository.addMeasure(
                type = measureType,
                value1 = value1,
                value2 = value2,
            )
        }
    }

    fun clearSelection() {
        measureSelected.clear()
        measureSelectedCount = 0
    }

    fun deleterAllSelected() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            measureRepository.deleterMeasureData(measureSelected.values.toList())
        }
        measureSelected.clear()
        measureSelectedCount = 0
    }

    fun toggleMeasureData(measureData: MeasureData) {
        if (measureSelected.containsKey(measureData.id)) {
            measureSelected.remove(measureData.id)
        } else {
            measureSelected[measureData.id] = measureData
        }

        measureSelectedCount = measureSelected.size

    }


    @AssistedFactory
    interface Factory {
        fun create(measureType: MeasureType): MeasureViewModel
    }

    companion object {
        /**
         * Provides a factory for creating instances of MeasureViewModel.
         *
         * @param factory The factory to create the ViewModel.
         * @param measureType The type of measure for the ViewModel.
         * @return A ViewModelProvider.Factory that creates MeasureViewModels.
         */
        fun provideMainViewModelFactory(
            factory: Factory,
            measureType: MeasureType
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return try {
                        factory.create(measureType) as T
                    } catch (e: Exception) {
                        Timber.e("Error creating view model with $measureType")
                        Firebase.crashlytics.setCustomKeys {
                            key("Error creating view model of ", measureType.toString())
                        }
                        Firebase.crashlytics.recordException(e)
                        throw RuntimeException("Cannot create an instance of $modelClass", e)
                    }
                }
            }
        }
    }
}