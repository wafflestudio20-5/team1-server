package com.wafflytime.board.service

import com.wafflytime.board.database.BoardEntity
import com.wafflytime.board.database.BoardRepository
import com.wafflytime.board.dto.*
import com.wafflytime.board.exception.*
import com.wafflytime.board.type.BoardCategory
import com.wafflytime.board.type.BoardType
import com.wafflytime.common.S3Service
import com.wafflytime.user.info.service.UserService
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class BoardService(
    private val boardRepository: BoardRepository,
    private val userService: UserService,
    private val s3Service: S3Service
) {

    @Transactional
    fun createBoard(userId: Long, request: CreateBoardRequest): CreateBoardResponse {
        /**
         * basic, career, student, department 게시판은 admin만 만들 수 있고, 일반 유저는 other 게시판만 만들 수 있다
        **/
        boardRepository.findByTitle(request.title)?.let { throw BoardTitleConflict }
        val user = userService.getUser(userId)

        if (!user.isAdmin) {
            if (request.boardType !in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw ForbiddenBoardType
            }
            if (request.boardCategory != BoardCategory.OTHER) {
                throw ForbiddenBoardCategory
            }
        } else {
            if (request.boardType in arrayOf(BoardType.CUSTOM_BASE, BoardType.CUSTOM_PHOTO)) {
                throw ForbiddenBoardTypeAdmin
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
        val board = getBoardEntity(boardId)
        val user = userService.getUser(userId)

        if (!user.isAdmin) {
            if (board.type == BoardType.DEFAULT) throw ForbiddenBoardRemoval
            if (board.owner!!.id != userId) throw ForbiddenBoardRemoval
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
        val board = getBoardEntity(boardId)
        return BoardResponse.of(board)
    }

    fun getBoardEntity(boardId: Long): BoardEntity {
        return boardRepository.findByIdOrNull(boardId)
            ?: throw BoardNotFound
    }

    fun getHomeBoards(): List<HomeBoardResponse> {
        return boardRepository.findHomeBoards().map { HomeBoardResponse.of(it)}
    }

    fun searchBoards(keyword: String): List<BoardResponse> {
        return boardRepository.findBoardsByKeyword(keyword).map { BoardResponse.of(it) }
    }
}