package com.kdt.team04.domain.user.dto;

import java.util.List;

import com.kdt.team04.domain.match.review.dto.MatchReviewResponse;
import com.kdt.team04.domain.team.dto.TeamResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public record UserResponse(Long id, String username, String password, String nickname) {

	@Builder
	public record FindProfile(
		@Schema(description = "회원 닉네임")
		String nickname,

		@Schema(description = "후기 정보")
		MatchReviewResponse.TotalCount review,

		@Schema(description = "소속 팀 목록")
		List<TeamResponse.SimpleResponse> teams
	) {}

	public record UserFindResponse(
		@Schema(description = "회원 고유 PK")
		Long id,
		@Schema(description = "회원 id")
		String username,
		@Schema(description = "회원 닉네임")
		String nickname
	) {}

}
