package com.tochy.tochybrowser.legacy.webkit.handler

import android.content.Context
import com.tochy.tochybrowser.legacy.utils.extensions.setClipboardWithToast
import java.lang.ref.WeakReference

class WebSrcImageCopyUrlHandler(context: Context) : WebSrcImageHandler() {
    private val mReference: WeakReference<Context> = WeakReference(context)

    override fun handleUrl(url: String) {
        mReference.get()?.run {
            setClipboardWithToast(url)
        }
    }
}
