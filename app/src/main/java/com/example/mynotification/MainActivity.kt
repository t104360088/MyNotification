package com.example.mynotification

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.media.app.NotificationCompat as MediaNotificationCompat
import com.example.mynotification.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var nm: NotificationManagerCompat
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nm = NotificationManagerCompat.from(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createChannel()

        binding.btnNormal.setOnClickListener {
            showNotification()
        }

        binding.btnBigPic.setOnClickListener {
            showExpandableNotification(NotificationStyle.BigPicture)
        }

        binding.btnBigText.setOnClickListener {
            showExpandableNotification(NotificationStyle.BigText)
        }

        binding.btnInbox.setOnClickListener {
            showExpandableNotification(NotificationStyle.Inbox)
        }

        binding.btnMsg.setOnClickListener {
            showExpandableNotification(NotificationStyle.Messaging)
        }

        binding.btnMedia.setOnClickListener {
            showExpandableNotification(NotificationStyle.Media)
        }

        binding.btnCustom.setOnClickListener {
            showCustomNotification()
        }

        binding.btnSetting.setOnClickListener {
            openNotificationSettingsForApp()
        }
    }

    private fun createChannel() {
        //若 Android 版本在 8.0 以上必須先建立通知頻道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //設定頻道 Id、名稱及訊息優先權
            val name = "My Channel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("Test", name, importance).apply {
                setBypassDnd(true) //由使用者決定是否要在勿擾時顯示，測試沒用
                setShowBadge(true)
            }
            //建立頻道
            nm.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val bmp = BitmapFactory.decodeResource(this.resources, R.drawable.landscape)
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        val notification = NotificationCompat.Builder(this, "Test")
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setLargeIcon(bmp)
            .setContentTitle("折價券")
            .setContentText("您還有一張五折折價券，滿額消費即贈現金回饋")
            .setContentIntent(pendingIntent) //It performs after being clicked
            .setAutoCancel(true) //It disappears after being clicked
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) //Show on lock screen
            .setNumber(2)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()

        nm.notify(0, notification)
    }

    private fun showCustomNotification() {
        // Get the layouts to use in the custom notification
        val notificationLayout = RemoteViews(packageName, R.layout.notification_small)
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_large)
        val bmp = BitmapFactory.decodeResource(this.resources, R.drawable.landscape)
        notificationLayout.setBitmap(R.id.img_icon, "setImageBitmap", bmp)

        // Apply the layouts to the notification
        val notification = NotificationCompat.Builder(this, "Test")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutExpanded)
            .build()

        nm.notify(0, notification)
    }

    enum class NotificationStyle {
        BigPicture,
        BigText,
        Inbox,
        Messaging,
        Media
    }

    private fun showExpandableNotification(style: NotificationStyle) {
        val bmp = BitmapFactory.decodeResource(this.resources, R.drawable.landscape)
        val builder = NotificationCompat.Builder(this, "Test")
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setLargeIcon(bmp)
            .setContentTitle("Title")
            .setContentText("Description")

        val notification = when (style) {
            // Call BigPictureStyle.bigLargeIcon() and pass it null
            // so the large icon goes away when the notification is expanded.
            NotificationStyle.BigPicture ->
                builder
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bmp)
                            .bigLargeIcon(null)
                    )
            NotificationStyle.BigText ->
                builder
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.long_string))
                    )
            // Can add multiple pieces of content text that are each truncated to one line,
            // instead of one continuous line of text provided by NotificationCompat.BigTextStyle.
            // To add a new line, call addLine() up to 6 times.
            NotificationStyle.Inbox ->
                builder
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine("Line1")
                            .addLine("Line2")
                            .addLine("Line3")
                    )
            NotificationStyle.Messaging -> {
                val me = Person.Builder()
                    .setName("Me")
                    .build()

                val msg = NotificationCompat.MessagingStyle.Message(
                    "How are you",
                    System.currentTimeMillis(),
                    Person.Builder().setName("Nick").build()
                )

                builder
                    .setStyle(
                        NotificationCompat.MessagingStyle(me)
                            .addMessage(msg)
                    )
                    .addAction(createReplyAction())
            }
            NotificationStyle.Media -> {
                builder
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    // Add media control buttons that invoke intents in your media service
                    .addAction(android.R.drawable.ic_media_previous, "Previous", null) // #0
                    .addAction(android.R.drawable.ic_media_pause, "Pause", null) // #1
                    .addAction(android.R.drawable.ic_media_next, "Next", null) // #2
                    // Apply the media style template
                    .setStyle(
                        MediaNotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(1 /* #1: pause button \*/)
                            .setMediaSession(getMediaSessionCompat().sessionToken)
                    )
                    .setContentTitle("Wonderful music")
                    .setContentText("My Awesome Band")
                    .setLargeIcon(bmp)
            }
        }.build()

        nm.notify(1, notification)
    }

    private fun getMediaSessionCompat(): MediaSessionCompat {
        return MediaSessionCompat(baseContext, "TAG").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    Log.e("debug", "aaa")
                }
            })

            // Set the session's token so that client activities can communicate with it.
            // setSessionToken(sessionToken)
        }
    }

    private fun openNotificationSettingsForApp() {
        // Links to this app's notification settings.
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.putExtra("app_package", packageName)
        intent.putExtra("app_uid", applicationInfo.uid)

        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName)
        startActivity(intent)
    }

    private fun cancelNotification() {
        nm.cancelAll()
    }

    companion object {
        val KEY_TEXT_REPLY = "key_text_reply"
    }
}

fun Context.createReplyAction(): NotificationCompat.Action {
    val remoteInput = RemoteInput.Builder(MainActivity.KEY_TEXT_REPLY).run {
        setLabel("Enter the content to reply")
        build()
    }

    val i = Intent(this, MyReceiver::class.java)
        .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)

    // Build a PendingIntent for the reply action to trigger.
    val replyPendingIntent: PendingIntent =
        PendingIntent.getBroadcast(
            applicationContext,
            0,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    // Create the reply action and add the remote input.
    return NotificationCompat.Action.Builder(
        R.drawable.ic_launcher_background,
        "Reply", replyPendingIntent
    )
        .addRemoteInput(remoteInput)
        .build()
}