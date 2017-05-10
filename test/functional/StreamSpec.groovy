import com.unifina.domain.data.Stream
import com.unifina.feed.mongodb.MongoDbConfig
import com.unifina.service.StreamService
import core.LoginTester1Spec
import core.mixins.ConfirmationMixin
import core.mixins.StreamMixin
import core.pages.*
import org.bson.Document
import spock.lang.Shared

import java.nio.file.Paths

class StreamSpec extends LoginTester1Spec {

	@Shared def mongoDbConfig = new MongoDbConfig([
		host: "dev.streamr",
		port: 27017,
		database: "test",
		collection: "streamSpec",
		timestampKey: "time",
		timestampType: MongoDbConfig.TimestampType.DATETIME
	])

	@Shared StreamService streamService

	def setupSpec() {
		// @Mixin is buggy, don't use it
		StreamSpec.metaClass.mixin(StreamMixin)
		StreamSpec.metaClass.mixin(ConfirmationMixin)

		streamService = createStreamService()

		def client = mongoDbConfig.createMongoClient()
		mongoDbConfig.openCollection().drop()
		client.getDatabase("test").createCollection("streamSpec")
		mongoDbConfig.openCollection().insertOne(new Document([a: "hello world", b: 10, c: new Date(0)]))
	}

	def cleanupSpec() {
		mongoDbConfig.openCollection().drop()
		cleanupStreamService(streamService)
	}
	
	private File getFile(String filename) {
		// The test csv files must be available in the local filesystem of the machine where the browser is running.
		// Note that it's impossible to check here whether the file exists because this code runs on a different machine.
		boolean inJenkins = (System.getenv('BUILD_NUMBER') != null)
		return inJenkins ? new File("/vagrant/$filename") : Paths.get(getClass().getResource("files/$filename").toURI()).toFile() 
	}
	
	void "removing data from a stream works"() {
		setup:
			to StreamListPage
			openStream("CSVImporterFuncSpec")
			waitFor { at StreamShowPage }
			fileInput = getFile("test-upload-file.csv")
			waitFor {
				at StreamShowPage
				historyDeleteButton.displayed
				historyStartDate.text() == "2015-02-23"
				historyEndDate.text() == "2015-02-25"
			}
		
		when: "only one day's feed files are removed"
			deleteFeedFilesUpTo("2015-02-23")
		then: "The stream has no data from that day anymore"
			waitFor { historyStartDate.text() == "2015-02-24" }

		when: "rest of the feed files are deleted"
			deleteAllFeedFiles()
		then: "the stream has no data anymore"
			waitFor { noHistoryMessage.displayed }

		
		when: "Clicked to go to the configure view and deleted the fields"
			configureFieldsButton.click()
		waitFor { $(".delete-field-button").size() == 4 }
			deleteFields()
		then: "There are no fields anymore"
			waitFor { $(".delete-field-button").size() == 0 }
		
		when: "Saved"
			$("button.save").click()
		then: "Go to StreamShowPage, no configured fields"
			waitFor { at StreamShowPage }
			waitFor { $("div.alert.alert-info").displayed }
	}
	
