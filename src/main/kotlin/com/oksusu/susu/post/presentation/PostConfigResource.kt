package com.oksusu.susu.post.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.post.application.PostConfigService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.POST_CONFIG_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/posts/configs"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PostConfigResource(
    private val postConfigService: PostConfigService,
) {
    @Operation(summary = "게시글 카테고리 데이터 제공")
    @GetMapping("/create-post")
    suspend fun getCreatePostsConfig(
        user: AuthUser,
    ) = postConfigService.getAll().wrapOk()
}
