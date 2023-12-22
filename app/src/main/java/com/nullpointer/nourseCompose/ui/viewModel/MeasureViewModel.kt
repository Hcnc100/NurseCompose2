package com.nullpointer.nourseCompose.ui.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import com.nullpointer.nourseCompose.models.data.MeasureData
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

class MeasureViewModel @AssistedInject constructor(
    private val measureRepository: MeasureRepository,
    @Assisted private val measureType: MeasureType
) : ViewModel() {


    private val _message = Channel<String>()
    val message = _message.receiveAsFlow()


    var measureListSelected = mutableStateListOf<MeasureData>()
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
            _message.send("Error get paginate in ${measureType.name} $it")
        }
        .flowOn(Dispatchers.IO).cachedIn(viewModelScope)


    val lastMeasureList =
        measureRepository
            .getListMeasureByType(measureType, 10)
            .flowOn(Dispatchers.IO)
            .catch {
                emit(emptyList())
                _message.send("Error get last list in ${measureType.name} $it")
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
                        throw RuntimeException("Cannot create an instance of $modelClass", e)
                    }
                }
            }
        }
    }
}