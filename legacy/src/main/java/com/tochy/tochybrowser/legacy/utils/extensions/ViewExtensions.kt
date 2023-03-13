/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tochy.tochybrowser.legacy.utils.extensions

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.documentfile.provider.DocumentFile
import com.tochy.tochybrowser.core.MIME_TYPE_MHTML
import com.tochy.tochybrowser.core.utility.extensions.getWritableFileOrNull
import com.tochy.tochybrowser.core.utility.utils.ui
import com.tochy.tochybrowser.download.NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY
import com.tochy.tochybrowser.download.core.data.DownloadFile
import com.tochy.tochybrowser.download.core.data.DownloadFileInfo
import com.tochy.tochybrowser.download.core.data.MetaData
import com.tochy.tochybrowser.download.createFileOpenIntent
import com.tochy.tochybrowser.download.service.DownloadDatabase
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.webview.CustomWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.io.File

fun CustomWebView.saveArchive(root: DocumentFile, file: DownloadFile) {
    val outFile = root.uri.getWritableFileOrNull()

    if (outFile != null) {
        val downloadedFile = File(outFile, file.name)
        saveWebArchiveMethod(downloadedFile.toString())
        onDownload(webView.context, root, file, DocumentFile.fromFile(downloadedFile), true, downloadedFile.length())
        return
    }

    ui {
        val context = webView.context
        val tmpFile = File(context.cacheDir, "page.tmp")
        saveWebArchiveMethod(tmpFile.absolutePath)

        delay(1000)
        var success = tmpFile.exists()
        if (success) {
            val size = withContext(Dispatchers.IO) {
                var size = 0L
                do {
                    delay(200)
                    val oldSize = size
                    size = tmpFile.length()
                } while (size == 0L || oldSize != size)
                return@withContext size
            }

            val name = file.name!!

            val saveTo = root.createFile(MIME_TYPE_MHTML, name)
            if (saveTo == null) {
                context.toast(R.string.failed)
                return@ui
            }

            withContext(Dispatchers.IO) {
                tmpFile.inputStream().use { input ->
                    context.contentResolver.openOutputStream(saveTo.uri, "w").use { out ->
                        checkNotNull(out)
                        input.copyTo(out)
                    }
                }
                tmpFile.delete()
            }

            success = saveTo.exists()

            onDownload(context, root, file, saveTo, success, size)
        }
    }
}

private fun onDownload(context: Context, root: DocumentFile, file: DownloadFile, downloadedFile: DocumentFile, success: Boolean, size: Long) {
    val name = file.name!!

    val info = DownloadFileInfo(root, file, MetaData(name, MIME_TYPE_MHTML, size, false))
    info.state = if (success) DownloadFileInfo.STATE_DOWNLOADED else DownloadFileInfo.STATE_UNKNOWN_ERROR
    DownloadDatabase.getInstance(context).insert(info)

    if (success) {
        context.toast(context.getString(R.string.saved_file) + name)

        val notify = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle(name)
                .setContentText(context.getText(R.string.download_success))
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentIntent(PendingIntent.getActivity(context.applicationContext, 0, info.createFileOpenIntent(context, downloadedFile), 0))
                .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(info.id.toInt(), notify)
    }
}
