package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.api.ApiException
import com.unifina.api.CommunityJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateCommunityJoinRequestCommand
import com.unifina.domain.community.CommunityJoinRequest
import com.unifina.domain.community.CommunitySecret
import com.unifina.domain.data.Stream
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import org.apache.log4j.Logger

class CommunityJoinRequestService {

	private static final Logger log = Logger.getLogger(CommunityJoinRequestService)

	EthereumService ethereumService
	PermissionService permissionService
	StreamrClientService streamrClientService

	private void onApproveJoinRequest(CommunityJoinRequest c) {
		log.debug("onApproveJoinRequest: approved JoinRequest for address ${c.memberAddress} to community ${c.communityAddress}")
		for (Stream s : findStreams(c)) {
			log.debug(String.format("granting write permission to %s (%s) for %s", s.name, s.id, c.user.username))
			permissionService.systemGrant(c.user, s, Permission.Operation.WRITE)
		}
		sendMessage(c, "join")
		log.debug("exiting onApproveJoinRequest")
	}

	protected Set<Stream> findStreams(CommunityJoinRequest c) {
		log.debug(String.format("entering findStreams(%s)", c))
		List<Product> products = Product.withCriteria {
			eq("type", Product.Type.COMMUNITY)
			eq("beneficiaryAddress", c.communityAddress)
		}
		Set<Stream> streams = new HashSet<>()
		for (Product p : products) {
			streams.addAll(p.streams)
		}
		log.debug(String.format("exiting findStreams(): %s", streams))
		return streams
	}

	/**
	 * Sends a message to joinPartStream using the credentials of this Engine node
	 */
	private void sendMessage(CommunityJoinRequest c, String type) {
		log.debug("sendMessage: fetching joinPartStreamID for community ${c.communityAddress}")
		String joinPartStreamID = ethereumService.fetchJoinPartStreamID(c.communityAddress)
		log.debug(String.format("sending message to join part stream id: '%s', community address: '%s'", joinPartStreamID, c.communityAddress))
		Map<String, Object> msg = new HashMap<>()
		msg.put("type", type)
		msg.put("addresses", Arrays.asList(c.memberAddress))

		StreamrClient client = streamrClientService.getInstanceForThisEngineNode()
		log.debug("sendMessage: StreamrClient state is ${client.getState()}, websocket url: ${client.getOptions().getWebsocketApiUrl()}")

		com.streamr.client.rest.Stream stream = client.getStream(joinPartStreamID)

		if (stream == null) {
			throw new NotFoundException(String.format("Stream not found by id: %s", joinPartStreamID))
		}

		log.debug("sendMessage: publishing message to stream ${stream.getId()}: ${msg}")
		client.publish(stream, msg)
		log.debug("exiting sendMessage")
	}

	Set<SecUser> findCommunityMembers(String communityAddress) {
		List<CommunityJoinRequest> requests = CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
		}
		Set<SecUser> users = new HashSet<>()
		for (CommunityJoinRequest c : requests) {
			users.add(c.user)
		}
		return users
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
		IntegrationKey key = IntegrationKey.withCriteria {
			eq("user", user)
			eq("idInService", cmd.memberAddress)
		}.find()
		if (key == null) {
			throw new NotFoundException("Given member address is not owned by the user")
		}
		// Create CommunityJoinRequest
		CommunityJoinRequest c = new CommunityJoinRequest()
		c.user = user
		c.communityAddress = communityAddress
		c.memberAddress = cmd.memberAddress
		if (cmd.secret) { // validate secret if it is given
			// Find CommunitySecret by communityAddress
			CommunitySecret secret = CommunitySecret.withCriteria {
				eq("communityAddress", communityAddress)
				eq("secret", cmd.secret)
			}.find()
			if (secret) { // validated!
				c.state = CommunityJoinRequest.State.ACCEPTED
				onApproveJoinRequest(c)
			} else {
				throw new ApiException(403, "INCORRECT_COMMUNITY_SECRET", "Incorrect community secret")
			}
		} else { // request stays in pending state
		}
		c.save(validate: true, failOnError: true)
		return c
	}

	CommunityJoinRequest find(String communityAddress, String joinRequestId) {
		CommunityJoinRequest c = CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
			eq("id", joinRequestId)
		}.find()
		return c
	}

	CommunityJoinRequest update(String communityAddress, String joinRequestId, UpdateCommunityJoinRequestCommand cmd) {
		CommunityJoinRequest c = CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
			eq("id", joinRequestId)
		}.find()
		if (c == null) {
			throw new NotFoundException("Community join request not found")
		}
		CommunityJoinRequest.State newState
		try {
			newState = CommunityJoinRequest.State.valueOf(cmd.state)
		} catch (IllegalArgumentException e) {
			throw new ApiException(400, "INVALID_JOIN_REQUEST_STATE", "Unknown community join request state")
		}
		if (c.state == CommunityJoinRequest.State.PENDING && (newState == CommunityJoinRequest.State.ACCEPTED || newState == CommunityJoinRequest.State.REJECTED)) {
			c.state = newState
		} else {
			throw new ApiException(400, "JOIN_REQUEST_ALREADY_ACCEPTED", "Community join request has been already accepted")
		}
		if (c.state == CommunityJoinRequest.State.ACCEPTED) {
			onApproveJoinRequest(c)
		}
		c.save(validate: true, failOnError: true)
		return c
	}

	void delete(String communityAddress, String joinRequestId) {
		CommunityJoinRequest c = CommunityJoinRequest.withCriteria {
			eq("communityAddress", communityAddress)
			eq("id", joinRequestId)
		}.find()
		if (c == null) {
			String fmt = "Community join request not found by community address: '%s' and join request id: '%s'"
			String message = String.format(fmt, communityAddress, joinRequestId)
			throw new NotFoundException(message)
		}

		for (Stream s : findStreams(c)) {
			permissionService.systemRevoke(c.user, s, Permission.Operation.WRITE)
		}
		sendMessage(c, "part")
		c.delete()
	}
}
