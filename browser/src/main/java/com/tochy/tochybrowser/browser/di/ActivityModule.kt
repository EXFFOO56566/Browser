

package com.tochy.tochybrowser.browser.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import com.tochy.tochybrowser.browser.BrowserActivity
import com.tochy.tochybrowser.download.DownloadDialogModule
import com.tochy.tochybrowser.legacy.di.WebViewModule

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [WebViewModule::class, DownloadDialogModule::class])
    abstract fun contributeBrowserActivity(): BrowserActivity
}
