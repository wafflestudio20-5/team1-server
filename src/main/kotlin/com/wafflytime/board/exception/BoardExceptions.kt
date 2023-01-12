package com.wafflytime.board.exception

import com.wafflytime.exception.BoardException
import org.springframework.http.HttpStatus

open class Board400(msg: String, errorCode: Int) : BoardException(msg, errorCode, HttpStatus.BAD_REQUEST)
open class Board401(msg: String, errorCode: Int) : BoardException(msg, errorCode, HttpStatus.UNAUTHORIZED)
open class Board403(msg: String, errorCode: Int) : BoardException(msg, errorCode, HttpStatus.FORBIDDEN)
open class Board404(msg: String, errorCode: Int) : BoardException(msg, errorCode, HttpStatus.NOT_FOUND)
open class Board409(msg: String, errorCode: Int) : BoardException(msg, errorCode, HttpStatus.CONFLICT)

object BoardTitleConflict : Board409("이미 존재하는 게시판 이름입니다", 0)
object ForbiddenBoardType : Board403("일반 유저는 CUSTOM_BASE, CUSTOM_PHOTO 타입의 게시판만 생성 가능합니다", 1)
object ForbiddenBoardCategory : Board403("일반 유저는 OTHER 카테고리의 게시판만 생성 가능합니다", 2)
object ForbiddenBoardTypeAdmin : Board403("관리자 유저는 CUSTOM 타입의 게시판 생성 불가능", 3)
object BoardNotFound : Board404("해당 id의 게시판이 존재하지 않습니다", 4)
object ForbiddenBoardRemoval : Board403("해당 게시판을 삭제할 권한이 없습니다", 5)
