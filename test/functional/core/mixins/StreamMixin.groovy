package core.mixins

import com.unifina.service.CassandraService
import com.unifina.service.KafkaService
import com.unifina.service.StreamService
import core.pages.StreamCreatePage
import core.pages.StreamListPage
import core.pages.StreamShowPage
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsApplication

trait StreamMixin {

	def createStream(streamName) {
		to StreamCreatePage
		name << streamName
		nextButton.click()
		waitFor { at StreamShowPage }
	}

	void withStreamService(Closure c) {
		StreamService ss = createStreamService()
		c(ss)
		cleanupStreamService(ss)
	}

	static StreamService createStreamService() {
		GrailsApplication grailsApplication = new DefaultGrailsApplication() // for config
		StreamService streamService = new StreamService()
		streamService.kafkaService = new KafkaService()
		streamService.kafkaService.grailsApplication = grailsApplication
		streamService.cassandraService = new CassandraService()
		streamService.cassandraService.grailsApplication = grailsApplication
		return streamService
	}

	static void cleanupStreamService(StreamService streamService) {
		streamService.kafkaService.destroy()
		streamService.cassandraService.destroy()
	}

	def deleteFeedFilesUpTo(date){
		$("#history-delete-date").firstElement().clear()
		$("#history-delete-date") << date
		$("#history-delete-button").click()
		waitFor { $(".bootbox.modal").displayed }
		$(".btn", "data-bb-handler":"confirm").click()
	}

	def deleteAllFeedFiles(){
		if ($(".history-end-date").displayed) {
			String endDate = $(".history-end-date").text()
			deleteFeedFilesUpTo(endDate)
		}
		waitFor {$("#no-history-message").displayed}
	}
	
	def deleteFields() {
		$(".delete-field-button").allElements().each {
			it.click()
		}
	}
	
	def openStream(name){
		$(".table .tbody .tr .td", text:contains(name))[0].click()
		waitFor { at StreamShowPage }
	}
	
	void emptyStream(name){
		to StreamListPage
		openStream(name)
		deleteAllFeedFiles()
		$("#configure-fields-button").click()
		waitFor { $("button.save").displayed }
		deleteFields()
		$("button.save").click()
		waitFor { at StreamShowPage }	
	}
	
	void streamExists(name){
		at StreamListPage
		waitFor {
			$(".table .tbody .tr .td", text:contains(name)).displayed
		}
	}
}
