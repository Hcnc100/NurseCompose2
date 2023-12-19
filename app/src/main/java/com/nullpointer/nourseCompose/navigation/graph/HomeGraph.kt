package com.nullpointer.nourseCompose.navigation.graph

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class HomeGraph(
    val start: Boolean = false
)
