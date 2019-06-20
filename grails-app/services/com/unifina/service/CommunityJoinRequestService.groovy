package com.unifina.service

import com.streamr.client.protocol.message_layer.StreamMessage
import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.data.Stream
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.SecUser
import com.unifina.signalpath.utils.MessageChainUtil
import com.unifina.utils.MapTraversal
import grails.util.Holders
import org.web3j.crypto.Credentials

class CommunityJoinRequestService {
	StreamService streamService
	EthereumService ethereumService

	void onApproveJoinRequest(CommunityJoinRequest c) {
		String joinPartStreamID = ethereumService.fetchJoinPartStreamID(c.communityAddress)
		// Send join message to joinPartStream
		Stream s = Stream.findById(joinPartStreamID)
		if (s == null) {
			throw new NotFoundException("stream not found by id: " + joinPartStreamID)
		}
		Map<String, Object> content = new HashMap<>()
		content.put("type", "join")
		content.put("addresses", Arrays.asList(c.memberAddress))
		String nodePrivateKey = MapTraversal.getString(Holders.getConfig(), "streamr.ethereum.nodePrivateKey")
		Credentials credentials = Credentials.create(nodePrivateKey)
		MessageChainUtil chain = new MessageChainUtil(credentials.getAddress())
		StreamMessage msg = chain.getStreamMessage(s, new Date(), content)
		streamService.sendMessage(msg)
	}

	List<CommunityJoinRequest> findAll(String communityAddress, CommunityJoinRequest.State state) {
		return CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
			if (state) {
				eq("state", state)
			}
		}
	}

	CommunityJoinRequest create(String communityAddress, CommunityJoinRequestCommand cmd, SecUser user) {
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

	CommunityJoinRequest find(String communityAddress, String joinRequestId) {
		CommunityJoinRequest c = CommunityJoinRequest.where {
			(communityAddress == communityAddress) && (id == joinRequestId)
		}.find()
		return c
	}

	CommunityJoinRequest update(String communityAddress, String joinRequestId, UpdateCommunityJoinRequestCommand cmd) {
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
