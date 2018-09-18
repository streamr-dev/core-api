package com.unifina.service

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

@GrailsCompileStatic
class CsvUploadService {
	static scope = "singleton"

	ApiService apiService
	StreamService streamService

	IdGenerator idGenerator = new IdGenerator()

	Map<String, String> idToFilePath = new HashMap<>()

	Map uploadCsvFile(File file, String streamId, SecUser user) throws NotFoundException, NotPermittedException {
		Stream stream = apiService.authorizedGetById(Stream, streamId, user, Permission.Operation.WRITE)

		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])
		List fields = (List) (config.fields ? config.fields : [])

		// Attempt to auto-detect timestamp field
		CSVImporter csv = new CSVImporter(file, fields, null, null, user.timezone)
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
		Stream stream = apiService.authorizedGetById(Stream, streamId, user, Permission.Operation.WRITE)

		Map config = (Map) (stream.config ? JSON.parse(stream.config) : [:])
		List fields = (List) (config.fields ? config.fields : [])

		String filePath = idToFilePath.get(instructions.fileId)
		if (!filePath) {
			throw new NotFoundException(String.format("File not found with identifier '%s'", instructions.fileId))
		}
		File file = new File(filePath)

		try {
			CSVImporter csv = new CSVImporter(file, fields, instructions.timestampColumnIndex, instructions.dateFormat)
			Map newStreamConfig = streamService.importCsv(csv, stream)
			stream.config = (newStreamConfig as JSON)
			return stream.save()
		} finally {
			file.delete()
			idToFilePath.remove(instructions.fileId)
		}
	}

}
