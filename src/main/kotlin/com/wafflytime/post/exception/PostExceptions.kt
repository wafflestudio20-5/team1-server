package com.wafflytime.post.exception

import com.wafflytime.exception.PostException
import org.springframework.http.HttpStatus

open class Post400(msg: String, errorCode: Int) : PostException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Post401(msg: String, errorCode: Int) : PostException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Post403(msg: String, errorCode: Int) : PostException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Post404(msg: String, errorCode: Int) : PostException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Post409(msg: String, errorCode: Int) : PostException(msg, errorCode, HttpStatus.CONFLICT)

object TitleRequired : Post400("게시글의 제목을 작성해야하는 게시판입니다", 0)
object TitleNotRequired : Post400("게시글에 제목이 존재하지 않는 게시판입니다", 1)
object AnonymousNotAllowed : Post400("익명으로 게시글을 작성할 수 없는 게시판입니다", 2)
object ForbiddenPostRemoval : Post403("해당 게시물을 삭제할 권한이 없습니다", 3)
object ForbiddenPostUpdate : Post403("해당 게시물을 수정할 권한이 없습니다", 4)
object PostNotFound : Post404("해당 id의 게시물이 존재하지 않습니다", 5)
object BoardPostMismatch : Post400("해당 게시판에 속한 게시물이 아닙니다", 6)
object AlreadyLiked : Post400("이미 공감한 게시물입니다", 7)
object AlreadyScrapped : Post400("이미 스크랩한 게시물입니다", 8)
object ForbiddenLikeScrap : Post400("게시물 작성자는 공감/스크랩할 수 없습니다", 9)