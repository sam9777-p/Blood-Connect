package com.example.blood

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.blood.ui.donor.DonorActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val channelId = "blood_request_channel"
        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Blood Requests",
                NotificationManager.IMPORTANCE_HIGH
            )

            val sysNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            sysNotificationManager.createNotificationChannel(channel)
        }

        val title = remoteMessage.notification?.title ?: "Blood Notification"
        val body = remoteMessage.notification?.body ?: ""

        // Default intent if nothing matches
        var intent = Intent(this, DonorActivity::class.java)

        val status = remoteMessage.data["type"]

        if (status == "request_fulfilled") {
            intent = Intent(this, RequestFulfilledActivity::class.java).apply {
                putExtra("handledBy", remoteMessage.data["handledBy"])
                putExtra("bloodGroup", remoteMessage.data["bloodGroup"])
                putExtra("units", remoteMessage.data["units"]?.toIntOrNull() ?: 0)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }

        else if (status == "blood_request") {
            intent =Intent(this, DonorRequestNotification::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("requestId", remoteMessage.data["requestId"])
                putExtra("bloodGroup", remoteMessage.data["bloodGroup"])
                putExtra("city", remoteMessage.data["city"])
                putExtra("requesterName", remoteMessage.data["requesterName"])
                putExtra("units", remoteMessage.data["units"])
                putExtra("handledBy", remoteMessage.data["handledBy"])
            }
        }
        val stackBuilder = android.app.TaskStackBuilder.create(this).apply {
            addNextIntentWithParentStack(intent)
        }

        val pendingIntent = stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_blood_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notificationManager.notify((0..100000).random(), notification)
            } else {
                Log.w("FCMService", "POST_NOTIFICATIONS permission not granted")
            }
        } else {
            notificationManager.notify((0..100000).random(), notification)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "New token: $token")

        val uid = FirebaseAuth.getInstance().currentUser?.phoneNumber
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("Accounts")
                .document(uid)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCMService", "Token updated") }
                .addOnFailureListener { e -> Log.e("FCMService", "Failed to update token", e) }
        } else {
            Log.w("FCMService", "User not logged in. Token not saved.")
        }
    }
}
