package org.wvt.horizonmgr

import android.app.Application
import android.content.Context
import android.os.Environment
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.wvt.horizonmgr.service.hzpack.HorizonManager
import org.wvt.horizonmgr.service.level.LevelTransporter
import org.wvt.horizonmgr.service.level.MCLevelManager
import org.wvt.horizonmgr.service.respack.ResourcePackManager
import org.wvt.horizonmgr.utils.LocalCache
import org.wvt.horizonmgr.utils.ModDownloader
import org.wvt.horizonmgr.utils.OfficialCDNPackageDownloader
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import org.wvt.horizonmgr.webapi.mgrinfo.MgrInfoModule
import org.wvt.horizonmgr.webapi.mod.ChineseModRepository
import org.wvt.horizonmgr.webapi.mod.OfficialModMirrorRepository
import org.wvt.horizonmgr.webapi.news.MgrArticleModule
import org.wvt.horizonmgr.webapi.pack.OfficialPackageCDNRepository
import javax.inject.Singleton

@HiltAndroidApp
class HorizonManagerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DependenciesModule {
    @Singleton
    @Provides
    fun provideLocalCache(
        @ApplicationContext context: Context
    ): LocalCache = LocalCache(context)

    @Singleton
    @Provides
    fun provideHorizonManager(): HorizonManager {
        return HorizonManager(
            Environment.getExternalStorageDirectory().resolve("games").resolve("horizon")
        )
    }

    @Singleton
    @Provides
    fun provideOfficialPackageCDNRepository() = OfficialPackageCDNRepository()

    @Singleton
    @Provides
    fun provideChineseModRepository() = ChineseModRepository()

    @Singleton
    @Provides
    fun provideOfficialModMirrorRepository() = OfficialModMirrorRepository()

    @Singleton
    @Provides
    fun provideMgrInfoModule() = MgrInfoModule()

    @Singleton
    @Provides
    fun provideICCNModule() = ICCNModule()

    @Singleton
    @Provides
    fun provideMgrArticleModule() = MgrArticleModule()

    @Singleton
    @Provides
    fun provideOfficialCDNPackageDownloader(
        @ApplicationContext context: Context
    ) = OfficialCDNPackageDownloader(context)

    @Singleton
    @Provides
    fun provide(
        @ApplicationContext context: Context
    ) = ModDownloader(context)

    @Singleton
    @Provides
    fun provideMCLevelManager() = MCLevelManager(
        Environment.getExternalStorageDirectory()
            .resolve("games")
            .resolve("com.mojang")
            .resolve("minecraftWorlds")
    )

    @Singleton
    @Provides
    fun provideLevelTransporter() = LevelTransporter(
        Environment.getExternalStorageDirectory()
            .resolve("games")
            .resolve("com.mojang")
            .resolve("minecraftWorlds").absolutePath
    )

    @Singleton
    @Provides
    fun provideResourcePackManager() = ResourcePackManager(
        Environment.getExternalStorageDirectory()
            .resolve("games")
            .resolve("com.mojang")
            .resolve("resource_packs")
    )
}