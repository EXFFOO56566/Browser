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

package com.tochy.tochybrowser.browser.view

import android.content.Context
import android.content.res.Configuration
import android.view.MotionEvent
import android.view.View
import com.tochy.tochybrowser.browser.manager.BrowserToolbarManager
import com.tochy.tochybrowser.legacy.action.manager.ActionController
import com.tochy.tochybrowser.legacy.action.manager.ActionIconManager
import com.tochy.tochybrowser.legacy.action.manager.TabActionManager
import com.tochy.tochybrowser.legacy.browser.BrowserController
import com.tochy.tochybrowser.legacy.tab.manager.MainTabData
import com.tochy.tochybrowser.legacy.toolbar.main.RequestCallback
import com.tochy.tochybrowser.legacy.utils.view.tab.TabLayout
import com.tochy.tochybrowser.ui.settings.container.ToolbarVisibilityContainer

class Toolbar(context: Context, root: View, private val controller: BrowserController, private val actionController: ActionController, iconManager: ActionIconManager) : BrowserToolbarManager(context, root, actionController, iconManager, object : RequestCallback {
    override fun shouldShowToolbar(visibility: ToolbarVisibilityContainer, tabData: MainTabData?, config: Configuration?): Boolean {
        if (!visibility.isVisible)
            return false

        if (controller.isFullscreenMode && visibility.isHideWhenFullscreen)
            return false

        val configuration = config ?: controller.resourcesByInfo.configuration
        val orientation = configuration.orientation
        if (visibility.isHideWhenPortrait && orientation == Configuration.ORIENTATION_PORTRAIT)
            return false

        if (visibility.isHideWhenLandscape && orientation == Configuration.ORIENTATION_LANDSCAPE)
            return false

        if (visibility.isHideWhenLayoutShrink && controller.isImeShown)
            return false

        val tab = tabData ?: controller.currentTabData ?: return visibility.isVisible

        return !(visibility.isHideWhenEndLoading && !tab.isInPageLoad)
    }
}), TabLayout.OnTabClickListener {

    private val tabActionManager = TabActionManager.getInstance(controller.applicationContextInfo)
    private val targetInfoCache = ActionController.TargetInfo()

    init {
        setOnTabClickListener(this)
    }

    override fun onTabTouch(v: View?, ev: MotionEvent?, id: Int, selected: Boolean) = false

    override fun onTabDoubleClick(id: Int) {
        targetInfoCache.target = id
        actionController.run(tabActionManager.tab_press.action, targetInfoCache)
    }

    override fun onTabChangeClick(from: Int, to: Int) {
        controller.setCurrentTab(to)
    }

    override fun onTabLongClick(id: Int) {
        targetInfoCache.target = id
        actionController.run(tabActionManager.tab_lpress.action, targetInfoCache)
    }

    override fun onChangeCurrentTab(from: Int, to: Int) {
        val fromTab = controller.getTabOrNull(from)
        val toTab = controller.getTabOrNull(to)

        val res = controller.resourcesByInfo
        val theme = controller.themeByInfo

        fromTab?.onMoveTabToBackground(res, theme)
        toTab?.onMoveTabToForeground(res, theme)
    }

    override fun onTabSwipeUp(id: Int) {
        targetInfoCache.target = id
        actionController.run(tabActionManager.tab_up.action, targetInfoCache)
    }

    override fun onTabSwipeDown(id: Int) {
        targetInfoCache.target = id
        actionController.run(tabActionManager.tab_down.action, targetInfoCache)
    }
}
