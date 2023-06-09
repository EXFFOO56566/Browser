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

package com.tochy.tochybrowser.browser

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.TextView
import android.widget.Toast
import androidx.print.PrintHelper
import com.tochy.tochybrowser.adblock.ui.original.AdBlockActivity
import com.tochy.tochybrowser.adblock.ui.original.AddAdBlockDialog
import com.tochy.tochybrowser.bookmark.view.BookmarkActivity
import com.tochy.tochybrowser.core.utility.extensions.clipboardText
import com.tochy.tochybrowser.core.utility.log.ErrorReport
import com.tochy.tochybrowser.core.utility.utils.FileUtils
import com.tochy.tochybrowser.core.utility.utils.ImageUtils
import com.tochy.tochybrowser.core.utility.utils.ui
import com.tochy.tochybrowser.download.core.data.DownloadFile
import com.tochy.tochybrowser.download.core.data.DownloadRequest
import com.tochy.tochybrowser.download.download
import com.tochy.tochybrowser.download.getBlobDownloadJavaScript
import com.tochy.tochybrowser.download.getDownloadFolderUri
import com.tochy.tochybrowser.download.getImage
import com.tochy.tochybrowser.download.ui.DownloadListActivity
import com.tochy.tochybrowser.download.ui.FastDownloadActivity
import com.tochy.tochybrowser.download.ui.fragment.DownloadDialog
import com.tochy.tochybrowser.download.ui.fragment.SaveWebArchiveDialog
import com.tochy.tochybrowser.favicon.FaviconManager
import com.tochy.tochybrowser.history.presenter.BrowserHistoryActivity
import com.tochy.tochybrowser.legacy.Constants
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.action.SingleAction
import com.tochy.tochybrowser.legacy.action.item.*
import com.tochy.tochybrowser.legacy.action.item.startactivity.StartActivitySingleAction
import com.tochy.tochybrowser.legacy.action.manager.ActionController
import com.tochy.tochybrowser.legacy.action.view.ActionActivity
import com.tochy.tochybrowser.legacy.action.view.ActionListViewAdapter
import com.tochy.tochybrowser.legacy.browser.BrowserController
import com.tochy.tochybrowser.legacy.browser.BrowserManager
import com.tochy.tochybrowser.legacy.browser.Scripts
import com.tochy.tochybrowser.legacy.pattern.url.PatternUrlActivity
import com.tochy.tochybrowser.legacy.reader.ReaderActivity
import com.tochy.tochybrowser.legacy.readitlater.ReadItLaterActivity
import com.tochy.tochybrowser.legacy.resblock.ResourceBlockListActivity
import com.tochy.tochybrowser.legacy.settings.activity.MainSettingsActivity
import com.tochy.tochybrowser.legacy.settings.preference.ClearBrowserDataAlertDialog
import com.tochy.tochybrowser.legacy.settings.preference.ProxySettingDialog
import com.tochy.tochybrowser.legacy.speeddial.view.SpeedDialSettingActivity
import com.tochy.tochybrowser.legacy.tab.manager.MainTabData
import com.tochy.tochybrowser.legacy.useragent.UserAgentListActivity
import com.tochy.tochybrowser.legacy.userjs.UserScriptListActivity
import com.tochy.tochybrowser.legacy.utils.DisplayUtils
import com.tochy.tochybrowser.legacy.utils.WebUtils
import com.tochy.tochybrowser.legacy.utils.extensions.setClipboardWithToast
import com.tochy.tochybrowser.legacy.webencode.WebTextEncodeListActivity
import com.tochy.tochybrowser.legacy.webkit.TabType
import com.tochy.tochybrowser.legacy.webkit.handler.*
import com.tochy.tochybrowser.search.domain.makeGoogleImageSearch
import com.tochy.tochybrowser.ui.BrowserApplication
import com.tochy.tochybrowser.ui.dialog.SeekBarDialog
import com.tochy.tochybrowser.ui.extensions.decodePunyCodeUrl
import com.tochy.tochybrowser.ui.settings.AppPrefs
import com.tochy.tochybrowser.ui.utils.PackageUtils
import com.tochy.tochybrowser.ui.utils.makeUrlFromQuery
import com.tochy.tochybrowser.ui.widget.ContextMenuTitleView
import com.tochy.tochybrowser.webview.utility.WebViewUtils
import com.tochy.tochybrowser.webview.utility.getUserAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class ActionExecutor(
    private val controller: BrowserController,
    private val faviconManager: FaviconManager
) : ActionController {
    override fun run(action: SingleAction, target: ActionController.HitTestResultTargetInfo): Boolean {
        val result = target.webView.hitTestResult ?: return false
        val url = result.extra ?: return false

        when (result.type) {
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                when (action.id) {
                    SingleAction.LPRESS_OPEN -> {
                        controller.openInCurrentTab(url)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_NEW -> {
                        controller.openInNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_BG -> {
                        controller.openInBackground(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_NEW_RIGHT -> {
                        controller.openInRightNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_BG_RIGHT -> {
                        controller.openInRightBgTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_SHARE -> {
                        WebUtils.shareWeb(controller.activity, url, null)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_OTHERS -> {
                        controller.activity.startActivity(PackageUtils.createChooser(controller.activity, url, controller.applicationContextInfo.getText(R.string.open_other_app)))
                        return true
                    }
                    SingleAction.LPRESS_COPY_URL -> {
                        controller.applicationContextInfo.setClipboardWithToast(url)
                        return true
                    }
                    SingleAction.LPRESS_SAVE_PAGE_AS -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 1), null)
                        } else {
                            DownloadDialog(url, target.webView.webSettings.userAgentString)//TODO referer
                                    .show(controller.activity.supportFragmentManager, "download")
                        }
                        return true
                    }
                    SingleAction.LPRESS_SAVE_PAGE -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 2), null)
                        } else {
                            val file = DownloadFile(url, null, DownloadRequest(null, target.webView.webSettings.userAgentString, null))
                            controller.activity.download(getDownloadFolderUri(controller.applicationContextInfo), file, null)
                        }
                        return true
                    }
                    SingleAction.LPRESS_PATTERN_MATCH -> {
                        controller.activity.startActivity(Intent(controller.activity, PatternUrlActivity::class.java).apply {
                            putExtra(Intent.EXTRA_TEXT, url)
                        })
                        return true
                    }
                    SingleAction.LPRESS_COPY_LINK_TEXT -> {
                        target.webView.requestFocusNodeHref(WebSrcLinkCopyHandler(controller.activity).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_ADD_BLACK_LIST -> {
                        AddAdBlockDialog.addBackListInstance(result.extra)
                                .show(controller.activity.supportFragmentManager, "add black")
                        return true
                    }
                    SingleAction.LPRESS_ADD_WHITE_LIST -> {
                        AddAdBlockDialog.addWhiteListInstance(result.extra)
                                .show(controller.activity.supportFragmentManager, "add white")
                        return true
                    }
                    else -> return run(action, target, null)
                }
            }
            WebView.HitTestResult.IMAGE_TYPE -> {
                when (action.id) {
                    SingleAction.LPRESS_OPEN_IMAGE -> {
                        controller.openInCurrentTab(url)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_NEW -> {
                        controller.openInNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_BG -> {
                        controller.openInBackground(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_NEW_RIGHT -> {
                        controller.openInRightNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_BG_RIGHT -> {
                        controller.openInRightBgTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_SHARE_IMAGE_URL -> {
                        WebUtils.shareWeb(controller.activity, url, null)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_OTHERS -> {
                        controller.activity.startActivity(PackageUtils.createChooser(controller.activity, url, controller.activity.getText(R.string.open_other_app)))
                        return true
                    }
                    SingleAction.LPRESS_COPY_IMAGE_URL -> {
                        controller.applicationContextInfo.setClipboardWithToast(url)
                        return true
                    }
                    SingleAction.LPRESS_SAVE_IMAGE_AS -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 1), null)
                        } else {
                            DownloadDialog(url, target.webView.webSettings.userAgentString, target.webView.url, ".jpg")
                                    .show(controller.activity.supportFragmentManager, "download")
                        }
                        return true
                    }
                    SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH -> {
                        controller.openInNewTab(url.makeGoogleImageSearch(), TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_IMAGE_RES_BLOCK -> {
                        controller.activity.startActivity(Intent(controller.activity, ResourceBlockListActivity::class.java).apply {
                            setAction(Constants.intent.ACTION_BLOCK_IMAGE)
                            putExtra(Intent.EXTRA_TEXT, url)
                        })
                        return true
                    }
                    SingleAction.LPRESS_PATTERN_MATCH -> {
                        controller.activity.startActivity(Intent(controller.activity, PatternUrlActivity::class.java).apply {
                            putExtra(Intent.EXTRA_TEXT, url)
                        })
                        return true
                    }
                    SingleAction.LPRESS_SHARE_IMAGE -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 3), null)
                        } else {
                            val intent = FastDownloadActivity.intent(
                                    controller.activity,
                                    url, target.webView.url,
                                    target.webView.getUserAgent(),
                                    ".jpg")
                            controller.startActivity(intent, BrowserController.REQUEST_SHARE_IMAGE)
                        }
                        return true
                    }
                    SingleAction.LPRESS_SAVE_IMAGE -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 2), null)
                        } else {
                            val file = DownloadFile(url, null,
                                    DownloadRequest(target.webView.url, target.webView.webView.settings.userAgentString, ".jpg"))
                            controller.activity.download(getDownloadFolderUri(controller.applicationContextInfo), file, null)
                        }
                        return true
                    }
                    SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST -> {
                        AddAdBlockDialog.addBackListInstance(url)
                                .show(controller.activity.supportFragmentManager, "add black")
                        return true
                    }
                    SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST -> {
                        AddAdBlockDialog.addWhiteListInstance(url)
                                .show(controller.activity.supportFragmentManager, "add white")
                        return true
                    }
                    else -> return run(action, target, null)
                }
            }
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                when (action.id) {
                    SingleAction.LPRESS_OPEN -> {
                        target.webView.requestFocusNodeHref(WebSrcImageLoadUrlHandler(controller).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_OPEN_NEW -> {
                        target.webView.requestFocusNodeHref(WebSrcImageOpenNewTabHandler(controller).obtainMessage())//TODO check stratActionMode's Nullpo exception
                        return true
                    }
                    SingleAction.LPRESS_OPEN_BG -> {
                        target.webView.requestFocusNodeHref(WebSrcImageOpenBackgroundHandler(controller).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_OPEN_NEW_RIGHT -> {
                        target.webView.requestFocusNodeHref(WebSrcImageOpenRightNewTabHandler(controller).obtainMessage())//TODO check stratActionMode's Nullpo exception
                        return true
                    }
                    SingleAction.LPRESS_OPEN_BG_RIGHT -> {
                        target.webView.requestFocusNodeHref(WebSrcImageOpenRightBgTabHandler(controller).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_SHARE -> {
                        target.webView.requestFocusNodeHref(WebSrcImageShareWebHandler(controller.activity).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_OPEN_OTHERS -> {
                        target.webView.requestFocusNodeHref(WebSrcImageOpenOtherAppHandler(controller.activity).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_COPY_URL -> {
                        target.webView.requestFocusNodeHref(WebSrcImageCopyUrlHandler(controller.applicationContextInfo).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_SAVE_PAGE_AS -> {
                        target.webView.requestFocusNodeHref(WebImageHandler(controller.activity,
                                target.webView.webSettings.userAgentString
                                        ?: WebSettings.getDefaultUserAgent(controller.applicationContextInfo)).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE -> {
                        controller.openInCurrentTab(url)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_NEW -> {
                        controller.openInNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_BG -> {
                        controller.openInBackground(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_NEW_RIGHT -> {
                        controller.openInRightNewTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_BG_RIGHT -> {
                        controller.openInRightBgTab(url, TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_SHARE_IMAGE_URL -> {
                        WebUtils.shareWeb(controller.activity, url, null)
                        return true
                    }
                    SingleAction.LPRESS_OPEN_IMAGE_OTHERS -> {
                        controller.activity.startActivity(PackageUtils.createChooser(controller.activity, url, controller.activity.getText(R.string.open_other_app)))
                        return true
                    }
                    SingleAction.LPRESS_COPY_IMAGE_URL -> {
                        controller.applicationContextInfo.setClipboardWithToast(url)
                        return true
                    }
                    SingleAction.LPRESS_SAVE_IMAGE_AS -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 1), null)
                        } else {
                            DownloadDialog(url, target.webView.webSettings.userAgentString, target.webView.url, ".jpg")
                                    .show(controller.activity.supportFragmentManager, "download")
                        }
                        return true
                    }
                    SingleAction.LPRESS_GOOGLE_IMAGE_SEARCH -> {
                        controller.openInNewTab(url.makeGoogleImageSearch(), TabType.WINDOW)
                        return true
                    }
                    SingleAction.LPRESS_IMAGE_RES_BLOCK -> {
                        controller.activity.startActivity(Intent(controller.activity, ResourceBlockListActivity::class.java).apply {
                            setAction(Constants.intent.ACTION_BLOCK_IMAGE)
                            putExtra(Intent.EXTRA_TEXT, url)
                        })
                        return true
                    }
                    SingleAction.LPRESS_PATTERN_MATCH -> {
                        controller.activity.startActivity(Intent(controller.activity, PatternUrlActivity::class.java).apply {
                            putExtra(Intent.EXTRA_TEXT, url)
                        })
                        return true
                    }
                    SingleAction.LPRESS_SHARE_IMAGE -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 3), null)
                        } else {
                            val webView = target.webView
                            val intent = FastDownloadActivity.intent(
                                    controller.activity,
                                    url,
                                    webView.url,
                                    webView.getUserAgent(),
                                    ".jpg")
                            controller.startActivity(intent, BrowserController.REQUEST_SHARE_IMAGE)
                        }
                        return true
                    }
                    SingleAction.LPRESS_SAVE_IMAGE -> {
                        if (url.startsWith("blob:")) {
                            target.webView.evaluateJavascript(controller.activity.getBlobDownloadJavaScript(url, controller.secretKey, 2), null)
                        } else {
                            val file = DownloadFile(url, null,
                                    DownloadRequest(target.webView.url, target.webView.webView.settings.userAgentString, ".jpg"))
                            controller.activity.download(getDownloadFolderUri(controller.applicationContextInfo), file, null)
                        }
                        return true
                    }
                    SingleAction.LPRESS_ADD_BLACK_LIST -> {
                        target.webView.requestFocusNodeHref(WebSrcImageBlackListHandler(controller.activity).obtainMessage())
                        return true
                    }
                    SingleAction.LPRESS_ADD_IMAGE_BLACK_LIST -> {
                        AddAdBlockDialog.addBackListInstance(url)
                                .show(controller.activity.supportFragmentManager, "add black")
                        return true
                    }
                    SingleAction.LPRESS_ADD_WHITE_LIST -> {
                        target.webView.requestFocusNodeHref(WebSrcImageWhiteListHandler(controller.activity).obtainMessage())
                        AddAdBlockDialog.addWhiteListInstance(url)
                                .show(controller.activity.supportFragmentManager, "add white")
                        return true
                    }
                    SingleAction.LPRESS_ADD_IMAGE_WHITE_LIST -> {
                        AddAdBlockDialog.addWhiteListInstance(url).show(controller.activity.supportFragmentManager, "add white")
                        return true
                    }
                    else -> return run(action, target, null)
                }
            }
        }
        return false
    }

    override fun run(action: SingleAction, target: ActionController.TargetInfo?, button: View?): Boolean {
        val actionTarget = if (target != null && target.target >= 0) target.target else controller.tabManager.currentTabNo

        if (actionTarget < 0 || actionTarget >= controller.tabSize) {
            return false
        }

        when (action.id) {
            SingleAction.GO_BACK -> {
                val tab = controller.getTab(actionTarget)
                if (tab.mWebView.canGoBack()) {
                    if (tab.isNavLock) {
                        val item = tab.mWebView.copyMyBackForwardList().prev
                        if (item != null) {
                            performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.url, TabType.WINDOW)
                            return true
                        }
                    }
                    tab.mWebView.goBack()

                    controller.superFrameLayoutInfo.postDelayed(takeCurrentTabScreen, 500)
                    controller.superFrameLayoutInfo.postDelayed(paddingReset, 50)
                } else {
                    checkAndRun((action as GoBackSingleAction).defaultAction, target)
                }
            }
            SingleAction.GO_FORWARD -> {
                val tab = controller.getTab(actionTarget)
                if (tab.mWebView.canGoForward()) {
                    if (tab.isNavLock) {
                        val item = tab.mWebView.copyMyBackForwardList().next
                        if (item != null) {
                            performNewTabLink(BrowserManager.LOAD_URL_TAB_NEW_RIGHT, tab, item.url, TabType.WINDOW)
                            return true
                        }
                    }
                    tab.mWebView.goForward()
                    controller.superFrameLayoutInfo.postDelayed(takeCurrentTabScreen, 500)
                    controller.superFrameLayoutInfo.postDelayed(paddingReset, 50)
                }
            }
            SingleAction.WEB_RELOAD_STOP -> {
                val tab = controller.getTab(actionTarget)
                if (tab.isInPageLoad)
                    tab.mWebView.stopLoading()
                else
                    tab.mWebView.reload()
            }
            SingleAction.WEB_RELOAD -> controller.getTab(actionTarget).mWebView.reload()
            SingleAction.WEB_STOP -> controller.getTab(actionTarget).mWebView.stopLoading()
            SingleAction.GO_HOME -> controller.loadUrl(controller.getTab(actionTarget), AppPrefs.home_page.get())
            SingleAction.ZOOM_IN -> controller.getTab(actionTarget).mWebView.zoomIn()
            SingleAction.ZOOM_OUT -> controller.getTab(actionTarget).mWebView.zoomOut()
            SingleAction.PAGE_UP -> controller.getTab(actionTarget).mWebView.pageUp(false)
            SingleAction.PAGE_DOWN -> controller.getTab(actionTarget).mWebView.pageDown(false)
            SingleAction.PAGE_TOP -> controller.getTab(actionTarget).mWebView.pageUp(true)
            SingleAction.PAGE_BOTTOM -> controller.getTab(actionTarget).mWebView.pageDown(true)
            SingleAction.PAGE_SCROLL -> (action as WebScrollSingleAction).scrollWebView(controller.applicationContextInfo, controller.getTab(actionTarget).mWebView)
            SingleAction.PAGE_FAST_SCROLL -> {
                if (controller.isEnableFastPageScroller) {
                    controller.closeFastPageScroller()
                } else {
                    controller.showFastPageScroller(actionTarget)
                }
            }
            SingleAction.PAGE_AUTO_SCROLL -> {
                if (controller.isEnableAutoScroll) {
                    controller.stopAutoScroll()
                } else {
                    controller.startAutoScroll(actionTarget, action as AutoPageScrollAction)
                }
            }
            SingleAction.FOCUS_UP -> {
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP))
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_UP))
            }
            SingleAction.FOCUS_DOWN -> {
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN))
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_DOWN))
            }
            SingleAction.FOCUS_LEFT -> {
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT))
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT))
            }
            SingleAction.FOCUS_RIGHT -> {
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT))
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT))
            }
            SingleAction.FOCUS_CLICK -> {
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER))
                controller.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER))
            }
            SingleAction.TOGGLE_JS -> {
                val web = controller.getTab(actionTarget).mWebView
                val to = !web.webSettings.javaScriptEnabled
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
                web.webSettings.javaScriptEnabled = to
                web.reload()
            }
            SingleAction.TOGGLE_IMAGE -> {
                val web = controller.getTab(actionTarget).mWebView
                val to = !web.webSettings.loadsImagesAutomatically
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
                web.webSettings.loadsImagesAutomatically = to
                web.reload()
            }
            SingleAction.TOGGLE_COOKIE -> {
                val cookie = !AppPrefs.accept_cookie.get()
                AppPrefs.accept_cookie.set(cookie)
                AppPrefs.commit(controller.applicationContextInfo, AppPrefs.accept_cookie)
                CookieManager.getInstance().setAcceptCookie(cookie)
                Toast.makeText(controller.applicationContextInfo, if (cookie) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
            }
            SingleAction.TOGGLE_USERJS -> {
                val to = !controller.isEnableUserScript
                controller.isEnableUserScript = to
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
            }
            SingleAction.TOGGLE_NAV_LOCK -> {
                val tab = controller.getTab(actionTarget)
                tab.isNavLock = !tab.isNavLock
                tab.invalidateView(actionTarget == controller.currentTabNo, controller.resourcesByInfo, controller.themeByInfo)
                controller.notifyChangeWebState()//icon change
            }
            SingleAction.PAGE_INFO -> {
                val tab = controller.getTab(actionTarget)

                val v = View.inflate(controller.activity, R.layout.page_info_dialog, null)
                val titleTextView: TextView = v.findViewById(R.id.titleTextView)
                val urlTextView: TextView = v.findViewById(R.id.urlTextView)

                titleTextView.text = tab.title
                val url = tab.url
                urlTextView.text = url.decodePunyCodeUrl()
                urlTextView.setOnLongClickListener {
                    controller.applicationContextInfo.setClipboardWithToast(url)
                    true
                }

                AlertDialog.Builder(controller.activity)
                        .setTitle(R.string.page_info)
                        .setView(v)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
            }
            SingleAction.COPY_URL -> controller.applicationContextInfo.setClipboardWithToast(controller.getTab(actionTarget).url)
            SingleAction.COPY_TITLE -> controller.applicationContextInfo.setClipboardWithToast(controller.getTab(actionTarget).title)
            SingleAction.COPY_TITLE_URL -> {
                val tab = controller.getTab(actionTarget)
                val url = tab.url
                val title = tab.title
                when {
                    url == null -> controller.applicationContextInfo.setClipboardWithToast(title)
                    title == null -> controller.applicationContextInfo.setClipboardWithToast(url)
                    else -> controller.applicationContextInfo.setClipboardWithToast("$title $url")
                }
            }
            SingleAction.TAB_HISTORY -> controller.showTabHistory(actionTarget)
            SingleAction.MOUSE_POINTER -> {
                if (controller.isEnableMousePointer) {
                    controller.closeMousePointer()
                } else {
                    controller.showMousePointer((action as MousePointerSingleAction).isBackFinish)
                }
            }
            SingleAction.FIND_ON_PAGE -> {
                if (controller.isEnableFindOnPage) {
                    controller.isEnableFindOnPage = false
                } else {
                    controller.setEnableFindOnPage(true, (action as FindOnPageAction).isAutoClose)
                }
            }
            SingleAction.SAVE_SCREENSHOT -> {
                val saveSsAction = action as SaveScreenshotSingleAction
                val file = File(saveSsAction.folder, "ss_" + System.currentTimeMillis() + ".png")
                val type = saveSsAction.type
                try {
                    when (type) {
                        SaveScreenshotSingleAction.SS_TYPE_ALL -> {
                            if (WebViewUtils.savePictureOverall(controller.getTab(actionTarget).mWebView, file))
                                Toast.makeText(controller.applicationContextInfo, getString(R.string.saved_file) + file.absolutePath, Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(controller.applicationContextInfo, R.string.failed, Toast.LENGTH_SHORT).show()
                            FileUtils.notifyImageFile(controller.applicationContextInfo, file.absolutePath)
                        }
                        SaveScreenshotSingleAction.SS_TYPE_PART -> {
                            if (WebViewUtils.savePicturePart(controller.getTab(actionTarget).mWebView.webView, file))
                                Toast.makeText(controller.applicationContextInfo, getString(R.string.saved_file) + file.absolutePath, Toast.LENGTH_SHORT).show()
                            else
                                Toast.makeText(controller.applicationContextInfo, R.string.failed, Toast.LENGTH_SHORT).show()
                            FileUtils.notifyImageFile(controller.applicationContextInfo, file.absolutePath)
                        }
                        else -> Toast.makeText(controller.applicationContextInfo, "Unknown screenshot type : $type", Toast.LENGTH_LONG).show()
                    }
                } catch (e: IOException) {
                    ErrorReport.printAndWriteLog(e)
                    Toast.makeText(controller.applicationContextInfo, "IOException : " + e.message, Toast.LENGTH_LONG).show()
                }

            }
            SingleAction.SHARE_SCREENSHOT -> {
                val file = File(controller.activity.externalCacheDir, "ss_" + System.currentTimeMillis() + ".png")
                val type = (action as ShareScreenshotSingleAction).type
                try {
                    var result = false
                    when (type) {
                        ShareScreenshotSingleAction.SS_TYPE_ALL -> result = WebViewUtils.savePictureOverall(controller.getTab(actionTarget).mWebView, file)
                        ShareScreenshotSingleAction.SS_TYPE_PART -> result = WebViewUtils.savePicturePart(controller.getTab(actionTarget).mWebView.webView, file)
                        else -> Toast.makeText(controller.applicationContextInfo, "Unknown screenshot type : $type", Toast.LENGTH_LONG).show()
                    }

                    if (result) {
                        val provider = (controller.activity.applicationContext as BrowserApplication)
                                .providerManager.downloadFileProvider
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.type = "image/png"
                        intent.putExtra(Intent.EXTRA_STREAM, provider.getUriForFile(file))
                        try {
                            controller.activity.startActivity(PackageUtils.createChooser(controller.activity, intent, null))
                        } catch (e: ActivityNotFoundException) {
                            e.printStackTrace()
                        }

                    } else {
                        Toast.makeText(controller.applicationContextInfo, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    ErrorReport.printAndWriteLog(e)
                    Toast.makeText(controller.applicationContextInfo, "IOException : " + e.message, Toast.LENGTH_LONG).show()
                }

            }
            SingleAction.SAVE_PAGE -> {
                val tab = controller.getTab(actionTarget)
                SaveWebArchiveDialog(controller.activity, tab.url, tab.mWebView, actionTarget)
                        .show(controller.activity.supportFragmentManager, "saveArchive")
            }
            SingleAction.OPEN_URL -> {
                val openUrlAction = action as OpenUrlSingleAction
                controller.loadUrl(controller.getTab(actionTarget), openUrlAction.url, openUrlAction.targetTab, TabType.WINDOW)
            }
            SingleAction.TRANSLATE_PAGE -> {
                val translateAction = action as TranslatePageSingleAction
                val tab = controller.getTab(actionTarget)
                val from = translateAction.translateFrom
                val to = translateAction.translateTo
                if (TextUtils.isEmpty(to)) {
                    AlertDialog.Builder(controller.activity)
                            .setItems(R.array.translate_language_list) { _, which ->
                                val to1 = controller.resourcesByInfo.getStringArray(R.array.translate_language_values)[which]
                                val url = URLUtil.composeSearchUrl(tab.url, "http://translate.google.com/translate?sl=${from ?: ""}&tl=$to1&u=%s", "%s")
                                controller.loadUrl(tab, url)
                            }
                            .show()
                } else {
                    val url = URLUtil.composeSearchUrl(tab.url, "http://translate.google.com/translate?sl=$from&tl=$to&u=%s", "%s")
                    controller.loadUrl(tab, url)
                }
            }
            SingleAction.NEW_TAB -> controller.openInNewTab(AppPrefs.home_page.get(), TabType.DEFAULT)
            SingleAction.CLOSE_TAB -> if (!controller.removeTab(actionTarget)) {
                checkAndRun((action as CloseTabSingleAction).defaultAction, target)
            }
            SingleAction.CLOSE_ALL -> {
                controller.openInNewTab(AppPrefs.home_page.get(), TabType.DEFAULT)
                for (i in controller.tabManager.lastTabNo - 1 downTo 0) {
                    controller.removeTab(i, false)
                }
            }
            SingleAction.CLOSE_OTHERS -> {
                for (i in controller.tabManager.lastTabNo downTo actionTarget + 1) {
                    controller.removeTab(i, false)
                }
                for (i in actionTarget - 1 downTo 0) {
                    controller.removeTab(i, false)
                }
            }
            SingleAction.CLOSE_AUTO_SELECT -> {
                val type = controller.getTab(actionTarget).tabType

                when (type) {
                    TabType.DEFAULT -> checkAndRun((action as CloseAutoSelectAction).defaultAction, target)
                    TabType.INTENT -> checkAndRun((action as CloseAutoSelectAction).intentAction, target)
                    TabType.WINDOW -> checkAndRun((action as CloseAutoSelectAction).windowAction, target)
                }
            }
            SingleAction.LEFT_TAB -> if (controller.tabManager.isFirst) {
                if ((action as LeftRightTabSingleAction).isTabLoop) {
                    controller.setCurrentTab(controller.tabManager.lastTabNo)
                    controller.toolbarManager.scrollTabRight()
                }
            } else {
                val to = controller.tabManager.currentTabNo - 1
                controller.setCurrentTab(to)
                controller.toolbarManager.scrollTabTo(to)
            }
            SingleAction.RIGHT_TAB -> if (controller.tabManager.isLast) {
                if ((action as LeftRightTabSingleAction).isTabLoop) {
                    controller.setCurrentTab(0)
                    controller.toolbarManager.scrollTabLeft()
                }
            } else {
                val to = controller.tabManager.currentTabNo + 1
                controller.setCurrentTab(to)
                controller.toolbarManager.scrollTabTo(to)
            }
            SingleAction.SWAP_LEFT_TAB -> if (!controller.tabManager.isFirst(actionTarget)) {
                val to = actionTarget - 1
                controller.swapTab(to, actionTarget)
                controller.toolbarManager.scrollTabTo(to)
            }
            SingleAction.SWAP_RIGHT_TAB -> if (!controller.tabManager.isLast(actionTarget)) {
                val to = actionTarget + 1
                controller.swapTab(to, actionTarget)
                controller.toolbarManager.scrollTabTo(to)
            }
            SingleAction.TAB_LIST -> controller.showTabList(action as TabListSingleAction)
            SingleAction.CLOSE_ALL_LEFT -> for (i in actionTarget - 1 downTo 0) {
                controller.removeTab(i, false)
            }
            SingleAction.CLOSE_ALL_RIGHT -> for (i in controller.tabManager.lastTabNo downTo actionTarget + 1) {
                controller.removeTab(i, false)
            }
            SingleAction.RESTORE_TAB -> controller.restoreTab()
            SingleAction.REPLICATE_TAB -> controller.openInNewTab(controller.getTab(actionTarget))
            SingleAction.SHOW_SEARCHBOX -> controller.showSearchBox(
                    controller.getTab(actionTarget).url ?: "",
                    actionTarget,
                    (action as ShowSearchBoxAction).isOpenNewTab,
                    action.isReverse)
            SingleAction.PASTE_SEARCHBOX -> controller.showSearchBox(controller.applicationContextInfo.clipboardText,
                    actionTarget,
                    (action as PasteSearchBoxAction).isOpenNewTab,
                    action.isReverse)
            SingleAction.PASTE_GO -> {
                val text = controller.applicationContextInfo.clipboardText
                if (TextUtils.isEmpty(text)) {
                    Toast.makeText(controller.applicationContextInfo, R.string.clipboard_empty, Toast.LENGTH_SHORT).show()
                    return true
                }
                controller.loadUrl(controller.getTab(actionTarget), text.makeUrlFromQuery(AppPrefs.search_url.get(), "%s"), (action as PasteGoSingleAction).targetTab, TabType.WINDOW)
            }
            SingleAction.SHOW_BOOKMARK -> {
                val intent = Intent(controller.applicationContextInfo, BookmarkActivity::class.java)
                intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                controller.startActivity(intent, BrowserController.REQUEST_BOOKMARK)
            }
            SingleAction.SHOW_HISTORY -> {
                val intent = Intent(controller.applicationContextInfo, BrowserHistoryActivity::class.java)
                intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                controller.startActivity(intent, BrowserController.REQUEST_HISTORY)
            }
            SingleAction.SHOW_DOWNLOADS -> {
                startActivity(Intent(controller.applicationContextInfo, DownloadListActivity::class.java).apply {
                    putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                    putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                })
            }
            SingleAction.SHOW_SETTINGS -> {
                val intent = Intent(controller.applicationContextInfo, MainSettingsActivity::class.java)
                controller.startActivity(intent, BrowserController.REQUEST_SETTING)
            }
            SingleAction.OPEN_SPEED_DIAL -> controller.loadUrl(controller.getTab(actionTarget), "ox:speeddial")
            SingleAction.ADD_BOOKMARK -> controller.addBookmark(controller.getTab(actionTarget))
            SingleAction.ADD_SPEED_DIAL -> {
                val tab = controller.getTab(actionTarget)
                startActivity(Intent(controller.activity, SpeedDialSettingActivity::class.java).also {
                    it.action = SpeedDialSettingActivity.ACTION_ADD_SPEED_DIAL
                    it.putExtra(Intent.EXTRA_TITLE, tab.title)
                    it.putExtra(Intent.EXTRA_TEXT, tab.url)
                    it.putExtra(SpeedDialSettingActivity.EXTRA_ICON, ImageUtils.trimSquare(tab.mWebView.favicon, 200))
                })
            }
            SingleAction.ADD_PATTERN -> {
                val tab = controller.getTab(actionTarget)
                val intent = Intent(controller.activity, PatternUrlActivity::class.java)
                intent.putExtra(Intent.EXTRA_TEXT, tab.url)
                startActivity(intent)
            }
            SingleAction.ADD_TO_HOME -> {
                val tab = controller.getTab(actionTarget)
                tab.mWebView.evaluateJavascript(Scripts.GET_ICON_URL, ValueCallback {
                    val iconUrl = if (it.startsWith('"') && it.endsWith('"')) it.substring(1, it.length - 1) else it
                    createShortCut(tab, iconUrl)
                })
            }
            SingleAction.SUB_GESTURE -> controller.showSubGesture()
            SingleAction.CLEAR_DATA -> ClearBrowserDataAlertDialog(controller.activity).show(controller.activity.supportFragmentManager)
            SingleAction.SHOW_PROXY_SETTING -> ProxySettingDialog(controller.activity).show(controller.activity.supportFragmentManager)
            SingleAction.ORIENTATION_SETTING -> AlertDialog.Builder(controller.activity)
                    .setItems(R.array.pref_oritentation_list
                    ) { _, which -> controller.requestedOrientationByCtrl = controller.resourcesByInfo.getIntArray(R.array.pref_oritentation_values)[which] }
                    .show()
            SingleAction.OPEN_LINK_SETTING -> AlertDialog.Builder(controller.activity)
                    .setItems(R.array.pref_newtab_list
                    ) { _, which -> AppPrefs.newtab_link.set(controller.resourcesByInfo.getIntArray(R.array.pref_newtab_values)[which]) }
                    .show()
            SingleAction.USERAGENT_SETTING -> {
                val uaIntent = Intent(controller.applicationContextInfo, UserAgentListActivity::class.java)
                uaIntent.putExtra(Intent.EXTRA_TEXT, controller.getTab(actionTarget).mWebView.webSettings.userAgentString)
                controller.startActivity(uaIntent, BrowserController.REQUEST_USERAGENT)
            }
            SingleAction.TEXTSIZE_SETTING -> {
                val setting = controller.getTab(actionTarget).mWebView.webSettings
                SeekBarDialog(controller.activity)
                        .setTitle(R.string.pref_text_size)
                        .setSeekMin(1)
                        .setSeekMax(300)
                        .setValue(setting.textZoom)
                        .setPositiveButton(android.R.string.ok) { _, _, value -> setting.textZoom = value }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
            }
            SingleAction.USERJS_SETTING -> controller.startActivity(Intent(controller.applicationContextInfo, UserScriptListActivity::class.java), BrowserController.REQUEST_USERJS_SETTING)
            SingleAction.WEB_ENCODE_SETTING -> {
                val webEncode = Intent(controller.applicationContextInfo, WebTextEncodeListActivity::class.java)
                webEncode.putExtra(Intent.EXTRA_TEXT, controller.getTab(actionTarget).mWebView.webSettings.defaultTextEncodingName)
                controller.startActivity(webEncode, BrowserController.REQUEST_WEB_ENCODE_SETTING)
            }
            SingleAction.DEFALUT_USERAGENT_SETTING -> {
                val uaIntent = Intent(controller.applicationContextInfo, UserAgentListActivity::class.java)
                uaIntent.putExtra(Intent.EXTRA_TEXT, controller.getTab(actionTarget).mWebView.webSettings.userAgentString)
                controller.startActivity(uaIntent, BrowserController.REQUEST_DEFAULT_USERAGENT)
            }
            SingleAction.RENDER_SETTING -> {
                val builder = AlertDialog.Builder(controller.activity)
                val mode = controller.getTab(actionTarget).mWebView.renderingMode
                builder.setTitle(R.string.pref_rendering)
                    .setSingleChoiceItems(R.array.pref_rendering_list, mode) { dialog, which ->
                        dialog.dismiss()
                        controller.applyRenderingMode(controller.getTab(actionTarget), which)
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                builder.create().show()
            }
            SingleAction.RENDER_ALL_SETTING -> {
                val builder = AlertDialog.Builder(controller.activity)
                val mode = controller.defaultRenderingMode
                builder.setTitle(R.string.pref_rendering)
                    .setSingleChoiceItems(R.array.pref_rendering_list, mode) { dialog, which ->
                        dialog.dismiss()
                        controller.defaultRenderingMode = which
                        controller.tabManager.loadedData.forEach {
                            controller.applyRenderingMode(it, which)
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                builder.create().show()
            }
            SingleAction.TOGGLE_VISIBLE_TAB -> controller.toolbarManager.tabBar.toggleVisibility()
            SingleAction.TOGGLE_VISIBLE_URL -> controller.toolbarManager.urlBar.toggleVisibility()
            SingleAction.TOGGLE_VISIBLE_PROGRESS -> controller.toolbarManager.progressBar.toggleVisibility()
            SingleAction.TOGGLE_VISIBLE_CUSTOM -> controller.toolbarManager.customBar.toggleVisibility()
            SingleAction.TOGGLE_WEB_TITLEBAR -> controller.toolbarManager.setWebViewTitleBar(controller.tabManager.currentTabData.mWebView, false)
            SingleAction.TOGGLE_WEB_GESTURE -> controller.isEnableGesture = !controller.isEnableGesture
            SingleAction.TOGGLE_FLICK -> {
                val to = !controller.isEnableFlick
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
                controller.isEnableFlick = to
            }
            SingleAction.TOGGLE_QUICK_CONTROL -> {
                val to = !controller.isEnableQuickControl
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
                controller.isEnableQuickControl = to
            }
            SingleAction.TOGGLE_MULTI_FINGER_GESTURE -> {
                val to = !controller.isEnableMultiFingerGesture
                Toast.makeText(controller.applicationContextInfo, if (to) R.string.toggle_enable else R.string.toggle_disable, Toast.LENGTH_SHORT).show()
                controller.isEnableMultiFingerGesture = to
            }
            SingleAction.TOGGLE_AD_BLOCK -> {
                val to = !controller.isEnableAdBlock
                controller.isEnableAdBlock = to
                controller.getTab(actionTarget).mWebView.reload()
                if ((action as WithToastAction).showToast) {
                    Toast.makeText(controller.applicationContextInfo,
                            if (to) R.string.toggle_enable else R.string.toggle_disable,
                            Toast.LENGTH_SHORT).show()
                }
            }
            SingleAction.OPEN_BLACK_LIST -> startActivity(
                    Intent(controller.activity, AdBlockActivity::class.java)
                            .setAction(AdBlockActivity.ACTION_OPEN_BLACK))
            SingleAction.OPEN_WHITE_LIST -> startActivity(
                    Intent(controller.activity, AdBlockActivity::class.java)
                            .setAction(AdBlockActivity.ACTION_OPEN_WHITE))
            SingleAction.OPEN_WHITE_PATE_LIST -> startActivity(
                    Intent(controller.activity, AdBlockActivity::class.java)
                            .setAction(AdBlockActivity.ACTION_OPEN_WHITE_PAGE))
            SingleAction.ADD_WHITE_LIST_PAGE -> AddAdBlockDialog.addWhitePageListInstance(controller.getTab(actionTarget).url)
                    .show(controller.activity.supportFragmentManager, "add white page")
            SingleAction.SHARE_WEB -> {
                val tab = controller.getTab(actionTarget)
                WebUtils.shareWeb(controller.activity, tab.url, tab.title)
            }
            SingleAction.OPEN_OTHER -> WebUtils.openInOtherApp(controller.activity, controller.getTab(actionTarget).url)
            SingleAction.START_ACTIVITY -> {
                val intent = (action as StartActivitySingleAction).getIntent(controller.getTab(actionTarget))
                if (intent != null) {
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(controller.activity, R.string.app_notfound, Toast.LENGTH_SHORT).show()
                    }

                }
            }
            SingleAction.TOGGLE_FULL_SCREEN -> controller.isFullscreenMode = !controller.isFullscreenMode
            SingleAction.OPEN_OPTIONS_MENU -> controller.showMenu(button, action as OpenOptionsMenuAction)
            SingleAction.CUSTOM_MENU -> {
                val tab = controller.getTab(actionTarget)
                val actionList = (action as CustomMenuSingleAction).actionList

                val builder = AlertDialog.Builder(controller.activity)
                if (target is ActionController.HitTestResultTargetInfo) {
                    builder.setCustomTitle(
                            ContextMenuTitleView(controller.activity, target.result.extra ?: ""))
                            .setAdapter(
                                    ActionListViewAdapter(controller.activity, actionList, target.actionNameArray)
                            ) { _, which -> checkAndRun(actionList[which], target) }
                } else {
                    builder.setCustomTitle(ContextMenuTitleView(controller.activity, tab.url ?: ""))
                            .setAdapter(
                                    ActionListViewAdapter(controller.activity, actionList, null)
                            ) { _, which -> checkAndRun(actionList[which], target) }
                }

                builder.show()
            }
            SingleAction.FINISH -> {
                val finishAction = action as FinishSingleAction
                val closeTabTarget = if (finishAction.isCloseTab) actionTarget else -1
                if (finishAction.isShowAlert)
                    controller.finishAlert(closeTabTarget)
                else
                    controller.finishQuick(closeTabTarget)
            }
            SingleAction.MINIMIZE -> controller.moveTaskToBack(true)
            SingleAction.CUSTOM_ACTION -> return if (target is ActionController.HitTestResultTargetInfo) {
                run((action as CustomSingleAction).action, target)
            } else {
                run((action as CustomSingleAction).action)
            }
            SingleAction.VIBRATION -> {
                val vibrator = controller.activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(
                            (action as VibrationSingleAction).time.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate((action as VibrationSingleAction).time.toLong())
                }
            }
            SingleAction.TOAST -> Toast.makeText(controller.applicationContextInfo, (action as ToastAction).text, Toast.LENGTH_SHORT).show()
            SingleAction.PRIVATE -> {
                val privateMode = !controller.isPrivateMode
                controller.isPrivateMode = privateMode
                if ((action as WithToastAction).showToast) {
                    Toast.makeText(controller.applicationContextInfo,
                            if (privateMode) R.string.toggle_enable else R.string.toggle_disable,
                            Toast.LENGTH_SHORT).show()
                }
            }
            SingleAction.VIEW_SOURCE -> {
                val webView = controller.getTab(actionTarget).mWebView
                controller.openInCurrentTab("view-source:" + webView.url)
            }
            SingleAction.PRINT -> if (PrintHelper.systemSupportsPrint()) {
                val tab = controller.getTab(actionTarget)
                var title = tab.title
                if (TextUtils.isEmpty(title))
                    title = tab.mWebView.title
                if (TextUtils.isEmpty(title))
                    title = "document"
                var jobName = tab.url
                if (TextUtils.isEmpty(jobName))
                    jobName = "about:blank"
                val adapter = tab.mWebView.createPrintDocumentAdapter(title)
                if (adapter == null) {
                    controller.activity.toast(R.string.failed)
                } else {
                    controller.printManager.print(jobName, adapter, null)
                }
            } else {
                Toast.makeText(controller.activity, R.string.print_not_support, Toast.LENGTH_SHORT).show()
            }
            SingleAction.TAB_PINNING -> {
                val tab = controller.getTab(actionTarget)
                tab.isPinning = !tab.isPinning
                tab.invalidateView(actionTarget == controller.tabManager.currentTabNo, controller.resourcesByInfo, controller.themeByInfo)
                controller.toolbarManager.notifyChangeWebState()//icon change
            }
            SingleAction.ALL_ACTION -> controller.startActivity(
                    ActionActivity.Builder(controller.activity)
                            .setTitle(R.string.action_list)
                            .create()
                            .setAction(ActionActivity.ACTION_ALL_ACTION)
                            .putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                            .putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl),
                    BrowserController.REQUEST_ACTION_LIST)
            SingleAction.READER_MODE -> {
                val tab = controller.getTab(actionTarget)
                val intent = Intent(controller.activity, ReaderActivity::class.java)
                intent.putExtra(Constants.intent.EXTRA_URL, tab.originalUrl)
                intent.putExtra(Constants.intent.EXTRA_USER_AGENT, tab.mWebView.webSettings.userAgentString)
                intent.putExtra(Constants.intent.EXTRA_MODE_FULLSCREEN, controller.isFullscreenMode && DisplayUtils.isNeedFullScreenFlag())
                intent.putExtra(Constants.intent.EXTRA_MODE_ORIENTATION, controller.requestedOrientationByCtrl)
                startActivity(intent)
            }
            SingleAction.READ_IT_LATER -> controller.savePage(controller.getTab(actionTarget))
            SingleAction.READ_IT_LATER_LIST -> startActivity(Intent(controller.activity, ReadItLaterActivity::class.java))
            else -> {
                Toast.makeText(controller.applicationContextInfo, "Unknown action:" + action.id, Toast.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    private fun performNewTabLink(perform: Int, tab: MainTabData, url: String, @TabType type: Int): Boolean {
        when (perform) {
            BrowserManager.LOAD_URL_TAB_CURRENT -> {
                controller.loadUrl(tab, url)
                return true
            }
            BrowserManager.LOAD_URL_TAB_NEW -> {
                controller.openInNewTab(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG -> {
                controller.openInBackground(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_NEW_RIGHT -> {
                controller.openInRightNewTab(url, type)
                return true
            }
            BrowserManager.LOAD_URL_TAB_BG_RIGHT -> {
                controller.openInRightBgTab(url, type)
                return true
            }
            else -> throw IllegalArgumentException("Unknown perform:$perform")
        }
    }

    private val takeCurrentTabScreen = Runnable {
        val data = controller.tabManager.currentTabData
        if (data != null && data.isShotThumbnail)
            controller.tabManager.forceTakeThumbnail(data)
    }

    private val paddingReset = Runnable {
        controller.adjustBrowserPadding(controller.tabManager.currentTabData)
    }

    private fun getString(id: Int): String = controller.activity.getString(id)

    private fun startActivity(intent: Intent) = controller.activity.startActivity(intent)

    private fun createShortCut(tab: MainTabData, iconUrl: String) = ui {
        val bitmap = if (iconUrl.isEmpty() || iconUrl == "null") {
            faviconManager[tab.originalUrl]
        } else {
            val userAgent = tab.mWebView.getUserAgent()
            withContext(Dispatchers.Default) {
                controller.okHttpClient
                    .getImage(iconUrl, userAgent, tab.url, CookieManager.getInstance().getCookie(tab.url))
            }
                ?: faviconManager[tab.originalUrl]
        }

        PackageUtils.createShortcut(controller.activity, tab.title, tab.url, bitmap)
    }
}
