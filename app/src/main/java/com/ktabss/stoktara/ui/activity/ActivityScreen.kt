package com.ktabss.stoktara.ui.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ktabss.stoktara.data.AppGraph
import com.ktabss.stoktara.data.AuditLog
import com.ktabss.stoktara.data.InventoryRepository
import com.ktabss.stoktara.ui.components.ActivityRow
import com.ktabss.stoktara.ui.components.EmptyNote
import com.ktabss.stoktara.ui.components.ScreenHeader
import com.ktabss.stoktara.ui.components.groupedRowsCard
import com.ktabss.stoktara.ui.theme.InkSoft
import com.ktabss.stoktara.ui.theme.Line
import java.util.Calendar
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private fun sixMonthsAgo(): Long = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }.timeInMillis

private fun List<AuditLog>.inWindow(): List<AuditLog> = sixMonthsAgo().let { cutoff -> filter { it.timestamp >= cutoff } }

class ActivityViewModel(repo: InventoryRepository = AppGraph.repository) : ViewModel() {
    /** Read-only by design (UC5): the trail can be viewed, never edited or deleted. */
    val logs: StateFlow<List<AuditLog>> = repo.auditLog
        .map { it.inWindow() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repo.auditLog.value.inWindow())
}

@Composable
fun ActivityScreen(onBack: () -> Unit, viewModel: ActivityViewModel = viewModel()) {
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Recent Activity", onBack = onBack)
        Text(
            "Showing all activity for the past 6 months",
            style = MaterialTheme.typography.bodySmall,
            color = InkSoft,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        )
        if (logs.isEmpty()) {
            EmptyNote("No activity yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f, fill = false)
                    .groupedRowsCard(),
            ) {
                itemsIndexed(logs, key = { _, log -> log.id }) { index, log ->
                    if (index > 0) HorizontalDivider(color = Line)
                    ActivityRow(log)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
