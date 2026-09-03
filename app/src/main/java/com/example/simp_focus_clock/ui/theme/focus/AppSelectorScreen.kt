package com.example.simp_focus_clock.ui.focus

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.simp_focus_clock.model.InstalledApp
import com.example.simp_focus_clock.repository.AppSelectorRepository

@Composable
fun AppSelectorScreen(
    repository: AppSelectorRepository,
    onAppSelected: (InstalledApp) -> Unit,
    activePackageNames: Set<String> = emptySet()
) {

    var apps by remember {
        mutableStateOf<List<InstalledApp>>(
            emptyList()
        )
    }

    var searchText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        apps =
            repository.getInstalledApps()
    }

    val filteredApps =
        remember(
            apps,
            searchText
        ) {

            if (searchText.isBlank()) {

                apps

            } else {

                apps.filter { app ->

                    app.appName.contains(
                        searchText,
                        ignoreCase = true
                    )
                }
            }
        }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp)
    ) {

        Text(
            text = "Select App",
            style =
                MaterialTheme.typography.headlineSmall
        )

        Spacer(
            modifier =
                Modifier.size(12.dp)
        )

        Text(
            text =
                "${activePackageNames.size}/10 apps currently active",

            style =
                MaterialTheme.typography.bodyMedium
        )

        Spacer(
            modifier =
                Modifier.size(12.dp)
        )

        OutlinedTextField(

            value =
                searchText,

            onValueChange = {
                searchText = it
            },

            modifier =
                Modifier.fillMaxWidth(),

            singleLine = true,

            label = {
                Text("Search apps")
            }
        )

        Spacer(
            modifier =
                Modifier.size(12.dp)
        )

        if (filteredApps.isEmpty()) {

            Box(
                modifier =
                    Modifier.fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Text(
                    text = "No apps found",

                    style =
                        MaterialTheme.typography.bodyLarge
                )
            }

        } else {

            LazyColumn(

                modifier =
                    Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(4.dp)
            ) {

                items(

                    items =
                        filteredApps,

                    key = {
                        it.packageName
                    }

                ) { app ->

                    val isActive =
                        app.packageName in
                                activePackageNames

                    AppRow(

                        app =
                            app,

                        isActive =
                            isActive,

                        onClick = {

                            if (!isActive) {

                                onAppSelected(
                                    app
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    isActive: Boolean,
    onClick: () -> Unit
) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = !isActive,
                    onClick = onClick
                )
                .padding(
                    vertical = 10.dp,
                    horizontal = 8.dp
                ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Image(

            bitmap =
                app.icon
                    .toBitmap(
                        width = 96,
                        height = 96
                    )
                    .asImageBitmap(),

            contentDescription =
                app.appName,

            modifier =
                Modifier.size(48.dp)
        )

        Spacer(
            modifier =
                Modifier.size(16.dp)
        )

        Column {

            Text(
                text =
                    app.appName,

                style =
                    MaterialTheme.typography.bodyLarge
            )

            if (isActive) {

                Spacer(
                    modifier =
                        Modifier.size(2.dp)
                )

                Text(
                    text =
                        "Focus session already active",

                    style =
                        MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}