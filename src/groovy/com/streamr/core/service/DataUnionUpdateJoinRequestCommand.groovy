package com.streamr.core.service

import com.streamr.core.domain.DataUnionJoinRequest
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class DataUnionUpdateJoinRequestCommand {
	String state
	static constraints = {
		state(nullable: false, validator: { String value ->
			return DataUnionJoinRequest.State.isState(value) != null
		})
	}
}
