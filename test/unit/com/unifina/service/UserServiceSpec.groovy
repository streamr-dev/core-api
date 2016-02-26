package com.unifina.service

import com.unifina.domain.data.Feed
import com.unifina.domain.data.FeedUser
import com.unifina.domain.security.SecRole
import com.unifina.domain.security.SecUser
import com.unifina.domain.security.SecUserSecRole
import com.unifina.domain.signalpath.ModulePackage
import com.unifina.domain.signalpath.ModulePackageUser
import com.unifina.domain.signalpath.Module
import com.unifina.feed.NoOpStreamListener
import com.unifina.user.UserCreationFailedException
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.springframework.security.authentication.encoding.PlaintextPasswordEncoder
import spock.lang.Specification

import javax.swing.Spring

@TestFor(UserService)
@Mock([Feed, SecUser, SecRole, ModulePackage, ModulePackageUser, SecUserSecRole, FeedUser, Module])
class UserServiceSpec extends Specification {

    void createData(){
        // A feed created with minimum fields required
        Feed feed = new Feed()
        feed.id = new Long(7)
        feed.name = "testFeed"
        feed.eventRecipientClass = ""
        feed.keyProviderClass = ""
        feed.messageSourceClass = ""
        feed.module = new Module()
        feed.parserClass = ""
        feed.timezone = "Europe/Minsk"
		feed.streamListenerClass = NoOpStreamListener.name
		feed.streamPageTemplate = ""
        feed.save(failOnError: true)

        // A modulePackage created with minimum fields required
        def modulePackage = new ModulePackage()
        modulePackage.id = new Long(1)
        modulePackage.name = "test"
        modulePackage.user = new SecUser()
        modulePackage.save()

        def modulePackage2 = new ModulePackage()
        modulePackage2.id = new Long(2)
        modulePackage2.name = "test2"
        modulePackage2.user = new SecUser()
        modulePackage2.save()

        // The roles created
        ["ROLE_USER","ROLE_LIVE","ROLE_ADMIN"].each {
            def role = new SecRole()
            role.authority = it
            role.save()
        }
    }

    def setup() {
        defineBeans {
            passwordEncoder(PlaintextPasswordEncoder)
            springSecurityService(SpringSecurityService)
        }
        // Do some wiring that should be done automatically but for some reason is not (in unit tests)
        grailsApplication.mainContext.getBean("springSecurityService").grailsApplication = grailsApplication
        grailsApplication.mainContext.getBean("springSecurityService").passwordEncoder = grailsApplication.mainContext.getBean("passwordEncoder")
    }

    def "the user is created when called"() {
        when:
        createData()
        service.createUser([username: "test@test.com", name:"test", password: "test", timezone:"Europe/Minsk", enabled:true, accountLocked:false, passwordExpired:false])

        then:
        SecUser.count() == 1
    }

    def "if no roles, feeds or modulePackages are given, it should use the default ones"() {
        when:
        createData()
        SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", timezone:"Europe/Minsk", enabled:true, accountLocked:false, passwordExpired:false])

        then:
        user.getAuthorities().size() == 2
        user.getAuthorities().toArray()[0].authority == "ROLE_USER"
        user.getAuthorities().toArray()[1].authority == "ROLE_LIVE"

        user.getModulePackages().size() == 1
        user.getModulePackages().toArray()[0].id == 1

        user.getFeeds().size() == 1
        user.getFeeds().toArray()[0].id == 7
    }

    def "if the roles, feeds and modulePackages are given, it should use them"() {
        when:
        createData()
        SecUser user = service.createUser([
                username       : "test@test.com",
                name           : "test",
                password       : "test",
                timezone       : "Europe/Minsk",
                enabled        : true,
                accountLocked  : false,
                passwordExpired: false
        ],
                SecRole.findAllByAuthorityInList(["ROLE_USER"]),
                new ArrayList<Feed>(),
                ModulePackage.findAllByIdInList([new Long(1), new Long(2)])
        )

        then:
        user.getAuthorities().size() == 1
        user.getAuthorities().toArray()[0].authority == "ROLE_USER"

        user.getFeeds().size() == 0

        user.getModulePackages().size() == 2
        user.getModulePackages().toArray()[0].id == 1
        user.getModulePackages().toArray()[1].id == 2

    }

    def "it should fail if the default roles, feeds of modulePackages are not found"() {
        when:
        // The data has not been created
        SecUser user = service.createUser([username: "test@test.com", name:"test", password: "test", timezone:"Europe/Minsk", enabled:true, accountLocked:false, passwordExpired:false])

        then:
        thrown RuntimeException
    }
}
