package com.example.catagenttracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.catagenttracker.RouteTrackingService.Companion.NOTIFICATION_ID

class RouteTrackingService: Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onCreate() {
        super.onCreate()
        notificationBuilder= startForegroundService()
        val handlerThread= HandlerThread("Route Tracking").apply {
            start()
        }
        serviceHandler= Handler(handlerThread.looper)
    }
    private fun trackToDestination(notificationBuilder: NotificationCompat.Builder){
        for (i in 10 downTo 0){
            Thread.sleep(1000L)
            notificationBuilder
                .setContentText("$i seconds to destination")
            startForeground(NOTIFICATION_ID,
            notificationBuilder.build())
        }
    }
    private fun notifyCompletion(agentId: String){
        Handler(Looper.getMainLooper()).post{
            mutableTrackingCompletion.value = agentId
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue= super.onStartCommand(intent, flags, startId)
        val agentId= intent?.getStringExtra(EXTRA_SECRET_CAT_AGENT_ID)
            ?: throw IllegalStateException("Agent id must be provided")
        serviceHandler.post {
            trackToDestination(notificationBuilder)
            notifyCompletion(agentId)
            stopForeground(true)
            stopSelf()
        }
        return returnValue
    }

    override fun onBind(intent: Intent?): IBinder?= null

    companion object{
        const val NOTIFICATION_ID= 0xCA7
        const val EXTRA_SECRET_CAT_AGENT_ID= "ScaId"
        private val mutableTrackingCompletion = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableTrackingCompletion

    }

    private fun pendingIntent()= PendingIntent.getActivity(this,0, Intent(this, MainActivity::class.java),0)

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        val channelId= "route Tracking"
        val channelName= "route Tracking"
        val channel= NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        val service= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        service.createNotificationChannel(channel)
        return channelId

    }
    private fun getNotificationBuilder(pendingIntent: PendingIntent,channelId: String)=
        NotificationCompat.Builder(this, channelId)
            .setContentTitle("Agent approaching destination")
            .setContentText("Agent dispatched")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setTicker("Agent dispatched, tracking movement")

private fun startForegroundService(): NotificationCompat.Builder{
    val pendingIntent= getPendingIntent()
    val channelId=if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
        createNotificationChannel()
    } else{
        ""
    }
    val notificationBuilder= getNotificationBuilder(pendingIntent,
    channelId)
    startForeground(NOTIFICATION_ID,notificationBuilder.build())
    return notificationBuilder

}

    private fun getPendingIntent()= PendingIntent.getActivity(this,0,
        Intent(this,MainActivity::class.java),0)

}