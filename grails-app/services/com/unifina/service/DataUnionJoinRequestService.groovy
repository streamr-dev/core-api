package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.api.ApiException
import com.unifina.api.DataUnionJoinRequestCommand
import com.unifina.api.NotFoundException
import com.unifina.api.UpdateDataUnionJoinRequestCommand
import com.unifina.domain.data.Stream
import com.unifina.domain.dataunion.DataUnionJoinRequest
import com.unifina.domain.dataunion.DataUnionSecret
import com.unifina.domain.marketplace.Product
import com.unifina.domain.security.IntegrationKey
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import org.apache.log4j.Logger

class DataUnionJoinRequestService {

	private static final Logger log = Logger.getLogger(DataUnionJoinRequestService)

	EthereumService ethereumService
	PermissionService permissionService
	StreamrClientService streamrClientService

	private void onApproveJoinRequest(DataUnionJoinRequest c) {
		log.debug("onApproveJoinRequest: approved JoinRequest for address ${c.memberAddress} to data union ${c.contractAddress}")
		for (Stream s : findStreams(c)) {
			if (permissionService.check(c.user, s, Permission.Operation.STREAM_PUBLISH)) {
				log.debug(String.format("user %s already has write permission to %s (%s), skipping grant", c.user.username, s.name, s.id))
			} else {
				log.debug(String.format("granting write permission to %s (%s) for %s", s.name, s.id, c.user.username))
				permissionService.systemGrant(c.user, s, Permission.Operation.STREAM_GET)
				permissionService.systemGrant(c.user, s, Permission.Operation.STREAM_PUBLISH)
			}
		}
		sendMessage(c, "join")
		log.debug("exiting onApproveJoinRequest")
	}

	protected Set<Stream> findStreams(DataUnionJoinRequest c) {
		log.debug(String.format("entering findStreams(%s)", c))
		List<Product> products = Product.createCriteria().list {
			eq("type", Product.Type.DATAUNION)
			ilike("beneficiaryAddress", c.contractAddress)
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
	private void sendMessage(DataUnionJoinRequest c, String type) {
		log.debug("sendMessage: fetching joinPartStreamID for data union ${c.contractAddress}")
		String joinPartStreamID = ethereumService.fetchJoinPartStreamID(c.contractAddress)
		log.debug(String.format("sending message to join part stream id: '%s', data union address: '%s'", joinPartStreamID, c.contractAddress))
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

	Set<SecUser> findMembers(String contractAddress) {
		List<DataUnionJoinRequest> requests = DataUnionJoinRequest.createCriteria().list {
			ilike("contractAddress", contractAddress)
		}
		Set<SecUser> users = new HashSet<>()
		for (DataUnionJoinRequest c : requests) {
			users.add(c.user)
		}
		return users
	}

	List<DataUnionJoinRequest> findAll(String contractAddress, DataUnionJoinRequest.State state) {
		return DataUnionJoinRequest.createCriteria().list {
			ilike("contractAddress", contractAddress)
			if (state) {
				eq("state", state)
			}
		}
	}

	DataUnionJoinRequest create(String contractAddress, DataUnionJoinRequestCommand cmd, SecUser user) {
		// TODO CORE-1834: check if user already has a PENDING request
		// TODO CORE-1834: OR if user already has a write permission to the stream

		// Backend must check that the given memberAddress is one of the Ethereum IDs bound to the logged in user
		IntegrationKey key = IntegrationKey.createCriteria().get {
			eq("user", user)
			ilike("idInService", cmd.memberAddress)
		}
		if (key == null) {
			throw new NotFoundException("Given member address is not owned by the user")
		}

		// Create DataUnionJoinRequest
		DataUnionJoinRequest c = new DataUnionJoinRequest()
		c.user = user
		c.contractAddress = contractAddress
		c.memberAddress = cmd.memberAddress

		// validate secret if it is given
		if (cmd.secret) {
			// Find DataUnionSecret by contractAddress
			DataUnionSecret secret = DataUnionSecret.createCriteria().get {
				ilike("contractAddress", contractAddress)
				eq("secret", cmd.secret)
			}
			if (secret) {
				c.state = DataUnionJoinRequest.State.ACCEPTED
				onApproveJoinRequest(c)
			} else {
				throw new ApiException(403, "INCORRECT_SECRET", "Incorrect data union secret")
			}
		} else {
			// request stays in pending state,
			//   waiting for a manual approval (PUT request) from admin
		}
		c.save(validate: true, failOnError: true)
		return c
	}

	DataUnionJoinRequest find(String contractAddress, String joinRequestId) {
		DataUnionJoinRequest c = DataUnionJoinRequest.createCriteria().get {
			ilike("contractAddress", contractAddress)
			eq("id", joinRequestId)
		}.find()
		return c
	}

	DataUnionJoinRequest update(String contractAddress, String joinRequestId, UpdateDataUnionJoinRequestCommand cmd) {
		DataUnionJoinRequest c = DataUnionJoinRequest.createCriteria().get {
			ilike("contractAddress", contractAddress)
			eq("id", joinRequestId)
		}
		if (c == null) {
			throw new NotFoundException("Join request not found")
		}
		DataUnionJoinRequest.State newState
		try {
			newState = DataUnionJoinRequest.State.valueOf(cmd.state)
		} catch (IllegalArgumentException e) {
			throw new ApiException(400, "INVALID_JOIN_REQUEST_STATE", "Unknown join request state")
		}
		if (c.state == DataUnionJoinRequest.State.PENDING && (newState == DataUnionJoinRequest.State.ACCEPTED || newState == DataUnionJoinRequest.State.REJECTED)) {
			c.state = newState
		} else {
			throw new ApiException(400, "JOIN_REQUEST_ALREADY_ACCEPTED", "Join request has been already accepted")
		}
		if (c.state == DataUnionJoinRequest.State.ACCEPTED) {
			onApproveJoinRequest(c)
		}
		c.save(validate: true, failOnError: true)
		return c
	}

	void delete(String contractAddress, String joinRequestId) {
		DataUnionJoinRequest c = DataUnionJoinRequest.createCriteria().get {
			ilike("contractAddress", contractAddress)
			eq("id", joinRequestId)
		}
		if (c == null) {
			String fmt = "Join request not found by contract address: '%s' and join request id: '%s'"
			String message = String.format(fmt, contractAddress, joinRequestId)
			throw new NotFoundException(message)
		}

		for (Stream s : findStreams(c)) {
			permissionService.systemRevoke(c.user, s, Permission.Operation.STREAM_PUBLISH)
		}
		sendMessage(c, "part")
		c.delete()
	}
}
