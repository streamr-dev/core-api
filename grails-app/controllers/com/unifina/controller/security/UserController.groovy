package com.unifina.controller.security

import com.unifina.domain.data.Feed
import com.unifina.domain.security.Key
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.security.SignupInvite
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.exceptions.UserCreationFailedException
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.plugin.springsecurity.authentication.dao.NullSaltSource
import org.springframework.dao.DataIntegrityViolationException

@Secured(["ROLE_ADMIN"])
class UserController {

	def userService
	def userCache
	def springSecurityService
	def permissionService

	static defaultAction = 'userSearch'

	def create() {
		def user = lookupUserClass().newInstance(params)
		[user: user, authorityList: sortedRoles()]
	}

    def save() {
		List<SecRole> roles = SecRole.findAllByAuthorityInList(params.list("role"))
		List<Feed> feeds = Feed.findAllByIdInList(params.list("feed").collect{ Long.parseLong(it) })
		List<ModulePackage> packages = ModulePackage.findAllByIdInList(params.list("modulePackage").collect{ Long.parseLong(it) })
		def user
        try {
            user = userService.createUser(params, roles, feeds, packages)
			redirect action: 'search'
        } catch (UserCreationFailedException e) {
			flash.error = e.getMessage()
			redirect action: "create"
        }
    }

	def search() {
		[enabled: 0, accountExpired: 0, accountLocked: 0, passwordExpired: 0]
	}

	def userSearch() {

		boolean useOffset = params.containsKey('offset')
		setIfMissing 'max', 10, 100
		setIfMissing 'offset', 0

		def hql = new StringBuilder('FROM ').append(lookupUserClassName()).append(' u WHERE 1=1 ')
		def queryParams = [:]

		def userLookup = SpringSecurityUtils.securityConfig.userLookup
		String usernameFieldName = userLookup.usernamePropertyName

		for (name in [username: usernameFieldName]) {
			if (params[name.key]) {
				hql.append " AND LOWER(u.${name.value}) LIKE :${name.key}"
				queryParams[name.key] = params[name.key].toLowerCase() + '%'
			}
		}

		String enabledPropertyName = userLookup.enabledPropertyName
		String accountExpiredPropertyName = userLookup.accountExpiredPropertyName
		String accountLockedPropertyName = userLookup.accountLockedPropertyName
		String passwordExpiredPropertyName = userLookup.passwordExpiredPropertyName

		for (name in [enabled: enabledPropertyName,
					  accountExpired: accountExpiredPropertyName,
					  accountLocked: accountLockedPropertyName,
					  passwordExpired: passwordExpiredPropertyName]) {
			Integer value = params.int(name.key)
			if (value) {
				hql.append " AND u.${name.value}=:${name.key}"
				queryParams[name.key] = value == 1
			}
		}

		int totalCount = lookupUserClass().executeQuery("SELECT COUNT(DISTINCT u) $hql", queryParams)[0]

		Integer max = params.int('max')
		Integer offset = params.int('offset')

		String orderBy = ''
		if (params.sort) {
			orderBy = " ORDER BY u.$params.sort ${params.order ?: 'ASC'}"
		}

		def results = lookupUserClass().executeQuery(
				"SELECT DISTINCT u $hql $orderBy",
				queryParams, [max: max, offset: offset])
		def model = [results: results, totalCount: totalCount, searched: true]

		// add query params to model for paging
		for (name in ['username', 'enabled', 'accountExpired', 'accountLocked',
					  'passwordExpired', 'sort', 'order']) {
			model[name] = params[name]
		}

		render view: 'search', model: model
	}

	def ajaxUserSearch() {

		def jsonData = []

		if (params.term?.length() > 2) {
			String username = params.term
			String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

			setIfMissing 'max', 10, 100

			def results = lookupUserClass().executeQuery(
					"SELECT DISTINCT u.$usernameFieldName " +
							"FROM ${lookupUserClassName()} u " +
							"WHERE LOWER(u.$usernameFieldName) LIKE :name " +
							"ORDER BY u.$usernameFieldName",
					[name: "${username.toLowerCase()}%"],
					[max: params.max])

			for (result in results) {
				jsonData << [value: result]
			}
		}

		render text: jsonData as JSON, contentType: 'text/plain'
	}

