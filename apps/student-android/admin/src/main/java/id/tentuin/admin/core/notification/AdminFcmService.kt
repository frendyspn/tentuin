package id.tentuin.admin.core.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import id.tentuin.admin.AdminApp
import id.tentuin.admin.ui.MainActivity
import kotlin.random.Random

class AdminFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val req = OneTimeWorkRequestBuilder<RegisterPushTokenWorker>()
            .setInputData(Data.Builder()
                .putString(RegisterPushTokenWorker.KEY_FCM_TOKEN, token)
                .build())
            .build()
        WorkManager.getInstance(applicationContext).enqueue(req)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val notif    = message.notification
        val data     = message.data
        val title    = notif?.title ?: data["title"] ?: "Tentuin Admin"
        val body     = notif?.body  ?: data["body"]  ?: ""
        val route    = data["route"]
        val event    = data["event"]

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (route != null) putExtra(EXTRA_ROUTE, route)
            if (event != null) putExtra(EXTRA_EVENT, event)
        }

        val pi = PendingIntent.getActivity(
            this,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val builder = NotificationCompat.Builder(this, AdminApp.CHANNEL_ADMIN_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)

        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(Random.nextInt(), builder.build())
    }

    companion object {
        const val EXTRA_ROUTE = "tentuin.admin.notif.route"
        const val EXTRA_EVENT = "tentuin.admin.notif.event"
    }
}
