package org.wvt.horizonmgr.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.activity.InstallPackageActivity
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.utils.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.viewmodel.InstallPackageViewModel
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository
import javax.inject.Inject

const val CHANNEL_ID = "online_package_install"

@AndroidEntryPoint
class OnlinePackageInstallService : Service() {
    private var notificationId = 912957293
    private lateinit var notification: Notification

    private val binder = MyBinder()

    private val scope = CoroutineScope(Dispatchers.Default)

    @Inject
    protected lateinit var manager: HorizonManager

    @Inject
    protected lateinit var packRepository: OfficialPackageCDNRepository

    @Inject
    protected lateinit var downloader: OfficialCDNPackageDownloader

    override fun onBind(intent: Intent): MyBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()

        notification = NotificationCompat.Builder(this.applicationContext, CHANNEL_ID).apply {
            setContentTitle("正在下载安装分包")
            setSmallIcon(R.drawable.ic_gear_full)
            val intent =
                PendingIntent.getActivity(
                    this@OnlinePackageInstallService.applicationContext,
                    0,
                    Intent(
                        this@OnlinePackageInstallService.applicationContext,
                        InstallPackageActivity::class.java
                    ).apply { action = "show_progress" }, 0
                )
            setContentIntent(intent)
        }.build()
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .apply {
                    setName("Online Package Installation")
                    setDescription("This notification shows the progress when you request to download and install a online package.")
                }.build()

        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun install() = scope.launch {
        val openIntent = PendingIntent.getActivity(
            this@OnlinePackageInstallService.applicationContext,
            0,
            Intent(
                this@OnlinePackageInstallService.applicationContext,
                InstallPackageActivity::class.java
            ).apply { action = "show_progress" }, 0
        )

        val builder = NotificationCompat.Builder(this@OnlinePackageInstallService.applicationContext, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_gear_full)
            setContentIntent(openIntent)
        }

        startForeground(
            notificationId,
            builder.apply {
                setContentTitle("正在请求分包")
            }.build()
        )


        startForeground(
            notificationId,
            builder.apply {
                setContentTitle("正在下载分包")
                setProgress(100, 0, false)
            }.build()
        )

        startForeground(
            notificationId,
            builder.apply {
                setContentTitle("正在安装分包")
                setProgress(100, 0, true)
            }.build()
        )

        startForeground(
            notificationId,
            builder.apply {
                setContentTitle("安装完成")
                setProgress(0, 0, false)
                setAutoCancel(true)
            }.build()
        )

        stopForeground(false)
    }

    inner class MyBinder : Binder() {
        inner class Task {
            val state =
                MutableStateFlow<InstallPackageViewModel.State>(InstallPackageViewModel.State.Loading)
            var totalProgress = MutableStateFlow<Float>(0f)
            val downloadSteps =
                MutableStateFlow<List<InstallPackageViewModel.DownloadStep>>(emptyList())
            val mergeState = MutableStateFlow<InstallPackageViewModel.StepState>(
                InstallPackageViewModel.StepState.Waiting
            )
            val downloadState = MutableStateFlow<InstallPackageViewModel.StepState>(
                InstallPackageViewModel.StepState.Waiting
            )
            val installState = MutableStateFlow<InstallPackageViewModel.StepState>(
                InstallPackageViewModel.StepState.Waiting
            )

            fun cancel() {

            }
        }

        fun getTasks(): Set<Task> {
            TODO()
        }

        fun startInstall(uuid: String, customName: String) {
            install()
        }
    }
}