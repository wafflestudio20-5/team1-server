package com.wafflytime.board.service

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.dto.CreateBoardRequest
import com.wafflytime.board.dto.CreateBoardResponse
import com.wafflytime.board.dto.DeleteBoardRequest
import com.wafflytime.board.dto.DeleteBoardResponse
import com.wafflytime.board.type.BoardType
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.exception.WafflyTime409
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import com.wafflytime.user.info.type.UserRole
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createBoard(userId: Long, request: CreateBoardRequest): CreateBoardResponse {
        boardRepository.findByTitle(request.title)?.let { throw WafflyTime409("이미 ${request.title}이 존재합니다") }
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("해당 유저가 존재하지 않습니다")

        if (user.role == UserRole.ROLE_USER) {
            if (request.boardType  !in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw WafflyTime400("user는 CUSTOM_BASE, CUSTOM_PHOTO 타입의 게시판만 생성 가능합니다")
            }
        } else {
            if (request.boardType in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw WafflyTime400("admin은 CUSTOM 타입으로 게시판을 생성하지 않습니다")
            }
        }
        boardRepository.save(BoardEntity(title = request.title, user = user, type = request.boardType))
        return CreateBoardResponse(id = userId, title = request.title)
    }

    @Transactional
    fun deleteBoard(userId: Long, boardId: Long): DeleteBoardResponse {
        val board: BoardEntity = boardRepository.findByIdOrNull(boardId) ?: throw WafflyTime404("board id가 존재하지 않습니다")
        val user: UserEntity = userRepository.findByIdOrNull(userId)!!

        if (user.role == UserRole.ROLE_USER) {
            if (board.type == BoardType.DEFAULT) throw WafflyTime401("일반 유저는 default 게시판을 삭제할 수 없습니다")
            if (board.user!!.id != userId) throw WafflyTime400("게시판 owner가 아닌 유저는 게시판을 삭제할 수 없습니다")
        }
        boardRepository.delete(board)
        return DeleteBoardResponse(boardId = board.id, title = board.title)
    }
}