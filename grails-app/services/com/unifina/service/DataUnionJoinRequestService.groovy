package com.unifina.service

import com.streamr.client.dataunion.DataUnion
import com.streamr.client.dataunion.DataUnionClient
import com.streamr.client.dataunion.EthereumTransactionReceipt
import com.unifina.domain.*
import com.unifina.utils.ApplicationConfig
import com.unifina.utils.ThreadUtil
import org.apache.log4j.Logger

class DataUnionJoinRequestService {

	private static final int JOIN_REQUEST_TRANSACTION_POLL_INTERVAL = 1000
	private static final int JOIN_REQUEST_TRANSACTION_TIMEOUT = 30000
	private static final Logger log = Logger.getLogger(DataUnionJoinRequestService)

	EthereumService ethereumService
	PermissionService permissionService
	StreamrClientService streamrClientService

	private isMemberActive(String contractAddress, String memberAddress) {
		DataUnion du = getClient().dataUnionFromMainnetAddress(contractAddress)
		return du.isMemberActive(memberAddress)
	}

	private void onApproveJoinRequest(DataUnionJoinRequest c) {
		log.debug("onApproveJoinRequest: approved JoinRequest for address ${c.memberAddress} to data union ${c.contractAddress}")
		if (isMemberActive(c.contractAddress, c.memberAddress)) {
			log.debug("onApproveJoinRequest: Member ${c.memberAddress} is already active. Skipping.")
			return
		}

		// Grant the member publish permission to streams in the Data Union
		for (Stream s : findStreams(c)) {
			if (permissionService.check(c.user, s, Permission.Operation.STREAM_PUBLISH)) {
				log.debug(String.format("user %s already has write permission to %s (%s), skipping grant", c.user.username, s.name, s.id))
			} else {
				log.debug(String.format("granting write permission to %s (%s) for %s", s.name, s.id, c.user.username))
				permissionService.systemGrant(c.user, s, Permission.Operation.STREAM_GET)
				permissionService.systemGrant(c.user, s, Permission.Operation.STREAM_PUBLISH)
			}
		}

		// Join the member
		// Add the member by calling the sidechain DU directly
		DataUnionClient client = getClient()
		DataUnion du = client.dataUnionFromMainnetAddress(c.contractAddress)
		EthereumTransactionReceipt transactionReceipt = du.addMembers(c.memberAddress)
		String txHash = transactionReceipt.getTransactionHash()
		log.debug("transaction=" + txHash)
		client.waitForSidechainTx(txHash, JOIN_REQUEST_TRANSACTION_POLL_INTERVAL, JOIN_REQUEST_TRANSACTION_TIMEOUT)

		// Check that member status becomes active within a timeout
		final int timeout = 10 * 1000 // ms
		final int interval = 1000 // ms
		int timeSpent
		for (timeSpent = 0; timeSpent < timeout; timeSpent += interval) {
			if (timeSpent > 0) {
				ThreadUtil.sleep(interval);
			}
			if (isMemberActive(c.contractAddress, c.memberAddress)) {
				break
			}
		}
		if (timeSpent >= timeout) {
			throw new DataUnionJoinRequestException("Member ${c.memberAddress} status in Data Union ${c.contractAddress} did not become active within ${timeout} ms")
		}

		log.debug("exiting onApproveJoinRequest")
	}

	protected Set<Stream> findStreams(DataUnionJoinRequest c) {
		log.debug(String.format("entering findStreams(%s)", c))
		List<Product> products = Product.createCriteria().list {
			eq("type", Product.Type.DATAUNION)
			// ilike = case-insensitive like: Ethereum addresses are case-insensitive but different case systems
			// are in use (checksum-case, lower-case at least)
			ilike("beneficiaryAddress", c.contractAddress)
		}
		Set<Stream> streams = new HashSet<>()
		for (Product p : products) {
			streams.addAll(p.streams)
		}
		log.debug(String.format("exiting findStreams(): %s", streams))
		return streams
	}

	Set<User> findMembers(String contractAddress) {
		List<DataUnionJoinRequest> requests = DataUnionJoinRequest.createCriteria().list {
			ilike("contractAddress", contractAddress)
		}
		Set<User> users = new HashSet<>()
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

	DataUnionJoinRequest create(String contractAddress, DataUnionJoinRequestCommand cmd, User user) {
		// TODO CORE-1834: check if user already has a PENDING request
		// TODO CORE-1834: OR if user already has a write permission to the stream

		// Backend must check that the given memberAddress is an Ethereum address bound to the logged in user
		if (user.getUsername().toLowerCase() != cmd.memberAddress.toLowerCase()) {
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
				// ilike = case-insensitive like: Ethereum addresses are case-insensitive but different
				// case systems are in use (checksum-case, lower-case at least)
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

	DataUnionJoinRequest update(String contractAddress, String joinRequestId, DataUnionUpdateJoinRequestCommand cmd) {
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
			permissionService.systemRevoke(c.user, s, Permission.Operation.STREAM_GET)
			permissionService.systemRevoke(c.user, s, Permission.Operation.STREAM_PUBLISH)
		}
		c.delete()
	}

	DataUnionClient getClient() {
		String nodePrivateKey = ApplicationConfig.getString("streamr.ethereum.nodePrivateKey")
		return streamrClientService.getInstanceForThisEngineNode().dataUnionClient(nodePrivateKey, nodePrivateKey)
	}
}
