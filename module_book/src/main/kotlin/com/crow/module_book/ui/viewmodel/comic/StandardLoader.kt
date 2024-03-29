package com.crow.module_book.ui.viewmodel.comic

import android.content.Context
import com.crow.module_book.R
import com.crow.module_book.model.entity.comic.reader.ReaderContent
import com.crow.module_book.model.entity.comic.reader.ReaderPrevNextInfo
import com.crow.module_book.model.resp.comic_page.Content

/**
 * ● StandardLoader
 *
 * ● 2024/3/9 00:47
 * @author crowforkotlin
 * @formatter:on
 */
object StandardLoader {

    fun obtaintStandrdPages(context: Context, reader: ReaderContent) : MutableList<Any> {
        val pages: MutableList<Any> = reader.mPages.toMutableList()
        val pageSize = pages.size
        val prevUUID = reader.mChapterInfo.mPrevUUID
        val nextUUID = reader.mChapterInfo.mNextUUID
        val prevInfo = if (prevUUID == null) context.getString(R.string.book_no_prev) else context.getString(R.string.book_prev)
        val nextInfo = if (nextUUID == null) context.getString(R.string.book_no_next) else context.getString(R.string.book_next)
        val chapterID = (pages.first() as Content).mChapterID
        pages.add(0, ReaderPrevNextInfo(
            mChapterID = chapterID,
            mChapterPagePos = 1,
            mUuid = prevUUID,
            mInfo = prevInfo,
            mIsNext = false
        ))
        pages.add(
            ReaderPrevNextInfo(
            mChapterID = chapterID,
            mChapterPagePos = pageSize,
            mUuid = nextUUID,
            mInfo = nextInfo,
            mIsNext = true
        ) )
        return pages
    }
}