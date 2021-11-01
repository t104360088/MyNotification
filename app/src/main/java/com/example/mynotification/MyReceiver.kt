package com.example.mynotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput

class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val bmp = BitmapFactory.decodeResource(context.resources, R.drawable.landscape)

        val person = Person.Builder()
            .setName("Me")
            .setBot(true)
            .build()

        val time = System.currentTimeMillis() - 60_000

        val message = NotificationCompat.MessagingStyle.Message(
            getMessageText(intent),
            time,
            person
        )

        val notification = NotificationCompat.Builder(context, "Test")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setLargeIcon(bmp)
            .setStyle(
                NotificationCompat.MessagingStyle(person)
                    .addMessage(message)
            )
            .addAction(context.createReplyAction())
            .build()

        val nm = NotificationManagerCompat.from(context)
        nm.notify(1, notification)
    }

    private fun getMessageText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(MainActivity.KEY_TEXT_REPLY)
    }
}