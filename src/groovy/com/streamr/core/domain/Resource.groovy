package com.streamr.core.domain
import com.streamr.core.service.NotFoundException
import com.streamr.core.service.NotPermittedException
import com.streamr.core.service.PermissionService
import grails.compiler.GrailsCompileStatic
import grails.util.Holders
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString
@GrailsCompileStatic
@EqualsAndHashCode
class Resource {
	Class<?> clazz = Product
	Object id

	Resource(Object id) {
		if (!id) {
			throw new IllegalArgumentException("Missing resource id")
		}
		this.id = id
	}

	Product load(User apiUser, boolean requireShareResourcePermission) {
		Product resource = Product.get(idToString())
		if (resource == null) {
			throw new NotFoundException(clazz.simpleName, idToString())
		}
		Permission.Operation shareOp = Permission.Operation.PRODUCT_SHARE
		PermissionService permissionService = Holders.getApplicationContext().getBean(PermissionService)
		if (requireShareResourcePermission && !permissionService.check(apiUser, resource, shareOp)) {
			throw new NotPermittedException(apiUser?.username, clazz.simpleName, idToString(), shareOp.id)
		}
		return resource
	}

	String idToString() {
		return id?.toString()
	}
}
