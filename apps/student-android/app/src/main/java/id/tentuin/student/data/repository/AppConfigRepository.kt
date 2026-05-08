package id.tentuin.student.data.repository

import id.tentuin.student.BuildConfig
import id.tentuin.student.core.network.SupabaseApi
import id.tentuin.student.core.util.VersionComparator
import id.tentuin.student.data.model.ForceUpdateResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepository @Inject constructor(
    private val api: SupabaseApi,
) {
    suspend fun checkForceUpdate(): ForceUpdateResult {
        return runCatching {
            val config = api.getAppConfig().firstOrNull() ?: return ForceUpdateResult.UpToDate
            val current = BuildConfig.VERSION_NAME
            if (VersionComparator.isLessThan(current, config.minVersion)) {
                ForceUpdateResult.UpdateRequired(config.storeUrl)
            } else {
                ForceUpdateResult.UpToDate
            }
        }.getOrDefault(ForceUpdateResult.UpToDate)
    }
}
