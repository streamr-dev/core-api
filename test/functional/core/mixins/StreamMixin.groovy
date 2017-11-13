package core.mixins

import com.unifina.service.CassandraService
import com.unifina.service.KafkaService
import com.unifina.service.StreamService
import core.pages.StreamListPage
import core.pages.StreamShowPage
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.converters.configuration.ConverterConfiguration
import org.codehaus.groovy.grails.web.converters.configuration.ConvertersConfigurationHolder
import org.codehaus.groovy.grails.web.converters.configuration.DefaultConverterConfiguration
import org.codehaus.groovy.grails.web.converters.marshaller.json.CollectionMarshaller
import org.codehaus.groovy.grails.web.converters.marshaller.json.GenericJavaBeanMarshaller
import org.codehaus.groovy.grails.web.converters.marshaller.json.MapMarshaller

class StreamMixin {

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
		$(".table .tbody .tr .td", text:contains(name)).click()
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
