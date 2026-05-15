package id.tentuin.admin.data.repository

import id.tentuin.admin.core.network.AdminApi
import id.tentuin.admin.data.model.UpsertPushTokenRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val api: AdminApi,
) {
    suspend fun registerToken(
        userId:      String,
        fcmToken:    String,
        deviceLabel: String? = null,
    ): Result<Unit> = runCatching {
        api.upsertPushToken(
            UpsertPushTokenRequest(
                userId      = userId,
                fcmToken    = fcmToken,
                platform    = "android",
                deviceLabel = deviceLabel,
            )
        )
        Unit
    }

    suspend fun unregisterToken(userId: String, fcmToken: String): Result<Unit> = runCatching {
        api.deletePushToken(
            userId   = "eq.$userId",
            fcmToken = "eq.$fcmToken",
        )
        Unit
    }
}
