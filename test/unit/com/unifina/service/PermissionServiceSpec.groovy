package com.unifina.service

import spock.lang.Specification

import com.unifina.domain.security.SecUser
import com.unifina.domain.security.Permission

import com.unifina.domain.signalpath.Module
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.Canvas
import com.unifina.domain.signalpath.UiChannel
import com.unifina.domain.dashboard.Dashboard

import com.unifina.utils.IdGenerator
import grails.test.mixin.*
import grails.test.mixin.support.GrailsUnitTestMixin

import java.security.AccessControlException

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(PermissionService)
@Mock([SecUser, Module, ModulePackage, Permission, Dashboard, Canvas, UiChannel])
class PermissionServiceSpec extends Specification {

	SecUser me, anotherUser, stranger

	ModulePackage modPackAllowed, modPackRestricted, modPackOwned
	Module modAllowed, modRestricted, modOwned

	Permission modPackReadPermission

	Dashboard dashAllowed, dashRestricted, dashOwned
	Permission dashReadPermission

	UiChannel uicAllowed, uicRestricted // UiChannels don't have an owner
	Permission uicReadPermission

    def setup() {

		// Users
		me = new SecUser(username: "me", password: "foo", apiKey: "apiKey", apiSecret: "apiSecret").save(validate:false)
		anotherUser = new SecUser(username: "him", password: "bar", apiKey: "anotherApiKey", apiSecret: "anotherApiSecret").save(validate:false)
		stranger = new SecUser(username: "stranger", password: "x", apiKey: "strangeApiKey", apiSecret: "strangeApiSecret").save(validate:false)

		// ModulePackages
		modPackAllowed = new ModulePackage(name:"allowed", user:anotherUser).save(validate:false)
		modPackRestricted = new ModulePackage(name:"restricted", user:anotherUser).save(validate:false)
		modPackOwned = new ModulePackage(name:"owned", user:me).save(validate:false)
		
		// Modules
		modAllowed = new Module(name:"modAllowed", modulePackage:modPackAllowed).save(validate:false)
		modRestricted = new Module(name:"modRestricted", modulePackage:modPackRestricted).save(validate:false)
		modOwned = new Module(name:"modOwned", modulePackage:modPackOwned).save(validate:false)

		// Dashboards
		dashAllowed = new Dashboard(name:"allowed", user:anotherUser).save(validate:false)
		dashRestricted = new Dashboard(name:"restricted", user:anotherUser).save(validate:false)
		dashOwned = new Dashboard(name:"owned", user:me).save(validate:false)

		// Ui channels (have stringId, have no "user")
		def canvas = new Canvas(user: anotherUser).save(validate: false)
		uicAllowed = new UiChannel(id: "allowed_ui_channel", canvas: 1, name:"allowed")
		uicRestricted = new UiChannel(id: "restricted_ui_channel", canvas: 1, name:"restricted")
		uicAllowed.id = IdGenerator.get()
		uicRestricted.id = IdGenerator.get()
		uicAllowed.save(validate: false)
		uicRestricted.save(validate: false)

		// Set up the Permissions to the allowed resources
		modPackReadPermission = service.grant(anotherUser, modPackAllowed, me)
		dashReadPermission = service.grant(anotherUser, dashAllowed, me)
		uicReadPermission = service.systemGrant(me, uicAllowed)
    }

	void "test setup"() {
		expect:
		SecUser.count() == 3
		ModulePackage.count() == 3
		Module.count() == 3
		Dashboard.count() == 3
		UiChannel.count() == 2
		Module.findByModulePackage(modPackAllowed)==modAllowed
		ModulePackage.findAllByUser(anotherUser).size()==2
		ModulePackage.findAllByUser(me).size()==1

		Permission.count()==3
	}

	void "access granted to permitted Dashboard"() {
		expect:
		service.canRead(me, dashAllowed)
	}

	void "access denied to non-permitted Dashboard"() {
		expect:
		!service.canRead(me, dashRestricted)
	}

	void "access granted to own Dashboard"() {
		expect:
		service.canRead(me, dashOwned)
	}

	void "non-permitted third-parties have no access to resources"() {
		expect:
		!service.canRead(stranger, dashAllowed)
		!service.canRead(stranger, dashRestricted)
		!service.canRead(stranger, dashOwned)
	}

	void "canRead returns false on bad inputs"() {
		expect:
		!service.canRead(null, dashAllowed)
		!service.canRead(me, new Dashboard())
		!service.canRead(me, null)
	}

	void "retrieve all readable Dashboards correctly"() {
		expect:
		service.getAllReadable(me, Dashboard) == [dashOwned, dashAllowed]
		service.getAllReadable(anotherUser, Dashboard) == [dashAllowed, dashRestricted]
		service.getAllReadable(stranger, Dashboard) == []
	}

