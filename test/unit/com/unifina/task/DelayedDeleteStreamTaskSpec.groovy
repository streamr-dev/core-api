package com.unifina.task

import com.unifina.domain.data.Stream
import com.unifina.domain.task.Task
import com.unifina.service.StreamService
import grails.test.mixin.Mock
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.context.ApplicationContext
import spock.lang.Specification

@Mock([Stream])
class DelayedDeleteStreamTaskSpec extends Specification {

	StreamService streamService
	GrailsApplication grailsApplication
	List<Stream> streams

    def setup() {
		streamService = Mock(StreamService)
		grailsApplication = Mock(GrailsApplication)
		grailsApplication.getMainContext() >> {
			def ctx = Mock(ApplicationContext)
			ctx.getBean(StreamService) >> streamService
			return ctx
		}

		streams = (1..3).collect { new Stream().save(validate: false) }
    }

	void "DelayedDeleteStreamTask must delete the Streams"() {
		DelayedDeleteStreamTask task = new DelayedDeleteStreamTask(
				new Task(),
				DelayedDeleteStreamTask.getConfig(streams),
				grailsApplication)

		when:
		task.run()

		then:
		3 * streamService.deleteStream(_)
	}

}
