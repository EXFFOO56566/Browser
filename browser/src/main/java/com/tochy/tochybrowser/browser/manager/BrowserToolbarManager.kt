

package com.tochy.tochybrowser.browser.manager

import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.appbar.AppBarLayout
import com.tochy.tochybrowser.core.utility.extensions.getResColor
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.action.manager.ActionController
import com.tochy.tochybrowser.legacy.action.manager.ActionIconManager
import com.tochy.tochybrowser.legacy.tab.manager.MainTabData
import com.tochy.tochybrowser.legacy.toolbar.SubToolbar
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM_ALWAYS
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_BOTTOM_OVERLAY_ALWAYS
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_FIXED_WEB
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_FLOAT_BOTTOM
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_LEFT
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_RIGHT
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_TOP
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_TOP_ALWAYS
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_UNDEFINED
import com.tochy.tochybrowser.legacy.toolbar.ToolbarManager.Companion.LOCATION_WEB
import com.tochy.tochybrowser.legacy.toolbar.main.*
import com.tochy.tochybrowser.legacy.utils.view.tab.TabLayout
import com.tochy.tochybrowser.ui.settings.AppPrefs
import com.tochy.tochybrowser.ui.theme.ThemeData
import com.tochy.tochybrowser.webview.CustomWebView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.browser_activity.*

open class BrowserToolbarManager(context: Context, override val containerView: View, controller: ActionController, iconManager: ActionIconManager, requestCallback: RequestCallback) : ToolbarManager, LayoutContainer {
    override val tabBar = TabBar(context, controller, iconManager, requestCallback)
    override val urlBar = if (AppPrefs.toolbar_url_box.get()) WhiteUrlBar(context, controller, iconManager, requestCallback) else UrlBar(context, controller, iconManager, requestCallback)
    override val progressBar = ProgressToolBar(context, requestCallback)
    override val customBar = CustomToolbar(context, controller, iconManager, requestCallback)
    private var mIsWebToolbarCombined = false

    override val findOnPage: View
        get() = find
    override val bottomToolbarAlwaysLayout: LinearLayout
        get() = bottomAlwaysToolbar