	void "creating streams and autodetecting fields"() {
		setup:
			def streamName = "StreamSpec"+System.currentTimeMillis()
			to StreamListPage
			waitFor { at StreamListPage }
		
		when: "create stream button is clicked"
			createButton.click()
		then: "must go to stream create page"
			waitFor { at StreamCreatePage }
			
		when: "name and desc are entered and next button is clicked"
			name << streamName
			description << streamName + " description"
			nextButton.click()
		then: "must navigate to stream show page, showing info about non-configured stream"
			waitFor { at StreamShowPage }
			$(".alert-info", text: contains('configure'))

		when: "Configure Fields button is clicked"
			def streamId = $(".stream-id").text()
			configureFieldsButton.click()
		then: "Navigate to configure page"
			waitFor { at StreamConfigurePage }

		when: "Produce an event into the stream and click autodetect button"
			Stream testStream = new Stream()
			testStream.id = streamId
			streamService.sendMessage(testStream, [foo: "bar", "xyz": 45.5], 30)
			sleep(1000)
			autodetectButton.click()
		then: "The fields in the stream must appear and be of correct type"
			waitFor {
					$("input", name:"field_name").size() == 2
					$("select", name:"field_type").size() == 2
					$("select", name:"field_type").getAt(0).value() == "string"
					$("select", name:"field_type").getAt(1).value() == "number"
					$(".delete-field-button").size() == 2
			}
			
		when: "save button is clicked"
			saveButton.click()
		then: "navigate back to show page, showing the fields and message"
			waitFor { at StreamShowPage }
			$(".alert-info").displayed
			$("#stream-fields tbody tr").size() == 2

		when: "open menu"
			streamMenuButton.click()
		then: "delete in menu"
			waitFor { deleteStreamButton.displayed }

		when: "delete stream button is clicked"
			deleteStreamButton.click()
		then: "must show confirmation"
			waitForConfirmation()
			
		when: "confirmation accepted"
			acceptConfirmation()
		then: "must navigate to list page and show message"
			waitFor { at StreamListPage }
	}

	void "creating a mongo stream and autodetecting its fields work"() {
		setup:
		def streamName = "StreamSpec_Mongo_"+System.currentTimeMillis()
		to StreamListPage
		waitFor { at StreamListPage }

		when: "create stream button is clicked"
		createButton.click()
		then: "must go to stream create page"
		waitFor { at StreamCreatePage }

		when: "name, description are entered, mongo chosen from list, and then next button is clicked"
		name << streamName
		description << streamName + " description"
		feed.click()
		feed.find("option").find { it.value() == "8" }.click()
		nextButton.click()

		then: "must navigate to stream show page, showing info about non-configured stream"
		waitFor { at StreamShowPage }
		$(".alert-info", text: contains('configure'))

		when: "mongodb edit button is clicked"
		editMongoDbButton.click()
		then: "arrive at mongodb configuration page"
		waitFor { at ConfigureMongoPage }

		when: "filling in configuration details"
		host << "dev.streamr"
		port.value("27017")
		username << ""
		password << ""
		database << "test"
		collection << "streamSpec"
		timestampKey << "time"
		pollIntervalMillis.value("5500")
		query.value("{}")

		and: "pressing submit"
		submit.click()

		then: "back at stream show page"
		waitFor { at StreamShowPage }

		and: "mongodb configurations visible"
		mongoHost.text() == "dev.streamr"
		mongoPort.text() == "27017"
		mongoUsername.text().empty
		mongoPassword.text().empty
		mongoDatabase.text() == "test"
		mongoCollection.text() == "streamSpec"
		mongoTimestampKey.text() == "time (datetime)"
		mongoPollIntervalMillis.text() == "5500"
		mongoQuery.text() == "{}"

		when: "Configure Fields button is clicked"
		configureFieldsButton.click()
		then: "Navigate to configure page"
		waitFor { at StreamConfigurePage }

		when: "Autodetect button is clicked"
		autodetectButton.click()
		then:
		waitFor {
			$("input", name: "field_name").size() == 4 && $("select", name: "field_type").size() == 4
		}
		$("input", name:"field_name").collect { it.value() } == ["_id", "a", "b", "c"]
		$("select", name:"field_type").collect { it.value() } == ["string", "string", "number", "string"]

		cleanup: "delete stream"
		to(StreamListPage)
		openStream(streamName)
		streamMenuButton.click()
		waitFor { deleteStreamButton.displayed }
		deleteStreamButton.click()
		waitFor { $(".modal-dialog").displayed }
		$(".modal-dialog .btn-primary").click()
		waitFor { at StreamListPage }
	}
	
}


