package com.unifina.api

import com.unifina.controller.api.CommunityProductApiController
import grails.validation.Validateable
import groovy.transform.ToString

@Validateable
@ToString
class UpdateCommunityJoinRequestCommand {
	String state
	static constraints = {
		state(nullable: false, validator: { String value ->
			return CommunityProductApiController.isState(value) != null
		})
	}
}