	void "retrieve all readable UiChannels correctly"() {
		expect:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(stranger, UiChannel) == []
	}

	void "grant and revoke work for UiChannels"() {
		when:
		service.systemGrant(anotherUser, uicRestricted)
		then:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(anotherUser, UiChannel) == [uicRestricted]

		when:
		service.systemRevoke(anotherUser, uicRestricted)
		then:
		service.getAllReadable(me, UiChannel) == [uicAllowed]
		service.getAllReadable(anotherUser, UiChannel) == []
	}

	void "getAllReadable returns throws IllegalArgumentException on invalid resource"() {
		when:
		service.getAllReadable(me, java.lang.Object)
		then:
		thrown IllegalArgumentException

		when:
		service.getAllReadable(me, null)
		then:
		thrown IllegalArgumentException
	}

	void "getAllReadable returns public resources on bad/null user"() {
		expect:
		service.getAllReadable(new SecUser(), Dashboard) == []
		service.getAllReadable(null, Dashboard) == []
	}

	void "getAllReadable closure filtering works as expected"() {
		expect:
		service.getAllReadable(me, Dashboard) { like("name", "%ll%") } == [dashAllowed]
	}

	void "getAllShareable closure filtering works as expected"() {
		expect:
		service.getAllShareable(me, Dashboard) == [dashOwned]
		service.getAllShareable(me, Dashboard) { like("name", "%ll%") } == []
	}

	void "granting and revoking read rights"() {
		when:
		service.grant(me, dashOwned, stranger)
		then:
		service.getAllReadable(stranger, Dashboard) == [dashOwned]

		when:
		service.revoke(me, dashOwned, stranger)
		then:
		service.getAllReadable(stranger, Dashboard) == []
	}

	void "grant and revoke throw for non-'share'-access users"() {
		when:
		service.grant(me, dashAllowed, stranger)
		then:
		thrown AccessControlException

		when:
		service.revoke(stranger, dashRestricted, me)
		then:
		thrown AccessControlException

		when:
		service.grant(anotherUser, dashAllowed, me, "share")
		service.revoke(me, dashAllowed, anotherUser)
		then: "Y U try to revoke owner's access?! That should never be generated from the UI!"
		thrown AccessControlException
	}

	void "sharing read rights to others"() {
		when:
		service.grant(me, dashOwned, stranger, "share")
		then:
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		service.getAllShareable(stranger, Dashboard) == [dashOwned]

		expect:
		!(dashOwned in service.getAllReadable(anotherUser, Dashboard))

		when: "stranger shares read access"
		service.grant(stranger, dashOwned, anotherUser)
		then:
		dashOwned in service.getAllReadable(anotherUser, Dashboard)
		!(dashOwned in service.getAllShareable(anotherUser, Dashboard))

		when:
		service.revoke(stranger, dashOwned, anotherUser)
		then:
		!(dashOwned in service.getAllReadable(anotherUser, Dashboard))

		when: "of course, it's silly to revoke 'share' access since it might already been re-shared..."
		service.revoke(me, dashOwned, stranger)
		service.grant(stranger, dashOwned, anotherUser)
		then:
		thrown AccessControlException
	}

	void "revocation is granular"() {
		setup:
		service.grant(me, dashOwned, stranger, "read")
		service.grant(me, dashOwned, stranger, "share")
		when:
		service.revoke(me, dashOwned, stranger, "share")
		then: "only 'share' access is revoked"
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		service.getAllShareable(stranger, Dashboard) == []
	}

	void "default revocation is all access"() {
		setup:
		service.grant(me, dashOwned, stranger, "read")
		service.grant(me, dashOwned, stranger, "share")
		when:
		service.revoke(me, dashOwned, stranger)
		then: "by default, revoke all access"
		service.getAllReadable(stranger, Dashboard) == []
		service.getAllShareable(stranger, Dashboard) == []
	}

	void "granting works (roughly) idempotently"() {
		expect:
		service.getAllReadable(stranger, Dashboard) == []
		when:
		service.grant(me, dashOwned, stranger)
		then: "now you see it..."
		service.getAllReadable(stranger, Dashboard) == [dashOwned]
		when:
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.grant(me, dashOwned, stranger)
		service.revoke(me, dashOwned, stranger)
		then: "now you don't."
		service.getAllReadable(stranger, Dashboard) == []
	}

	//----------------------------------
	// (soon to be) deprecated canAccess methods
	// once these are dumped, also SpringSecurityUtils dependency is gone from tester; no need to defineBeans

	void "granting access to restricted object based supplied user"() {
		expect:
		service.canAccess(modPackOwned, me)
		!service.canAccess(modPackRestricted, me)
		!service.canAccess(modPackOwned, anotherUser)
	}
}
