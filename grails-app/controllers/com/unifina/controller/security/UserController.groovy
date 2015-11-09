package com.unifina.controller.security

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.user.UserCreationFailedException
import grails.plugin.springsecurity.annotation.Secured

@Secured(["ROLE_ADMIN"])
class UserController extends grails.plugin.springsecurity.ui.UserController {

	def userService
	
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
    
    @Override
    def save() {
        def par = params
		List<SecRole> roles = SecRole.findAllByAuthorityInList(params.list("role"))
		List<Feed> feeds = Feed.findAllByIdInList(params.list("feed"))
		List<ModulePackage> packages = ModulePackage.findAllByIdInList(params.list("modulePackage"))
		def user
        try {
            user = userService.createUser(params, roles, feeds, packages)
			redirect action: 'search'
        } catch (UserCreationFailedException e) {
			flash.error = e.getMessage()
			redirect action: "create"
        }
    }
    
    @Override
    def delete() {
        def user = findById()
        if (!user) return
        
        SecUserSecRole.executeUpdate("delete from SecUserSecRole ss where ss.secUser = ?", [user])
        FeedUser.executeUpdate("delete from FeedUser fu where fu.user = ?", [user])
        ModulePackageUser.executeUpdate("delete from ModulePackageUser mu where mu.user = ?", [user])
        SignupInvite.executeUpdate("delete from SignupInvite si where si.username = ?", [user.username])
        
        super.delete()
    }
}
