package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser

class CommunityJoinRequestService {
	void onApproveJoinRequest(CommunityJoinRequest c) {
		// TODO: Fetch joinPartStream id from smart contract at address
		// TODO: Backend produces join message to joinPartStream
	}

	List<CommunityJoinRequest> findAll(String communityAddress, CommunityJoinRequest.State state) {
		return CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
			if (state) {
				eq("state", state)
			}
		}
	}

	CommunityJoinRequest createCommunityJoinRequest(String communityAddress, CommunityJoinRequestCommand cmd, SecUser user) {
		// Backend must check that the given memberAddress is one of the Ethereum IDs bound to the logged in user
		IntegrationKey key = IntegrationKey.where {
			(user == user) && (idInService == cmd.memberAddress)
		}.find()
		if (key == null) {
			throw new NotFoundException("given member address is not owned by the user")
		}
		// Create CommunityJoinRequest
		CommunityJoinRequest c = new CommunityJoinRequest()
		c.user = user
		c.communityAddress = communityAddress
		c.memberAddress = cmd.memberAddress
		if (cmd.secret) { // validate secret if it is given
			// Find CommunitySecret by communityAddress
			CommunitySecret secret = CommunitySecret.where {
				communityAddress == communityAddress && secret == cmd.secret
			}.find()
			if (secret) { // validated!
				c.state = CommunityJoinRequest.State.ACCEPTED
				onApproveJoinRequest(c)
			} else {
				throw new ApiException(403, "INCORRECT_COMMUNITY_SECRET", "incorrect community secret")
			}
		} else { // request stays in pending state
		}
		c.save(validate: true, failOnError: true)
		return c
	}

	CommunityJoinRequest findCommunityJoinRequest(String communityAddress, String joinRequestId) {
		CommunityJoinRequest c = CommunityJoinRequest.where {
			(communityAddress == communityAddress) && (id == joinRequestId)
		}.find()
		return c
	}

	CommunityJoinRequest updateCommunityJoinRequest(String communityAddress, String joinRequestId, UpdateCommunityJoinRequestCommand cmd) {
		CommunityJoinRequest c = CommunityJoinRequest.where {
			(communityAddress == communityAddress) && (id == joinRequestId)
		}.find()
		if (c == null) {
			throw new NotFoundException("community join request not found")
		}
		CommunityJoinRequest.State newState
		try {
			newState = CommunityJoinRequest.State.valueOf(cmd.state)
		} catch (IllegalArgumentException e) {
			throw new ApiException(400, "INVALID_JOIN_REQUEST_STATE", "unknown community join request state")
		}
		if (c.state == CommunityJoinRequest.State.PENDING && (newState == CommunityJoinRequest.State.ACCEPTED || newState == CommunityJoinRequest.State.REJECTED)) {
			c.state = newState
		} else {
			throw new ApiException(400, "JOIN_REQUEST_ALREADY_ACCEPTED", "community join request has been already accepted")
		}
		if (c.state == CommunityJoinRequest.State.ACCEPTED) {
			onApproveJoinRequest(c)
		}
		c.save(validate: true, failOnError: true)
		return c
	}
}
