

package com.tochy.tochybrowser.bookmark.overflow

import androidx.annotation.IntDef

object MenuType {
    const val SITE = 0
    const val FOLDER = 1
}

@IntDef(MenuType.SITE, MenuType.FOLDER)
annotation class HideMenuType

