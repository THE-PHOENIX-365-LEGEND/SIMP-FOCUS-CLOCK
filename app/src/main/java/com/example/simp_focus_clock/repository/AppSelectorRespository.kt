package com.example.simp_focus_clock.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.simp_focus_clock.model.InstalledApp

class AppSelectorRepository(
    private val context: Context
) {

    private val packageManager: PackageManager =
        context.packageManager

    fun getInstalledApps(): List<InstalledApp> {

        val launcherIntent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

        val resolvedApps =
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.MATCH_ALL
            )

        return resolvedApps
            .mapNotNull { resolveInfo ->

                try {

                    val applicationInfo =
                        resolveInfo.activityInfo
                            ?.applicationInfo
                            ?: return@mapNotNull null

                    val packageName =
                        applicationInfo.packageName

                    if (
                        packageName ==
                        context.packageName
                    ) {
                        return@mapNotNull null
                    }

                    val appName =
                        applicationInfo
                            .loadLabel(
                                packageManager
                            )
                            .toString()
                            .trim()

                    if (appName.isBlank()) {
                        return@mapNotNull null
                    }

                    InstalledApp(
                        packageName = packageName,
                        appName = appName,
                        icon =
                            applicationInfo.loadIcon(
                                packageManager
                            )
                    )

                } catch (_: Exception) {

                    null
                }
            }
            .distinctBy {
                it.packageName
            }
            .sortedBy {
                it.appName.lowercase()
            }
    }
}