package com.wafflytime.board.service

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.dto.*
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType
import com.wafflytime.common.S3Service
import com.wafflytime.exception.WafflyTime400
import com.wafflytime.exception.WafflyTime401
import com.wafflytime.exception.WafflyTime404
import com.wafflytime.exception.WafflyTime409
import com.wafflytime.user.info.database.UserEntity
import com.wafflytime.user.info.database.UserRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userRepository: UserRepository,
    private val s3Service: S3Service
) {

    @Transactional
    fun createBoard(userId: Long, request: CreateBoardRequest): CreateBoardResponse {
        /**
         * basic, career, student, department 게시판은 admin만 만들 수 있고, 일반 유저는 other 게시판만 만들 수 있다
        **/
        boardRepository.findByTitle(request.title)?.let { throw WafflyTime409("이미 ${request.title}이 존재합니다") }
        val user = userRepository.findByIdOrNull(userId) ?: throw WafflyTime404("해당 유저가 존재하지 않습니다")

        if (!user.isAdmin) {
            if (request.boardType !in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw WafflyTime400("user는 CUSTOM_BASE, CUSTOM_PHOTO 타입의 게시판만 생성 가능합니다")
            }
            if (request.boardCategory != BoardCategory.OTHER) {
                throw WafflyTime400("user는 OTHER 카테고리의 게시판만 생성 가능합니다")
            }
        } else {
            if (request.boardType in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw WafflyTime400("admin은 CUSTOM 타입으로 게시판을 생성하지 않습니다")
            }
        }
        val board = boardRepository.save(BoardEntity(
            title = request.title,
            description = request.description,
            owner = user,
            type = request.boardType,
            category = request.boardCategory,
            allowAnonymous = request.allowAnonymous
        ))
        return CreateBoardResponse(
            userId = userId,
            boardId = board.id,
            boardType = board.type,
            category = board.category,
            title = board.title,
            description = board.description,
            allowAnonymous = board.allowAnonymous
        )
    }

    @Transactional
    fun deleteBoard(userId: Long, boardId: Long): DeleteBoardResponse {
        val board: BoardEntity = boardRepository.findByIdOrNull(boardId) ?: throw WafflyTime404("board id가 존재하지 않습니다")
        val user: UserEntity = userRepository.findByIdOrNull(userId)!!

        if (!user.isAdmin) {
            if (board.type == BoardType.DEFAULT) throw WafflyTime401("일반 유저는 default 게시판을 삭제할 수 없습니다")
            if (board.owner!!.id != userId) throw WafflyTime400("게시판 owner가 아닌 유저는 게시판을 삭제할 수 없습니다")
        }
        val posts =  board.posts
        val listOfImages = posts.map { it.images }
        s3Service.deleteListOfFiles(listOfImages)

        boardRepository.delete(board)
        return DeleteBoardResponse(boardId = board.id, title = board.title)
    }

    fun getAllBoards(): List<BoardListResponse> {
        val boardsGrouped = boardRepository.findAll().groupBy { it.category }
        val boardGroupedListResponse: MutableList<BoardListResponse> = mutableListOf()
        BoardCategory.values().forEach {
            boardGroupedListResponse.add(BoardListResponse.of(it, boardsGrouped[it]))

        }
        return boardGroupedListResponse
    }

    fun getBoard(boardId: Long): BoardResponse {
        val board = boardRepository.findByIdOrNull(boardId) ?: throw WafflyTime404("board id가 존재하지 않습니다")
        return BoardResponse.of(board)
    }
}