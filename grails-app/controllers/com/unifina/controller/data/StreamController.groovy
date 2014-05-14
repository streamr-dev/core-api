package com.unifina.controller.data

import grails.converters.JSON

import org.hibernate.criterion.CriteriaSpecification

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Module

class StreamController {

	def springSecurityService
	
	def search() {
		Set<Feed> allowedFeeds = springSecurityService.currentUser?.feeds ?: new HashSet<>()
		List<Map> streams = []

		if (!allowedFeeds.isEmpty()) {
			streams = Stream.createCriteria().list {
				resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
				projections {
					property("id","id")
					property("name","name")
					property("module.id","module")
				}
				createAlias('feed', 'feed', CriteriaSpecification.LEFT_JOIN)
				createAlias('feed.module', 'module', CriteriaSpecification.LEFT_JOIN)
				if (params.feed) {
					eq("feed", Feed.load(params.feed))
				}
				if (params.module) {
					eq("feed.module", Module.load(params.module))
				}
				like("name","%"+params.term+"%")
				'in'("feed",allowedFeeds)
				maxResults(10)
			}
		}
		
		render streams as JSON
	}

}
