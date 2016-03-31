package com.unifina.service

import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.api.SaveDashboardCommand
import com.unifina.domain.dashboard.Dashboard
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import groovy.transform.CompileStatic

class DashboardService {

	PermissionService permissionService

	List<Dashboard> findAllDashboards(SecUser user) {
		return permissionService.getAll(Dashboard, user) { order "dateCreated", "desc" }
	}

	/**
	 * Find Dashboard by id and authorize that user is permitted to read it.
	 * @param id dashboard id
	 * @param user current user
	 * @return found dashboard
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to read it
	 */
	@CompileStatic
	Dashboard findById(Long id, SecUser user) throws NotFoundException, NotPermittedException {
		return authorizedGetById(id, user, Permission.Operation.READ)
	}

	/**
	 * Delete Dashboard by id and authorize that user is permitted to do so.
	 * @param id dashboard id
	 * @param user current user
	 * @return found dashboard
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to delete it
	 */
	@CompileStatic
	void deleteById(Long id, SecUser user) throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(id, user, Permission.Operation.WRITE)
		dashboard.delete()
	}

	/**
	 * Update Dashboard by id and command, and authorize that user is permitted to do so.
	 * @param id dashboard id
	 * @param validCommand a save command that has been validated before
	 * @param user current user
	 * @return updated dashboard
	 * @throws NotFoundException when dashboard was not found.
	 * @throws NotPermittedException when dashboard was found but user not permitted to update it
	 */
	@CompileStatic
	Dashboard update(Long id, SaveDashboardCommand validCommand, SecUser user)
			throws NotFoundException, NotPermittedException {
		def dashboard = authorizedGetById(id, user, Permission.Operation.WRITE)
		dashboard.name = validCommand.name
		dashboard.save(failOnError: true)
	}


	@CompileStatic
	private Dashboard authorizedGetById(Long id, SecUser user, Permission.Operation operation) {
		def dashboard = Dashboard.get(id)
		if (!dashboard) {
			throw new NotFoundException(Dashboard.simpleName, id.toString())
		}
		if (!permissionService.check(user, dashboard, operation)) {
			throw new NotPermittedException(user.username, Dashboard.simpleName, id.toString())
		}
		return dashboard
	}
}
