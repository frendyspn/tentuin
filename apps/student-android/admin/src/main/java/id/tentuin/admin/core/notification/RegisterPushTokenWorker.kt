package id.tentuin.admin.core.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import id.tentuin.admin.core.datastore.SessionDataStore
import id.tentuin.admin.data.repository.NotificationRepository
import kotlinx.coroutines.flow.first

@HiltWorker
class RegisterPushTokenWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params:     WorkerParameters,
    private val session:        SessionDataStore,
    private val notificationRepo: NotificationRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val fcmToken = inputData.getString(KEY_FCM_TOKEN) ?: return Result.failure()
        val userId   = session.userId.first() ?: return Result.retry()
        val role     = session.role.first()
        if (role !in setOf("admin", "super_admin")) {
            // belum login sebagai admin → tunggu nanti retry setelah login
            return Result.retry()
        }

        return notificationRepo.registerToken(
            userId      = userId,
            fcmToken    = fcmToken,
            deviceLabel = android.os.Build.MODEL,
        ).fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val KEY_FCM_TOKEN = "fcm_token"
    }
}
