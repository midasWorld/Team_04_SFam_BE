package com.kdt.team04.domain.user.service;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kdt.team04.common.exception.EntityNotFoundException;
import com.kdt.team04.common.exception.ErrorCode;
import com.kdt.team04.common.file.ImagePath;
import com.kdt.team04.common.file.service.S3Uploader;
import com.kdt.team04.domain.matches.review.dto.MatchReviewResponse;
import com.kdt.team04.domain.matches.review.service.MatchReviewGiverService;
import com.kdt.team04.domain.team.dto.TeamResponse;
import com.kdt.team04.domain.team.service.TeamGiverService;
import com.kdt.team04.domain.user.UserConverter;
import com.kdt.team04.domain.user.dto.UserRequest;
import com.kdt.team04.domain.user.dto.UserResponse;
import com.kdt.team04.domain.user.entity.Location;
import com.kdt.team04.domain.user.entity.User;
import com.kdt.team04.domain.user.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final MatchReviewGiverService matchReviewGiver;
	private final TeamGiverService teamGiver;
	private final UserConverter userConverter;
	private final S3Uploader s3Uploader;

	public UserService(UserRepository userRepository, MatchReviewGiverService matchReviewGiver,
		TeamGiverService teamGiver, S3Uploader s3Uploader, UserConverter userConverter) {
		this.userRepository = userRepository;
		this.matchReviewGiver = matchReviewGiver;
		this.teamGiver = teamGiver;
		this.userConverter = userConverter;
		this.s3Uploader = s3Uploader;
	}

	public UserResponse findByUsername(String username) {
		User foundUser = this.userRepository.findByUsername(username)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("Username = {0}", username)));

		return userConverter.toUserResponse(foundUser);
	}

	public UserResponse findByEmail(String email) {
		User foundUser = this.userRepository.findByEmail(email)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("email = {0}", email)));

		return userConverter.toUserResponse(foundUser);
	}

	public UserResponse findById(Long id) {
		User foundUser = this.userRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("UserId = {0}", id)));

		return userConverter.toUserResponse(foundUser);
	}

	public UserResponse.FindProfile findProfileById(Long id) {
		User foundUser = userRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("UserId = {0}", id)));

		MatchReviewResponse.TotalCount review = matchReviewGiver.findTotalReviewByUserId(id);
		List<TeamResponse.SimpleResponse> teams = teamGiver.findAllByTeamMemberUserId(id);

		return new UserResponse.FindProfile(
			foundUser.getNickname(),
			foundUser.getProfileImageUrl(),
			review,
			teams
		);
	}

	@Transactional
	public Long create(UserRequest.CreateRequest request) {
		User user = userConverter.toUser(request);

		return userRepository.save(user).getId();
	}

	public List<UserResponse.UserFindResponse> findAllByNickname(String nickname) {
		return userRepository.findByNicknameContaining(nickname).stream()
			.map(
				user -> new UserResponse.UserFindResponse(
					user.getId(),
					user.getUsername(),
					user.getNickname(),
					user.getProfileImageUrl()
				)
			)
			.toList();
	}

	@Transactional
	public UserResponse.UpdateLocationResponse updateLocation(Long targetId,
		UserRequest.UpdateLocationRequest request) {
		User foundUser = this.userRepository.findById(targetId)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("UserId = {0}", targetId)));
		foundUser.updateLocation(new Location(request.latitude(), request.longitude()));

		return new UserResponse.UpdateLocationResponse(request.latitude(), request.longitude());
	}

	public Boolean usernameDuplicationCheck(String username) {
		return userRepository.existsByUsername(username);
	}

	public Boolean nicknameDuplicationCheck(String nickname) {
		return userRepository.existsByNickname(nickname);
	}

	public void update(Long targetId, UserRequest.Update request) {
		User foundUser = this.userRepository.findById(targetId)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("UserId = {0}", targetId)));
		foundUser.update(request.nickname(), request.email(), request.profileImageUrl());
	}

	@Transactional
	public void uploadProfile(Long id, MultipartFile file) {
		User foundUser = this.userRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND,
				MessageFormat.format("UserId = {0}", id)));

		Optional.ofNullable(foundUser.getProfileImageUrl())
			.ifPresentOrElse(
				key -> s3Uploader.uploadByKey(file.getResource(), key),
				() -> {
					String key = s3Uploader.uploadByPath(file.getResource(), ImagePath.USERS_PROFILES.getPath());
					foundUser.updateImageUrl(key);
				}
			);
	}

}
