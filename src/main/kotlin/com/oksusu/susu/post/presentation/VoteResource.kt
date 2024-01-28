package com.oksusu.susu.post.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapSlice
import com.oksusu.susu.extension.wrapVoid
import com.oksusu.susu.post.application.VoteFacade
import com.oksusu.susu.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import com.oksusu.susu.post.model.vo.SearchVoteRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.VOTE_SWAGGER_TAG, description = "투표 API")
@RestController
@RequestMapping(value = ["/api/v1/votes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteResource(
    private val voteFacade: VoteFacade,
) {
    @Operation(summary = "투표 생성")
    @PostMapping
    suspend fun createVote(
        user: AuthUser,
        @RequestBody request: CreateVoteRequest,
    ) = voteFacade.createVote(user, request).wrapCreated()

    @Operation(summary = "투표 조회")
    @GetMapping
    suspend fun searchVotes(
        user: AuthUser,
        @ParameterObject searchRequest: SearchVoteRequest,
        @ParameterObject sliceRequest: SusuPageRequest,
    ) = voteFacade.getAllVotes(user, searchRequest, sliceRequest).wrapSlice()

    @Operation(summary = "투표 하나 조회")
    @GetMapping("/{id}")
    suspend fun getVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = voteFacade.getVote(user, id).wrapOk()

    @Operation(summary = "투표하기")
    @PostMapping("/{id}")
    suspend fun castVote(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateVoteHistoryRequest,
    ) = voteFacade.vote(user, id, request).wrapCreated()

    @Operation(summary = "투표 삭제하기")
    @DeleteMapping("/{id}")
    suspend fun deleteVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = voteFacade.deleteVote(user, id).wrapVoid()

    @Operation(summary = "투표 업데이트")
    @PatchMapping("/{id}")
    suspend fun update(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: UpdateVoteRequest,
    ) = voteFacade.update(user, id, request).wrapOk()

    @Operation(summary = "가장 인기 있는 투표 검색")
    @GetMapping("/popular")
    suspend fun getPopularVotes(
        user: AuthUser,
        @RequestParam(defaultValue = "5") size: Int,
    ) = voteFacade.getPopularVotes(user, size).wrapOk()
}