    private val webToolbarLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }
    private val fixedWebToolbarLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(0xff000000.toInt())
    }

    companion object {
        private val TOOLBAR_LAYOUT_PARAMS = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
    }

    init {
        bottomAlwaysToolbar.background =
                ColorDrawable(context.getResColor(R.color.deep_gray))

        bottomAlwaysOverlayToolbarPadding.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            bottomAlwaysOverlayToolbarPadding.height = bottom - top
        }
    }

    override fun addToolbarView(isPortrait: Boolean) {
        for (i in 1..4) {
            addSingleToolbarView(i, tabBar, isPortrait)
            addSingleToolbarView(i, urlBar, isPortrait)
            addSingleToolbarView(i, progressBar, isPortrait)
            addSingleToolbarView(i, customBar, isPortrait)
        }
    }

    private fun addSingleToolbarView(priority: Int, toolbar: ToolbarBase, isPortrait: Boolean) {
        val toolbarPreference = toolbar.toolbarPreferences
        var toolbarPriority: Int
        if (isPortrait) {
            toolbarPriority = toolbarPreference.location_priority.get()
        } else {
            toolbarPriority = toolbarPreference.location_landscape_priority.get()
            if (toolbarPriority < 0)
                toolbarPriority = toolbarPreference.location_priority.get()
        }
        if (toolbarPriority != priority)
            return

        var location: Int
        if (isPortrait) {
            location = toolbarPreference.location.get()
        } else {
            location = toolbarPreference.location_landscape.get()
            if (location == LOCATION_UNDEFINED)
                location = toolbarPreference.location.get()
        }

        when (location) {
            LOCATION_TOP -> topToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM, LOCATION_FLOAT_BOTTOM -> bottomOverlayToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_WEB -> webToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_FIXED_WEB -> fixedWebToolbarLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_LEFT -> {
                (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.VERTICAL
                leftToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            }
            LOCATION_RIGHT -> {
                (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.VERTICAL
                rightToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            }
            LOCATION_TOP_ALWAYS -> topAlwaysToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM_ALWAYS -> bottomToolbarAlwaysLayout.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            LOCATION_BOTTOM_OVERLAY_ALWAYS -> bottomAlwaysOverlayToolbar.addView(toolbar, TOOLBAR_LAYOUT_PARAMS)
            else -> throw IllegalStateException("Unknown location:" + toolbar.toolbarPreferences.location.get())
        }
    }

    override fun changeCurrentTab(to_id: Int, from: MainTabData?, to: MainTabData?) {
        tabBar.changeCurrentTab(to_id)

        from?.mWebView?.setEmbeddedTitleBarMethod(null)
        if (to == null) return
        setWebViewTitleBar(to.mWebView, !to.isInPageLoad)

        notifyChangeWebState(to)
    }

    override fun moveCurrentTabPosition(id: Int) = tabBar.changeCurrentTab(id)

    override fun swapTab(a: Int, b: Int) = tabBar.swapTab(a, b)

    override fun moveTab(from: Int, to: Int, newCurrent: Int) = tabBar.moveTab(from, to, newCurrent)

    override fun onPreferenceReset() {
        tabBar.onPreferenceReset()
        urlBar.onPreferenceReset()
        progressBar.onPreferenceReset()
        customBar.onPreferenceReset()

        val params = topToolbar.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = if (AppPrefs.snap_toolbar.get()) {
            (AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                    or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP)
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        }
        bottomOverlayToolbar.background.alpha = AppPrefs.overlay_bottom_alpha.get()
        bottomAlwaysOverlayToolbar.background.alpha = AppPrefs.overlay_bottom_alpha.get()
    }

    override fun onThemeChanged(themeData: ThemeData?) {
        tabBar.onThemeChanged(themeData)
        urlBar.onThemeChanged(themeData)
        progressBar.onThemeChanged(themeData)
        customBar.onThemeChanged(themeData)

        if (themeData != null && themeData.toolbarBackgroundColor != 0) {
            topToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomOverlayToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomAlwaysOverlayToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            webToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            fixedWebToolbarLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
            leftToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            rightToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            topAlwaysToolbar.setBackgroundColor(themeData.toolbarBackgroundColor)
            bottomToolbarAlwaysLayout.setBackgroundColor(themeData.toolbarBackgroundColor)
        } else {
            topToolbar.setBackgroundResource(R.color.deep_gray)
            bottomOverlayToolbar.setBackgroundResource(R.color.deep_gray)
            bottomAlwaysOverlayToolbar.setBackgroundResource(R.color.deep_gray)
            webToolbarLayout.setBackgroundResource(R.color.deep_gray)
            fixedWebToolbarLayout.setBackgroundResource(R.color.deep_gray)
            leftToolbar.setBackgroundResource(R.color.deep_gray)
            rightToolbar.setBackgroundResource(R.color.deep_gray)
            topAlwaysToolbar.setBackgroundResource(R.color.deep_gray)
            bottomToolbarAlwaysLayout.setBackgroundResource(R.color.deep_gray)
        }

        bottomOverlayToolbar.background.alpha = AppPrefs.overlay_bottom_alpha.get()
        bottomAlwaysOverlayToolbar.background.alpha = AppPrefs.overlay_bottom_alpha.get()

        for (i in 0 until bottomToolbarAlwaysLayout.childCount) {
            (bottomOverlayToolbar.getChildAt(i) as? SubToolbar)?.run {
                applyTheme(themeData)
            }
        }
    }

    override fun onActivityConfigurationChanged(config: Configuration) {
        onActivityConfigurationChangedSingle(tabBar, config)
        onActivityConfigurationChangedSingle(urlBar, config)
        onActivityConfigurationChangedSingle(progressBar, config)
        onActivityConfigurationChangedSingle(customBar, config)

        addToolbarView(config.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    private fun onActivityConfigurationChangedSingle(toolbar: ToolbarBase, config: Configuration) {
        val parent = toolbar.parent as? ViewGroup ?: throw NullPointerException()
        if (parent === leftToolbar || parent === rightToolbar)
            (toolbar.findViewById<View>(R.id.linearLayout) as LinearLayout).orientation = LinearLayout.HORIZONTAL
        parent.removeView(toolbar)
        toolbar.onActivityConfigurationChanged(config)
    }

    override fun onFullscreenChanged(isFullscreen: Boolean) {
        tabBar.onFullscreenChanged(isFullscreen)
        urlBar.onFullscreenChanged(isFullscreen)
        progressBar.onFullscreenChanged(isFullscreen)
        customBar.onFullscreenChanged(isFullscreen)
    }

    override fun onImeChanged(isShown: Boolean) {
        bottomOverlayLayout.visibility = if (isShown) View.GONE else View.VISIBLE
    }

    override fun notifyChangeProgress(data: MainTabData) = progressBar.changeProgress(data)

    override fun notifyChangeWebState(data: MainTabData?) {
        if (data != null)
            setWebViewTitleBar(data.mWebView, !data.isInPageLoad)

        tabBar.notifyChangeWebState(data)
        urlBar.notifyChangeWebState(data)
        progressBar.notifyChangeWebState(data)
        customBar.notifyChangeWebState(data)
    }

    override fun resetToolBar() {
        tabBar.resetToolBar()
        urlBar.resetToolBar()
        customBar.resetToolBar()
    }

    override fun addNewTabView() = tabBar.addNewTabView()

    override fun scrollTabRight() = tabBar.fullScrollRight()

    override fun scrollTabLeft() = tabBar.fullScrollLeft()

    override fun scrollTabTo(position: Int) = tabBar.scrollToPosition(position)

    override fun removeTab(no: Int) = tabBar.removeTab(no)

    override fun addTab(id: Int, view: View) {
        tabBar.addTab(id, view)
    }

    override fun setWebViewTitleBar(web: CustomWebView, combine: Boolean) {
        if (fixedWebToolbarLayout.parent is ViewGroup) {
            (fixedWebToolbarLayout.parent as ViewGroup).removeView(fixedWebToolbarLayout)
        }
        if (combine) {
            if (!mIsWebToolbarCombined) {
                topToolbar.removeView(webToolbarLayout)
                fixedWebToolbarLayout.addView(webToolbarLayout, 0)
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout)
        } else {
            if (mIsWebToolbarCombined) {
                fixedWebToolbarLayout.removeView(webToolbarLayout)
                topToolbar.addView(webToolbarLayout)
            }
            web.setEmbeddedTitleBarMethod(fixedWebToolbarLayout)
        }
        mIsWebToolbarCombined = combine
    }

    override fun setOnTabClickListener(l: TabLayout.OnTabClickListener) {
        tabBar.setOnTabClickListener(l)
    }

    override fun showGeolocationPermissionPrompt(view: View) {
        bottomAlwaysOverlayToolbar.addView(view, 0)
    }

    override fun hideGeolocationPermissionPrompt(view: View) {
        bottomAlwaysOverlayToolbar.removeView(view)
    }

    override fun isContainsWebToolbar(ev: MotionEvent): Boolean {
        val rc = Rect()
        fixedWebToolbarLayout.getGlobalVisibleRect(rc)
        return rc.contains(ev.rawX.toInt(), ev.rawY.toInt())
    }

    override fun onWebViewScroll(web: CustomWebView, e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float) {
        if (topToolbar.height == 0 && web.isScrollable) {
            val translationY = bottomOverlayLayout.translationY
            val newTrans: Float

            newTrans = when {
                distanceY < 0 -> Math.max(0f, distanceY + translationY) //down
                distanceY > 0 -> Math.min(bottomOverlayToolbar.height.toFloat(), distanceY + translationY) //up
                else -> return
            }

            bottomOverlayLayout.translationY = newTrans
        }
    }

    override fun onWebViewTapUp() {
        if (topToolbar.height == 0 && AppPrefs.snap_toolbar.get()) {
            val trans = bottomOverlayLayout.translationY
            val animator: ObjectAnimator
            var duration: Int
            val bottomBarHeight = bottomOverlayToolbar.height
            if (trans > bottomBarHeight / 2) {
                animator = ObjectAnimator.ofFloat(bottomOverlayLayout, "translationY", trans, bottomBarHeight.toFloat())
                duration = (((bottomBarHeight - trans) / bottomBarHeight + 1) * 150).toInt()
            } else {
                animator = ObjectAnimator.ofFloat(bottomOverlayLayout, "translationY", trans, 0f)
                duration = ((trans / bottomBarHeight + 1) * 150).toInt()
            }
            if (duration < 0) {
                duration = 0
            }
            animator.duration = duration.toLong()
            animator.start()
        }
    }
}