	def update() {
		def user = findById()
		if (!user) return
		if (!versionCheck('user.label', 'User', user, [user: user])) {
			return
		}

		def oldPassword = user.password
		user.properties = params

		// If the password is left unchanged, params.password contains the old (hashed) password
		if (params.password && !params.password.equals(oldPassword)) {
			user.password = springSecurityService.encodePassword(params.password)
		}

		if (!user.save(flush: true)) {
			render view: 'edit', model: buildUserModel(user)
			return
		}

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

		lookupUserRoleClass().removeAll user

		def roles = SecRole.findAllByAuthorityInList(params.list("role"))
		userService.addRoles(user, roles)
		def feeds = Feed.findAllByIdInList(params.list("feed").collect{ Long.parseLong(it) })
		userService.setFeeds(user, feeds)
		def packages = ModulePackage.findAllByIdInList(params.list("modulePackage").collect{ Long.parseLong(it) })
		userService.setModulePackages(user, packages)
		userCache.removeUserFromCache user[usernameFieldName]
		flash.message = "${message(code: 'default.updated.message', args: [message(code: 'user.label', default: 'User'), user.id])}"
		redirect action: 'edit', id: user.id
	}

	protected findById() {
		def user = lookupUserClass().get(params.id)
		if (!user) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: 'search'
		}

		user
	}

	def edit() {
		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName

		def user = params.username ? lookupUserClass().findWhere((usernameFieldName): params.username) : null
		if (!user) user = findById()
		if (!user) return

		return buildUserModel(user)
	}

	protected Map buildUserModel(user) {

		String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
		String authoritiesPropertyName = SpringSecurityUtils.securityConfig.userLookup.authoritiesPropertyName

		List roles = sortedRoles()
		Set userRoleNames = user[authoritiesPropertyName].collect { it[authorityFieldName] }
		def granted = [:]
		def notGranted = [:]
		for (role in roles) {
			String authority = role[authorityFieldName]
			if (userRoleNames.contains(authority)) {
				granted[(role)] = userRoleNames.contains(authority)
			}
			else {
				notGranted[(role)] = userRoleNames.contains(authority)
			}
		}

		return [
			user: user,
			authorityList: sortedRoles(),
			roleMap: granted + notGranted,
			userModulePackages: permissionService.get(ModulePackage, user),
			userFeeds: permissionService.get(Feed, user)
		]
	}
    
    def delete() {
        def user = findById()
        if (!user) return

		Key.executeUpdate("delete from Key k where k.user = ?", [user])
        SecUserSecRole.executeUpdate("delete from SecUserSecRole ss where ss.secUser = ?", [user])
        Permission.executeUpdate("delete from Permission p where p.user = ?", [user])
        SignupInvite.executeUpdate("delete from SignupInvite si where si.username = ?", [user.username])

		String usernameFieldName = SpringSecurityUtils.securityConfig.userLookup.usernamePropertyName
		try {
			lookupUserRoleClass().removeAll user
			user.delete flush: true
			userCache.removeUserFromCache user[usernameFieldName]
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: 'search'
		}
		catch (DataIntegrityViolationException e) {
			flash.error = "${message(code: 'default.not.deleted.message', args: [message(code: 'user.label', default: 'User'), params.id])}"
			redirect action: 'edit', id: params.id
		}
    }

	protected List sortedRoles() {
		lookupRoleClass().list().sort { it.authority }
	}

	protected void setIfMissing(String paramName, long valueIfMissing, Long max = null) {
		long value = (params[paramName] ?: valueIfMissing).toLong()
		if (max) {
			value = Math.min(value, max)
		}
		params[paramName] = value
	}

	protected String lookupUserClassName() {
		SpringSecurityUtils.securityConfig.userLookup.userDomainClassName
	}

	protected Class<?> lookupUserClass() {
		grailsApplication.getDomainClass(lookupUserClassName()).clazz
	}

	protected String lookupRoleClassName() {
		SpringSecurityUtils.securityConfig.authority.className
	}

	protected Class<?> lookupRoleClass() {
		grailsApplication.getDomainClass(lookupRoleClassName()).clazz
	}

	protected String lookupUserRoleClassName() {
		SpringSecurityUtils.securityConfig.userLookup.authorityJoinClassName
	}

	protected Class<?> lookupUserRoleClass() {
		grailsApplication.getDomainClass(lookupUserRoleClassName()).clazz
	}

	protected boolean versionCheck(String messageCode, String messageCodeDefault, instance, model) {
		if (params.version) {
			def version = params.version.toLong()
			if (instance.version > version) {
				instance.errors.rejectValue('version', 'default.optimistic.locking.failure',
						[message(code: messageCode, default: messageCodeDefault)] as Object[],
						"Another user has updated this instance while you were editing")
				render view: 'edit', model: model
				return false
			}
		}
		true
	}
}
