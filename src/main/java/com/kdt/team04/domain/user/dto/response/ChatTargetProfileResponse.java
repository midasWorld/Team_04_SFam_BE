package com.kdt.team04.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatTargetProfileResponse(
	@Schema(description = "채팅 상대 닉네임")
	String nickname
) {
}
