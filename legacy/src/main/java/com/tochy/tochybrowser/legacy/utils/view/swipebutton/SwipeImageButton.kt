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

package com.tochy.tochybrowser.legacy.utils.view.swipebutton

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.tochy.tochybrowser.core.utility.extensions.convertDpToPx
import com.tochy.tochybrowser.legacy.R
import com.tochy.tochybrowser.legacy.action.manager.ActionController
import com.tochy.tochybrowser.legacy.action.manager.ActionIconManager
import com.tochy.tochybrowser.legacy.action.manager.SoftButtonActionFile
import com.tochy.tochybrowser.ui.settings.AppPrefs
import com.tochy.tochybrowser.ui.theme.ThemeData
import com.tochy.tochybrowser.ui.widget.swipebutton.OverlayImageButton
import com.tochy.tochybrowser.ui.widget.swipebutton.SmallButtonDrawable
import com.tochy.tochybrowser.ui.widget.swipebutton.SwipeController

class SwipeImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : OverlayImageButton(context, attrs), SwipeController.OnChangeListener {
    private val mController = SwipeSoftButtonController(getContext().applicationContext)
    private val smallIconSize = context.convertDpToPx(12)

    init {
        scaleType = ScaleType.CENTER_INSIDE
    }

    fun setActionData(action_list: SoftButtonActionFile, controller: ActionController, iconManager: ActionIconManager) {
        mController.setActionData(action_list, controller, iconManager)
        mController.setOnChangeListener(this)
        setImageDrawable(mController.defaultIcon)
        setOverlayIcon(AppPrefs.toolbar_small_icon.get())
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
    }

    fun setToDefault() {
        mController.setToDefault()
    }

    fun notifyChangeState() {
        mController.notifyChangeState()
        setOverlayIcon(AppPrefs.toolbar_small_icon.get())
    }

    fun setSense(sense: Int) {
        mController.setSense(sense)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mController.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onLongPress() {}

    override fun onEventOutSide(): Boolean {
        setImageDrawable(mController.defaultIcon)
        overlay?.setVisible(true, false)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventCancel(): Boolean {
        setImageDrawable(mController.defaultIcon)
        overlay?.setVisible(true, false)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventActionUp(whatNo: Int): Boolean {
        setImageDrawable(mController.defaultIcon)
        overlay?.setVisible(true, false)
        setBackgroundResource(R.drawable.swipebtn_image_background_normal)
        return false
    }

    override fun onEventActionDown(): Boolean {
        val themeData = ThemeData.getInstance()
        if (themeData?.toolbarButtonBackgroundPress != null) {
            background = themeData.toolbarButtonBackgroundPress
        } else {
            setBackgroundResource(R.drawable.swipebtn_image_background_pressed)
        }
        return false
    }

    override fun onChangeState(whatNo: Int) {
        setImageDrawable(mController.icon)
        overlay?.setVisible(whatNo == SwipeController.SWIPE_PRESS || whatNo == SwipeController.SWIPE_CANCEL, false)
    }

    private fun setOverlayIcon(show: Boolean) {
        val icon = if (show) mController.getIcon(SwipeController.SWIPE_LPRESS) else null

        overlay = if (icon != null) SmallButtonDrawable(icon, smallIconSize) else null
    }
}
