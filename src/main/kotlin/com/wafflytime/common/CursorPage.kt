package com.wafflytime.common

data class CursorPage<T>(
    val contents: List<T>,
    val cursor: Long?,
    val size: Long,
) {

    inline fun <R> map(transform: (T) -> R): CursorPage<R> {
        return CursorPage(
            contents.map { transform(it) },
            cursor,
            size,
        )
    }
}

data class DoubleCursorPage<T>(
    val contents: List<T>,
    val cursor: Pair<Long, Long>?,
    val size: Long,
) {

    inline fun <R> map(transform: (T) -> R): DoubleCursorPage<R> {
        return DoubleCursorPage(
            contents.map { transform(it) },
            cursor,
            size,
        )
    }
}