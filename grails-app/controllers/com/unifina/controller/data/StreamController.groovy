package com.unifina.controller.data

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import org.hibernate.criterion.CriteriaSpecification

import com.unifina.domain.data.Feed
import com.unifina.domain.data.Stream
import com.unifina.domain.signalpath.Module

@Secured(["ROLE_USER"])
class StreamController {

	def springSecurityService
	
	def search() {
		Set<Feed> allowedFeeds = springSecurityService.currentUser?.feeds ?: new HashSet<>()
		List<Map> streams = []

		if (!allowedFeeds.isEmpty()) {
			String hql = "select new map(s.id as id, s.name as name, s.feed.module.id as module, s.description as description) from Stream s "+
				"left outer join s.feed "+
				"left outer join s.feed.module "+
				"where (s.name like '"+params.term+"%' or s.description like '"+params.term+"%') "
				"and s.feed.id in ("+allowedFeeds.collect{ feed -> feed.id }.join(',')+") "

				if (params.feed) {
					hql += " and s.feed.id="+Feed.load(params.feed).id
				}

				if (params.module) {
					hql += " and s.feed.module.id="+Module.load(params.module).id
				}

				hql += " order by length(s.name), s.id asc"
		
				streams = Stream.executeQuery(hql, [ max: 10 ])
		}

		render streams as JSON
	}

}
