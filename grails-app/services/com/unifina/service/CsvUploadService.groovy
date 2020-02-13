package com.unifina.service

import com.streamr.client.StreamrClient
import com.unifina.api.CsvParseInstructions
import com.unifina.api.NotFoundException
import com.unifina.api.NotPermittedException
import com.unifina.domain.data.Stream
import com.unifina.domain.security.Permission
import com.unifina.domain.security.SecUser
import com.unifina.utils.CSVImporter
import com.unifina.utils.IdGenerator
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import groovy.transform.CompileStatic

@GrailsCompileStatic
class CsvUploadService {
	static scope = "singleton"

	private static final long MIN_NANOS_PER_MSG = (Math.pow(10,9) / 500).longValue() // 10^9 nanos per second divided by 500 messages per second

	ApiService apiService
	StreamService streamService
	StreamrClientService streamrClientService

	IdGenerator idGenerator = new IdGenerator()

	Map<String, String> idToFilePath = new HashMap<>()

	Map uploadCsvFile(File file, String streamId, SecUser user) throws NotFoundException, NotPermittedException {
		Stream stream = apiService.authorizedGetById(Stream, streamId, user, Permission.Operation.STREAM_PUBLISH)

		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])
		List fields = (List) (config.fields ? config.fields : [])

		// Attempt to auto-detect timestamp field
		CSVImporter csv = new CSVImporter(file, fields, null, null, "UTC")
		Map schema = csv.getSchema().toMap()

		String fileId = idGenerator.generate()
		idToFilePath.put(fileId, file.canonicalPath)

		return [
			fileId: fileId,
			schema: schema
		]
	}

	Stream parseAndConsumeCsvFile(CsvParseInstructions instructions, String streamId, SecUser user)
			throws NotFoundException, NotPermittedException {
		Stream stream = apiService.authorizedGetById(Stream, streamId, user, Permission.Operation.STREAM_PUBLISH)

		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])
		List fields = (List) (config.fields ? config.fields : [])

		String filePath = idToFilePath.get(instructions.fileId)
		if (!filePath) {
			throw new NotFoundException(String.format("File not found with identifier '%s'", instructions.fileId))
		}
		File file = new File(filePath)

		try {
			CSVImporter csv = new CSVImporter(file, fields, instructions.timestampColumnIndex, instructions.dateFormat)
			Map newStreamConfig = importCsv(csv, stream, user)
			stream.config = (newStreamConfig as JSON)
			return stream.save()
		} finally {
			boolean fileDeleted = file.delete()
			if (!fileDeleted) {
				log.error(String.format("error deleting uploaded csv file: %s", filePath))
			}
			idToFilePath.remove(instructions.fileId)
		}
	}

	/**
	 * Imports data from a csv file into a Stream.
	 * @param csv
	 * @param stream
	 * @return Autocreated Stream field config as a Map (can be written to stream.config as JSON)
	 */
	@CompileStatic
	private Map importCsv(CSVImporter csv, Stream stream, SecUser user) {
		StreamrClient streamrClient = streamrClientService.getAuthenticatedInstance(user.id)
		com.streamr.client.rest.Stream clientStream = streamrClient.getStream(stream.id)

		for (CSVImporter.LineValues line : csv) {
			long startTime = System.nanoTime()
			Date date = line.getTimestamp()

			// Write all fields into the message except for the timestamp column
			Map<String, Object> message = [:]
			for (int i=0; i<line.values.length; i++) {
				if (i!=line.schema.timestampColumnIndex && line.values[i]!=null) {
					String name = line.schema.entries[i].name
					message[name] = line.values[i]
				}
			}

			log.info("importCsv: publishing " + message)

			streamrClient.publish(clientStream, message, date)

			// Throttle the upload speed so that each message takes at least MIN_NANOS_PER_MSG
			long timeSpentNanos = System.nanoTime() - startTime
			if (timeSpentNanos < MIN_NANOS_PER_MSG) {
				long needToSleepNanos = MIN_NANOS_PER_MSG - timeSpentNanos
				long needToSleepMillis = (long) Math.pow(needToSleepNanos, -6)
				Thread.sleep(needToSleepMillis, (int)(needToSleepNanos % Math.pow(10, 6)))
			}
		}

		// Autocreate the stream config based on fields in the csv schema
		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])

		List fields = []

		// The primary timestamp column is implicit, so don't include it in streamConfig
		for (int i=0; i < csv.schema.entries.length; i++) {
			if (i != csv.getSchema().timestampColumnIndex) {
				CSVImporter.SchemaEntry e = csv.getSchema().entries[i]
				if (e!=null)
					fields << [name:e.name, type:e.type]
			}
		}

		config.fields = fields
		return config
	}

}
