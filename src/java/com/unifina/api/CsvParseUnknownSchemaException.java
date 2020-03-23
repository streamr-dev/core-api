package com.unifina.api;

public class CsvParseUnknownSchemaException extends ApiException {
	private final String fileUrl;
	private final String schema;

	public CsvParseUnknownSchemaException(String fileUrl, String schema) {
		super(500, "CSV_PARSE_UNKNOWN_SCHEMA",
				"Parsing of the file failed. Please configure the schema of the file and try again!");
		this.fileUrl = fileUrl;
		this.schema = schema;
	}

	@Override
	public ApiError asApiError() {
		ApiError apiError = super.asApiError();
		apiError.addToBody("fileUrl", fileUrl);
		apiError.addToBody("schema", schema);
		return apiError;
	}
}
