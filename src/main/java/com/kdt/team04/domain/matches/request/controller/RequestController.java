package com.kdt.team04.domain.matches.request.controller;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kdt.team04.common.exception.NotAuthenticationException;
import com.kdt.team04.common.security.jwt.JwtAuthentication;
import com.kdt.team04.domain.matches.request.dto.MatchProposalRequest;
import com.kdt.team04.domain.matches.request.service.MatchProposalService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/matches/{matchId}/proposals")
public class RequestController {

	private final MatchProposalService matchProposalService;

	public RequestController(MatchProposalService matchProposalService) {
		this.matchProposalService = matchProposalService;
	}

	@PostMapping
	@Operation(summary = "대결 신청", description = "사용자는 대결을 신청할 수 있다.")
	public void propose(@PathVariable Long matchId, @AuthenticationPrincipal JwtAuthentication jwtAuthentication,
		@RequestBody @Valid MatchProposalRequest.ProposalCreate request) {
		if (jwtAuthentication == null) {
			throw new NotAuthenticationException("Not Authenticated");
		}

		matchProposalService.create(jwtAuthentication.id(), matchId, request);
	}
}
