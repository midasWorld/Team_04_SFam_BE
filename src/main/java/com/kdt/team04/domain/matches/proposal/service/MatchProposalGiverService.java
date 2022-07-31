package com.kdt.team04.domain.matches.proposal.service;

import java.text.MessageFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kdt.team04.common.exception.EntityNotFoundException;
import com.kdt.team04.common.exception.ErrorCode;
import com.kdt.team04.domain.matches.proposal.dto.MatchProposalQueryDto;
import com.kdt.team04.domain.matches.proposal.dto.MatchProposalResponse;
import com.kdt.team04.domain.matches.proposal.entity.MatchProposal;
import com.kdt.team04.domain.matches.proposal.entity.MatchProposalStatus;
import com.kdt.team04.domain.matches.proposal.repository.MatchProposalRepository;
import com.kdt.team04.domain.team.dto.TeamResponse;
import com.kdt.team04.domain.team.entity.Team;
import com.kdt.team04.domain.user.dto.UserResponse;
import com.kdt.team04.domain.user.entity.User;

@Service
@Transactional(readOnly = true)
public class MatchProposalGiverService {

	private final MatchProposalRepository matchProposalRepository;

	public MatchProposalGiverService(MatchProposalRepository matchProposalRepository) {
		this.matchProposalRepository = matchProposalRepository;
	}

	public MatchProposalQueryDto findSimpleProposalById(Long id) {
		MatchProposalQueryDto matchProposal = matchProposalRepository.findSimpleProposalById(id)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCH_PROPOSAL_NOT_FOUND,
				MessageFormat.format("matchProposalId = {0}", id)));

		return matchProposal;
	}

	@Transactional
	public MatchProposalResponse.FixedProposal updateToFixed(Long id) {
		MatchProposal matchProposal = matchProposalRepository.findProposalById(id)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.MATCH_PROPOSAL_NOT_FOUND,
				MessageFormat.format("matchProposalId = {0}", id)));

		matchProposal.updateStatus(MatchProposalStatus.FIXED);

		User user = matchProposal.getUser();
		UserResponse.AuthorResponse userResponse = new UserResponse.AuthorResponse(user.getId(), user.getNickname());

		Team team = matchProposal.getTeam();
		TeamResponse.SimpleResponse teamResponse =
			team == null ? null : new TeamResponse.SimpleResponse(team.getId(), team.getName(), team.getSportsCategory());

		return new MatchProposalResponse.FixedProposal(
			matchProposal.getId(),
			userResponse,
			teamResponse
		);
	}
}
