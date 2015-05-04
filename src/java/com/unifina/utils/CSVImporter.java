package com.unifina.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.log4j.Logger;

import com.opencsv.CSVParser;
import com.unifina.utils.CSVImporter.LineValues;

public class CSVImporter implements Iterable<LineValues> {

	private Schema schema;
	private File file;
	
	private static final Logger log = Logger.getLogger(CSVImporter.class);
	
	public CSVImporter(File file) throws IOException {
		this.file = file;
		
		// Detect the schema
		InputStream is = new FileInputStream(file);
		
		try {
			schema = new Schema(is);
		} finally {
			is.close();
		}
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
			} catch (IOException | ParseException e) {
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
		
		// TODO: expand, support ISO standards
		public final SimpleDateFormat[] dateFormatsToTry = {
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
		};
		
		public CSVParser parser = null;
		public SchemaEntry[] entries;
		public Integer timestampColumnIndex = null;
		
		public Schema(InputStream is) throws IOException {
			for (SimpleDateFormat df : dateFormatsToTry)
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
			
			detect(is);
		}
		
		private void detect(InputStream is) throws IOException {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			try {
				int lineCount = 0;
				int undetectedSchemaEntries = Integer.MAX_VALUE;
			    String line;
			    String[] headers = null;
			    
			    while (undetectedSchemaEntries>0 && (line = reader.readLine()) != null) {
			    	
			    	// Try to detect separator format and parse headers from first line
			    	if (lineCount==0) {
			    		headers = detectHeaders(line);
						undetectedSchemaEntries = headers.length;
						entries = new SchemaEntry[headers.length];
			    	}
			    	// On subsequent lines, try to detect the format of each individual column
			    	else {
			    		String[] fields = parser.parseLine(line);
			    		
			    		if (fields.length < entries.length) {
							throw new RuntimeException("Unexpected number of columns on row "+lineCount);
						}
			    		
			    		for (int i=0;i<fields.length;i++) {
			    			// Is the type of this field still undetected?
			    			if (entries[i]==null) {
			    				entries[i] = detectEntry(fields[i], headers[i]);
			    				if (entries[i] != null) {
			    					undetectedSchemaEntries--;
			    					if (entries[i].dateFormat!=null && timestampColumnIndex==null)
			    						timestampColumnIndex = i;
			    				}
			    			}
			    		}
			    		if(timestampColumnIndex == null){
			    			throw new IncorrectTimestampException("The index of the timestamp column could not be detected");
			    		}
			    	}
			    	
			    	lineCount++;
			    }
				
			    if (undetectedSchemaEntries>0) {
			    	StringBuilder sb = new StringBuilder("Format could not be detected for the following columns: ");
			    	for (int i=0;i<headers.length;i++) {
			    		if (entries[i]==null)
			    			sb.append(headers[i]);
			    			sb.append(",");
			    	}
			    	throw new RuntimeException(sb.toString());
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
				throw new RuntimeException("Sorry, couldn't determine format of the csv file!");
			}
			else return headers;
		}
		
		private SchemaEntry detectEntry(String value, String name) {
			if (value==null || value.length()==0)
				return null;
			
			// Try to parse as one of the date formats
			for (SimpleDateFormat df : dateFormatsToTry) {
				try {
					df.parse(value);
					return new SchemaEntry(name, df);
				} catch (Exception e) {}
			}
			
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
		
		LineValues parseLine(String line) throws IOException, ParseException {
			String[] values = parser.parseLine(line);
			Object[] parsed = new Object[values.length];
			
    		if (values.length < entries.length) {
				throw new RuntimeException("Unexpected number of columns on line: "+line);
			}
			
			for (int i=0; i<values.length; i++) {
				// The timestamp column cannot be empty
				if (i == timestampColumnIndex)
					parsed[i] = entries[i].dateFormat.parse(values[i]);
				// Check for empty fields
				else if (values[i]==null || values[i].length()==0)
					parsed[i] = null;
				// Parse date field
				else if (entries[i].dateFormat != null) {
					parsed[i] = entries[i].dateFormat.parse(values[i]);
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
			
			return new LineValues(schema, parsed);
		}
	}
	
	
	public class SchemaEntry {

		public String name;
		public String type;
		public SimpleDateFormat dateFormat;
		
		public SchemaEntry(String name, String type) {
			this.name = name;
			this.type = type;
		}
		
		public SchemaEntry(String name, SimpleDateFormat dateFormat) {
			this.name = name;
			this.type = "timestamp";
			this.dateFormat = dateFormat;
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
	

	public class IncorrectTimestampException extends IOException {
		public IncorrectTimestampException() { 
			super();
		}
		public IncorrectTimestampException(String message) {
			super(message); 
		}
		public IncorrectTimestampException(String message, Throwable cause) {
			super(message, cause); 
		}
		public IncorrectTimestampException(Throwable cause) {
			super(cause);
		}
	}

}
