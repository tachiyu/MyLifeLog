package com.example.timeitforward.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.timeitforward.TimeLogViewModel
import com.example.timeitforward.model.db.TimeLog

@Composable
fun SummaryScreenSetup(viewModel: TimeLogViewModel) {
    val searchResults by viewModel.searchResults.observeAsState(listOf())

    SummaryScreen(
        searchResults = searchResults,
        viewModel = viewModel
    )
}

@Composable
fun SummaryScreen(
    searchResults: List<TimeLog>,
    viewModel: TimeLogViewModel
) {
    Text(text = "Hello World")
}