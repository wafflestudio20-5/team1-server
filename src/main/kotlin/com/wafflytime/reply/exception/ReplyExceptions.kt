package com.wafflytime.reply.exception

import com.wafflytime.exception.ReplyException
import org.springframework.http.HttpStatus

open class Reply400(msg: String, errorCode: Int) : ReplyException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Reply401(msg: String, errorCode: Int) : ReplyException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Reply403(msg: String, errorCode: Int) : ReplyException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Reply404(msg: String, errorCode: Int) : ReplyException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Reply409(msg: String, errorCode: Int) : ReplyException(msg, errorCode, HttpStatus.CONFLICT)

object ReplyNotFound : Reply404("해당 id의 댓글이 존재하지 않습니다", 0)
object PostReplyMismatch : Reply400("해당 게시물에 속한 댓글이 아닙니다", 1)
object ReplyDeleted : Reply400("해당 댓글은 이미 삭제되었습니다", 2)
object WriterAnonymousFixed : Reply400("익명 여부를 변경할 수 없습니다", 3)
object ForbiddenReplyRemoval : Reply403("해당 댓글을 삭제할 권한이 없습니다", 4)
object ForbiddenReplyUpdate : Reply403("해당 댓글을 수정할 권한이 없습니다", 5)
object AlreadyLiked : Reply400("이미 공감한 댓글입니다", 6)
object ForbiddenLike : Reply400("댓글 작성자는 공감할 수 없습니다", 7)