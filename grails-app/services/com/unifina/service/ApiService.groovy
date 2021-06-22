package com.unifina.service

import com.unifina.domain.Permission
import com.unifina.domain.User
import com.unifina.domain.Userish
import grails.compiler.GrailsCompileStatic
import org.apache.log4j.Logger

class ApiService {

	static transactional = false

	private static final Logger log = Logger.getLogger(ApiService)

	PermissionService permissionService

	/**
	 * List/(search for) all domain objects readable by user that satisfy given conditions. Also validates conditions.
	 *
	 * @param domainClass Class of domain object
	 * @param listParams conditions for listing
	 * @param apiUser user for which listing is conducted
	 * @return list of results with pagination information
	 * @throws ValidationException if listParams does not pass validation
	 */
	@GrailsCompileStatic
	<T> List<T> list(Class<T> domainClass, ListParams listParams, User apiUser) throws ValidationException {
		if (!listParams.validate()) {
			throw new ValidationException(listParams.errors)
		}
		Closure searchCriteria = listParams.createListCriteria()
		User effectiveUser = listParams.grantedAccess ? apiUser : null
		permissionService.get(domainClass, effectiveUser, listParams.operationToEnum(), listParams.publicAccess, searchCriteria)
	}

	/**
	 * Fetch a domain object by id while authorizing that current user has required permission
	 */
	@GrailsCompileStatic
	<T> T authorizedGetById(Class<T> domainClass, String id, Userish currentUser, Permission.Operation operation)
		throws NotFoundException, NotPermittedException {
		T domainObject = getByIdAndThrowIfNotFound(domainClass, id)
		permissionService.verify(currentUser, domainObject, operation)
		return domainObject
	}

	/**
	 * Fetch a domain object by id and throw NotFoundException if not found
	 */
	def <T> T getByIdAndThrowIfNotFound(Class<T> domainClass, String id) throws NotFoundException {
		T domainObject = domainClass.get(id)
		if (domainObject == null) {
			throw new NotFoundException(domainClass.simpleName, id)
		}
		return domainObject
	}
}
