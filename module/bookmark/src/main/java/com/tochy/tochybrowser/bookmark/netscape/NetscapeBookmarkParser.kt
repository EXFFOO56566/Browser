

package com.tochy.tochybrowser.bookmark.netscape

import android.graphics.BitmapFactory
import android.util.Base64
import com.tochy.tochybrowser.bookmark.item.BookmarkFolder
import com.tochy.tochybrowser.bookmark.item.BookmarkSite
import com.tochy.tochybrowser.bookmark.util.BookmarkIdGenerator
import com.tochy.tochybrowser.favicon.FaviconManager
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.DocumentType
import org.jsoup.nodes.Element
import java.io.File
import java.io.IOException

class NetscapeBookmarkParser(
    private val parent: BookmarkFolder,
    private val favicon: FaviconManager) {

    @Throws(NetscapeBookmarkException::class, IOException::class)
    fun parse(file: File) {
        if (!file.name.endsWith(".html", true) && !file.name.endsWith(".htm", true)) {
            throw NetscapeBookmarkException()
        }
        val document = Jsoup.parse(file, "UTF-8")
        document.getElementsByTag("p").remove()
        if (!checkDocType(document)) {
            throw NetscapeBookmarkException()
        }

        val itemRoot = document.body() ?: throw NetscapeBookmarkException()

        parseItem(itemRoot.children().firstOrNull { it.tagName() == "dl" } ?: throw NetscapeBookmarkException(), parent)
    }

    private fun parseItem(element: Element, parent: BookmarkFolder) {
        element.children().forEach { child ->
            if (child.tagName() == "dt") {
                val children = child.children()
                val folderNode = children.firstOrNull { it.tagName() == "h3" }
                if (folderNode != null) {
                    val folder = BookmarkFolder(folderNode.text(), parent, BookmarkIdGenerator.getNewId())
                    parent.add(folder)
                    parseItem(children.firstOrNull { it.tagName() == "dl" }
                            ?: throw NetscapeBookmarkException(), folder)
                    return@forEach
                }
                val item = children.firstOrNull { it.tagName() == "a" }
                if (item != null) {
                    val url = item.attr("href")
                    if (url.isNotEmpty()) {
                        parent.add(BookmarkSite(item.text(), url, BookmarkIdGenerator.getNewId()))
                        val icon = item.attr("icon")
                        val index = icon.indexOf(",")
                        if (icon.isNotEmpty() && icon.startsWith("data:") && index > -1) {
                            try {
                                val byte = Base64.decode(icon.substring(index), Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(byte, 0, byte.size)
                                favicon[url] = bitmap
                            } catch (e: OutOfMemoryError) {
                                System.gc()
                            }
                        }
                    }
                }
            }
        }

    }

    private fun checkDocType(doc: Document): Boolean {
        return "<!DOCTYPE netscape-bookmark-file-1>".equals(doc.childNodes().firstOrNull { it is DocumentType }?.toString(), true)
    }
}
