package com.unifina.controller.tour

import com.unifina.domain.tour.TourUser
import grails.converters.JSON

class TourUserController {

	def springSecurityService

	static allowedMethods = [completed: "POST"]

	def completed() {
		def tu = new TourUser()
		tu.user = springSecurityService.currentUser
		tu.tourNumber = params.int("tourNumber")
		tu.completedAt = new Date()
		tu.save(flush: true, failOnError: true)
		render ""
	}

	def list() {
		def result = []
		TourUser.findAll {
			user == springSecurityService.currentUser
		}.each { tu ->
			result.add(tu.tourNumber)
		}
		render result as JSON
	}
}
