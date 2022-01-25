package com.streamr.core.service

import com.streamr.core.domain.Permission
import com.streamr.core.domain.Product
import com.streamr.core.domain.User
import grails.compiler.GrailsCompileStatic
import org.apache.log4j.Logger

class ApiService {

	static transactional = false

	private static final Logger log = Logger.getLogger(ApiService)

	PermissionService permissionService

	/**
	 * List/(search for) all domain objects readable by user that satisfy given conditions. Also validates conditions.
	 *
	 * @param listParams conditions for listing
	 * @param apiUser user for which listing is conducted
	 * @return list of results with pagination information
	 * @throws com.streamr.core.service.ValidationException if listParams does not pass validation
	 */
	@GrailsCompileStatic
	List<Product> list(ListParams listParams, User apiUser) throws ValidationException {
		if (!listParams.validate()) {
			throw new ValidationException(listParams.errors)
		}
		Closure searchCriteria = listParams.createListCriteria()
		User effectiveUser = listParams.grantedAccess ? apiUser : null
		return permissionService.get(effectiveUser, listParams.operationToEnum(), listParams.publicAccess, searchCriteria)
	}

	/**
	 * Fetch a domain object by id while authorizing that current user has required permission
	 */
	@GrailsCompileStatic
	Product authorizedGetById(String id, User currentUser, Permission.Operation operation)
		throws NotFoundException, NotPermittedException {
		Product domainObject = getByIdAndThrowIfNotFound(id)
		permissionService.verify(currentUser, domainObject, operation)
		return domainObject
	}

	/**
	 * Fetch a domain object by id and throw NotFoundException if not found
	 */
	Product getByIdAndThrowIfNotFound(String id) throws NotFoundException {
		Product domainObject = Product.get(id)
		if (domainObject == null) {
			throw new NotFoundException(Product.simpleName, id)
		}
		return domainObject
	}
}
