package com.wafflytime.common

data class CursorPage<T>(
    val contents: List<T>,
    val page: Long? = null,
    val cursor: Long? = null,
    val size: Long,
    val isLast: Boolean
) {

    companion object {
        fun <T> of(contents: List<T>, page: Long? = null, cursor: Long? = null, size: Long, requestSize :Long) : CursorPage<T> {
            return CursorPage(contents=contents, page=page, cursor=cursor, size=size, isLast = size < requestSize)
        }
    }

    inline fun <R> map(transform: (T) -> R): CursorPage<R> {
        return CursorPage(
            contents.map { transform(it) },
            page,
            cursor,
            size,
            isLast
        )
    }
}

data class DoubleCursorPage<T>(
    val contents: List<T>,
    val page: Long? = null,
    val cursor: Pair<Long, Long>? = null,
    val size: Long,
    val isLast: Boolean
) {
    companion object {
        fun <T> of(contents: List<T>, page: Long? = null, cursor: Pair<Long, Long>? = null, size: Long, requestSize :Long) : DoubleCursorPage<T> {
            return DoubleCursorPage(contents=contents, page=page, cursor=cursor, size=size, isLast = size < requestSize)
        }
    }

    inline fun <R> map(transform: (T) -> R): DoubleCursorPage<R> {
        return DoubleCursorPage(
            contents.map { transform(it) },
            page,
            cursor,
            size,
            isLast
        )
    }
}