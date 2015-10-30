package com.unifina.controller.security

import grails.plugin.springsecurity.annotation.Secured

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser

@Secured(["ROLE_ADMIN"])
class UserController extends grails.plugin.springsecurity.ui.UserController {
	
	@Override
	protected void addRoles(user) {
		super.addRoles(user)
		addPackages(user)
	}
	
	protected void addPackages(user) {
		List selectedPackages = params.list("modulePackage").collect {ModulePackage.get(Long.parseLong(it))}
		List allPackages = ModulePackage.list()
		Set userPackages = user.modulePackages
		
		allPackages.each {ModulePackage mp->
			if (userPackages.contains(mp) && !selectedPackages.contains(mp)) {
				ModulePackageUser.findByUserAndModulePackage(user,mp).delete()
			}
			else if (!userPackages.contains(mp) && selectedPackages.contains(mp)) {
				ModulePackageUser modulePackageUser = new ModulePackageUser([user:user,modulePackage:mp])
				modulePackageUser.save()
			}
		}
		
		List selectedFeeds = params.list("feed").collect {Feed.get(Long.parseLong(it))}
		List allFeeds = Feed.list()
		Set userFeeds = user.feeds
		
		allFeeds.each {Feed f->
			if (userFeeds.contains(f) && !selectedFeeds.contains(f)) {
				FeedUser.findByUserAndFeed(user,f).delete()
			}
			else if (!userFeeds.contains(f) && selectedFeeds.contains(f)) {
				FeedUser feedUser = new FeedUser([user:user,feed:f])
				feedUser.save()
			}
		}
	}
}
