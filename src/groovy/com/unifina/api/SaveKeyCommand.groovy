package com.unifina.api

import grails.validation.Validateable

@Validateable
class SaveKeyCommand {
	String name
	String username
	String streamId
	String permission
}
