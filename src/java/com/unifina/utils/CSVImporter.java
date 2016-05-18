package com.unifina.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.opencsv.CSVParser;
import com.unifina.utils.CSVImporter.LineValues;
import org.joda.time.format.*;

public class CSVImporter implements Iterable<LineValues> {

	private Schema schema;
	private File file;

	private static final Logger log = Logger.getLogger(CSVImporter.class);

	/**
	 * @param file The File to read
	 * @param fields Existing field config, if available
	 * @param timestampIndex Index of the timestamp column, if available
	 * @param format Timestamp format (in JodaTime syntax), if available
	 * @param ignoreEmptyFields If true, ignores empty fields. If false, returns empty strings, and throws exception for empty numbers and booleans. True by default.
     * @throws IOException
     */
	public CSVImporter(File file, List<Map> fields, Integer timestampIndex, String format, boolean ignoreEmptyFields) throws IOException {
		this.file = file;

		// Detect the schema
		InputStream is = new FileInputStream(file);

		HashMap<String, String> fieldMap = new HashMap<>();
		if(fields != null)
			for(Map field : fields){
				String name = (String) field.get("name");
				String value = (String) field.get("type");
				fieldMap.put(name, value);
			}
		
		try {
			schema = new Schema(is, fieldMap, timestampIndex, format, ignoreEmptyFields);
		} finally {
			is.close();
		}
	}

	public CSVImporter(File file, List<Map> fields, Integer timestampIndex, String format) throws IOException {
		this(file, fields, timestampIndex, format, true);
	}

	public CSVImporter(File file) throws IOException {
		this(file, null, null, null, true);
	}

	public CSVImporter(File file, List fields) throws IOException {
		this(file, fields, null, null, true);
	}

