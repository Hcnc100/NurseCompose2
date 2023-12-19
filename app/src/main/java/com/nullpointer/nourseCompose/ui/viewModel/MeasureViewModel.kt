package com.nullpointer.nourseCompose.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.nullpointer.nourseCompose.domain.measure.MeasureRepository
import com.nullpointer.nourseCompose.models.data.MeasureData
import com.nullpointer.nourseCompose.models.types.MeasureType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MeasureViewModel @Inject constructor(
    private val measureRepository: MeasureRepository
) : ViewModel() {


    val listPagingTemperature = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 30,
            prefetchDistance = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            measureRepository.getPagingMeasureByType(MeasureType.TEMPERATURE)
        }
    ).flow.map { list ->
        list.map(MeasureData::fromMeasureEntity)
    }.flowOn(Dispatchers.IO).cachedIn(viewModelScope)


    val lastTemperatureList =
        measureRepository
            .getListMeasureByType(MeasureType.TEMPERATURE, 10)
            .flowOn(Dispatchers.IO)
            .catch {
                emit(emptyList())
                println(it)
            }.stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )


    fun addFakeData() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            val random = Random.nextFloat() * (38.0f - 36.0f) + 36.0f
            measureRepository.addMeasure(
                type = MeasureType.TEMPERATURE,
                value = random
            )
        }
    }
}