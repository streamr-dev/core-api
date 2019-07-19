package com.unifina.service

import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class CommunityProductService {
	PasswordEncoder encoder = new BCryptPasswordEncoder()

	void onApproveJoinRequest(CommunityJoinRequest c) {
		c.state = CommunityJoinRequest.State.ACCEPTED
		// Fetch joinPartStream id from smart contract at address
		// Backend produces join message to joinPartStream
	}

	List<CommunityJoinRequest> findCommunityJoinRequests(String communityAddress, CommunityJoinRequest.State st) {
		if (st != null) {
			def query = CommunityJoinRequest.where {
				(state == st.toString()) && (communityAddress == communityAddress)
			}
			return query.findAll()
		}
		return CommunityJoinRequest.findAllWhere(communityAddress: communityAddress)
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
		// Find CommunitySecret by communityAddress
		CommunitySecret secret = CommunitySecret.where {
			communityAddress == communityAddress
		}.find()
		// If cmd.secret matches CommunitySecret.secret then set CommunityJoinRequest.state = ACCEPTED
		if (secret != null && secret.secret != null && cmd.secret != null) {
			if (encoder.matches(cmd.secret, secret.secret)) {
				if (c.state != CommunityJoinRequest.State.ACCEPTED) { // TODO: ????
					onApproveJoinRequest(c)
				}
			} else {
				throw new ApiException(403, "INCORRECT_COMMUNITY_SECRET", "incorrect community secret")
			}
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
			return null
		}
		try {
			c.state = CommunityJoinRequest.State.valueOf(cmd.state)
		} catch (IllegalArgumentException e) {
			return null
		}
		if (c.state == CommunityJoinRequest.State.ACCEPTED) {
			onApproveJoinRequest(c)
		}
		c.save(validate: true, failOnError: true)
		return c
	}
}