	@Override
	public Iterator<LineValues> iterator() {
		try {
			return new LineValuesIterator(file, schema);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public class LineValuesIterator implements Iterator<LineValues> {
		private LineIterator it;
		private Schema schema;
		
		boolean headerRead = false;
		
		public LineValuesIterator(File file, Schema schema) throws IOException {
			 this.it = FileUtils.lineIterator(file, "UTF-8");
			 this.schema = schema;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public LineValues next() {
			String line = it.next();
			
			if (!headerRead) {
				headerRead = true;
				line = it.next();
			}

			try {
				return schema.parseLine(line);
			} catch (IOException | ParseException | RuntimeException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void remove() {}
		
		public void close() {
			it.close();
		}
	}
	
	public class Schema {
		
		public final CSVParser[] parsersToTry = {
				new CSVParser(','),
				new CSVParser('\t'),
				new CSVParser(';')
		};

		public CSVParser parser = null;
		public SchemaEntry[] entries;

		private boolean ignoreEmptyFields;

		public Integer timestampColumnIndex = null;
		private String format;
		public String[] headers;

		// Used to test if the lines are in chronological order
		private Date lastDate = null;

		private Map<String, String> fields = null;
		
		public Schema(InputStream is, Map<String, String> fields, Integer timestampIndex, String format, boolean ignoreEmptyFields) throws IOException {
			this.ignoreEmptyFields = ignoreEmptyFields;
			if(timestampIndex != null)
				setTimeStampColumnIndex(timestampIndex);
			if(format != null)
				setDateFormat(format);
			if(fields != null)
				this.fields = fields;

			detect(is);
		}
		
		private void detect(InputStream is) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			try {
				int lineCount = 0;
				int undetectedSchemaEntries = Integer.MAX_VALUE;
			    String line;
			    
			    while (undetectedSchemaEntries>0 && (line = reader.readLine()) != null) {
			    	
			    	// Try to detect separator format and parse headers from first line
			    	if (lineCount==0) {
			    		headers = detectHeaders(line);
						undetectedSchemaEntries = headers.length;
						entries = new SchemaEntry[headers.length];
			    	}
			    	// On subsequent lines, try to detect the format of each individual column
			    	else {
			    		String[] columns = parser.parseLine(line);

						// Ignore extra columns or missing columns (columns.length != entries.length)
			    		for (int i=0;i<columns.length && i<entries.length;i++) {
			    			// Is the type of this field still undetected?
			    			if (entries[i]==null) {
			    				if(timestampColumnIndex != null && i == timestampColumnIndex){
			    					entries[i] = new SchemaEntry(headers[i], new CustomDateTimeParser(format));
			    				} else {
			    					entries[i] = detectEntry(columns[i], headers[i]);
			    				}
			    				if (entries[i] != null) {
			    					undetectedSchemaEntries--;
			    					if (entries[i].dateTimeParser !=null && timestampColumnIndex==null)
			    						timestampColumnIndex = i;
			    				}
			    				
			    			}
			    		}
			    	}
			    	
			    	lineCount++;
			    }
				
			    if (undetectedSchemaEntries>0) {
			    	StringBuilder sb = new StringBuilder("Format could not be detected for the following columns: ");
			    	for (int i=0;i<headers.length;i++) {
			    		if (entries[i]==null) {
							sb.append(headers[i]);
							sb.append(",");
						}
			    	}
					log.warn(sb.toString());
			    	// throw new RuntimeException(sb.toString());
			    }
			}
		    finally {
		    	reader.close();
		    }
		}
		
		private String[] detectHeaders(String line) throws IOException {
    		int parserIdx = 0;
    		String[] headers = null;
    		
    		while ((headers==null || headers.length<2) && parserIdx < parsersToTry.length) {
				parser = parsersToTry[parserIdx++];
				headers = parser.parseLine(line);
			}
			
			if (headers.length<2) {
				throw new CSVImporterException("Sorry, couldn't determine format of csv file!");
			}
			else {
				return headers;
			}
		}
		
		private SchemaEntry detectEntry(String value, String name) {
			if (value==null || value.length()==0)
				return null;

			if(fields != null && fields.containsKey(name)) {
				return new SchemaEntry(name, fields.get(name));
			} else {
				// Try to parse as ISO dateTime
				try {
					CustomDateTimeParser parser = new CustomDateTimeParser();
					parser.parse(value);
					return new SchemaEntry(name, parser);
				} catch (Exception e) {}

				// Try to parse as double
				try {
					Double.parseDouble(value);
					return new SchemaEntry(name, "number");
				} catch (Exception e) {}

				// Try to parse as boolean
				if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
					return new SchemaEntry(name, "boolean");
				}
				// Else treat it as string
				else return new SchemaEntry(name, "string");
			}

		}
		
		LineValues parseLine(String line) throws IOException, ParseException {
			String[] values = parser.parseLine(line);
			Object[] parsed = new Object[values.length];

			int i=0;
			// Ignore missing or extra columns
			try {
				for (i=0; i < values.length && i < entries.length; i++) {
					// The timestamp column cannot be empty
					if (i == timestampColumnIndex) {
						Date d = entries[i].dateTimeParser.parse(values[i]);
						if (lastDate != null && d.before(lastDate)) {
							throw new CSVImporterException("The lines must be in a chronological order!");
						}
						parsed[i] = d;
						lastDate = d;
					}
					// Check for missing or empty (if ignoreEmptyFields == true) fields
					else if (values[i] == null || (ignoreEmptyFields && values[i].length() == 0))
						parsed[i] = null;
					// Parse date field
					else if (entries[i].dateTimeParser != null) {
						parsed[i] = entries[i].dateTimeParser.parse(values[i]);
					}
					// Parse number
					else if (entries[i].type.equals("number"))
						parsed[i] = Double.parseDouble(values[i]);
					// Parse boolean
					else if (entries[i].type.equals("boolean"))
						parsed[i] = Boolean.parseBoolean(values[i]);
					// String
					else if (entries[i].type.equals("string"))
						parsed[i] = values[i];
				}
			} catch (NumberFormatException e) {
				throw new CSVImporterException("Invalid value '"+values[i]+"' in column '"+entries[i].name+"', detected column type was: "+entries[i].type);
			}
			
			return new LineValues(schema, parsed);
		}
		
		private void setTimeStampColumnIndex(int index){
			this.timestampColumnIndex = index;
		}
		
		private void setDateFormat(String format){
			this.format = format;
		}


	}
	
	public class SchemaEntry {

		public String name;
		public String type;
		public CustomDateTimeParser dateTimeParser;
		
		public SchemaEntry(String name, String type) {
			this.name = name;
			this.type = type;
		}
		
		public SchemaEntry(String name, CustomDateTimeParser dateTimeParser) {
			this.name = name;
			this.type = "timestamp";
			this.dateTimeParser = dateTimeParser;
		}
		
	}
	
	public class LineValues {
		public Schema schema;
		public Object[] values;
		
		public LineValues(Schema schema, Object[] values) {
			this.schema = schema;
			this.values = values;
		}
		
		public Date getTimestamp() {
			return (Date) values[schema.timestampColumnIndex];
		}
	}

	public class CustomDateTimeParser {

		private DateTimeFormatter fmt;
		private String format;

		public CustomDateTimeParser() {
			this("");
		}

		public CustomDateTimeParser(String format) {
			this.format = format;
			if (format.equals("")) {
				// A formatter with regular ISO8601 format and a custom one where 'T' is replaced by whitespace
				fmt = new DateTimeFormatterBuilder()
						.append(ISODateTimeFormat.dateParser())
						.appendOptional(new DateTimeFormatterBuilder().appendLiteral(' ').toParser())
						.appendOptional(new DateTimeFormatterBuilder().appendLiteral('T').toParser())
						.append(ISODateTimeFormat.timeParser())
						.toFormatter().withZoneUTC();
			} else if (!(format.equals("unix") || format.equals("unix-s"))) {
				fmt = DateTimeFormat.forPattern(format);
			}
		}

		public Date parse(String date) {
			try {
				if (format.equals("unix") || format.equals("unix-s")) {
					long l;
					l = Long.parseLong(date);
					if (format.equals("unix-s")) {
						l *= 1000;
					}
					return new Date(l);
				} else {
					return fmt.parseDateTime(date).toDate();
				}
			} catch (Exception e) {
				throw new RuntimeException("Invalid date!");
			}
		}

		public Date parse(long date) {
			return parse("" + date);
		}
	}

	public class CSVImporterException extends RuntimeException {
		public CSVImporterException(String message) {
			super(message);
		}
	}
}