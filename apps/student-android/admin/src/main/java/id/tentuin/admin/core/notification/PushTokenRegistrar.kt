package id.tentuin.admin.core.notification

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class PushTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /** Ambil FCM token saat ini dan enqueue worker untuk register ke backend. */
    suspend fun ensureRegistered() {
        val token = currentToken() ?: return
        val work = OneTimeWorkRequestBuilder<RegisterPushTokenWorker>()
            .setInputData(Data.Builder()
                .putString(RegisterPushTokenWorker.KEY_FCM_TOKEN, token)
                .build())
            .build()
        WorkManager.getInstance(context).enqueue(work)
    }

    private suspend fun currentToken(): String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener {
                if (cont.isActive) cont.resumeWithException(it)
            }
    }
}
