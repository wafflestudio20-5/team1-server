package com.wafflytime.board.service

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.dto.CreateBoardRequest
import com.wafflytime.board.dto.CreateBoardResponse
import com.wafflytime.board.dto.DeleteBoardRequest
import com.wafflytime.board.dto.DeleteBoardResponse
import com.wafflytime.board.types.BoardType
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.exception.WafflyTime409
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service


@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createBoard(request: CreateBoardRequest): CreateBoardResponse {
        val univEmail : String? = request.univEmail
        boardRepository.findByTitle(request.title)?.let { throw WafflyTime409("이미 ${request.title}이 존재합니다") }

        if (univEmail == null) {
            if (request.boardType == BoardType.CUSTOM) throw WafflyTime400("게시판 타입이 잘못됐습니다")
            boardRepository.save(BoardEntity(title=request.title, type = request.boardType))
            return CreateBoardResponse(id = null, title = request.title)
        } else {
            if (request.boardType != BoardType.CUSTOM) throw WafflyTime400("게시판 타입이 잘못됐습니다")
            val user: UserEntity = userRepository.findByUnivEmail(univEmail) ?: throw WafflyTime404("해당 메일을 가진 유저가 없습니다")
            boardRepository.save(BoardEntity(title = request.title, user = user, type = BoardType.CUSTOM))
            return CreateBoardResponse(id = user.id, title = request.title)
        }
    }

    @Transactional
    fun deleteBoard(request: DeleteBoardRequest): DeleteBoardResponse {
        val board: BoardEntity = boardRepository.findByTitle(request.title) ?: throw WafflyTime404("${request.title} 이 존재하지 않습니다")

        request.univEmail ?.let {
            // 일반 유저인 경우
            if (board.type == BoardType.DEFAULT) throw WafflyTime401("일반 유저는 default 게시판을 삭제할 수 없습니다")
            if (board.user!!.univEmail != request.univEmail) throw WafflyTime400("게시판 owner가 아닌 유저는 게시판을 삭제할 수 없습니다")
            boardRepository.delete(board)
        }
        boardRepository.delete(board)
        return DeleteBoardResponse(boardId = board.id, title = board.title)
    }
}