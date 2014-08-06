package com.unifina.utils;

import groovy.util.ConfigObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

public class CSVWriter {

	public static int TIMEFORMAT_MILLIS = 1;
	public static int TIMEFORMAT_ISO = 2;
	
	private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	File file;
	String separator;
	int timeFormat;
	boolean lastOfDayOnly;
	
	BufferedWriter writer;
	StringBuilder sb;
	String lastLineToday = null;
	
	public CSVWriter() {
		this(null,null,null,null,null);
	}
	
	public CSVWriter(File file, ConfigObject config, Map<String,Object> signalPathContext, TimeZone timeZone) {
		this(file, 
			(String) MapTraversal.getProperty(config,"unifina.csv.separator"),
			MapTraversal.getInteger(signalPathContext,"csvOptions.timeFormat"),
			MapTraversal.getBoolean(signalPathContext,"csvOptions.lastOfDayOnly"),
			timeZone
		);
	}
	
	public CSVWriter(File file, String separator, Integer timeFormat, Boolean lastOfDayOnly, TimeZone timeZone) {
		try {
			this.file = file!=null ? file : File.createTempFile("unifina_csv_",".csv");
			this.separator = separator!=null ? separator : ";";
			this.timeFormat = timeFormat!=null ? timeFormat : TIMEFORMAT_ISO;
			this.lastOfDayOnly = lastOfDayOnly!=null ? lastOfDayOnly : false;
			if (timeZone!=null)
				isoFormat.setTimeZone(timeZone);
			
			writer = new BufferedWriter(new FileWriter(this.file),8192*100);
		} catch (IOException e) {
			throw new RuntimeException("Failed to create CSV file",e);
		}
		sb = new StringBuilder();
	}
	
	public void writeField(String s) {
		if (sb.length()>0)
			sb.append(separator);
		
		sb.append(s);
	}
	
	public void writeField(Date date) {
		if (timeFormat==TIMEFORMAT_ISO)
			writeField(isoFormat.format(date));
		else if (timeFormat==TIMEFORMAT_MILLIS)
			writeField(String.valueOf(date.getTime()));
	}
	
	public void newLine() {
		sb.append("\r\n");
		lastLineToday = sb.toString();
		if (!lastOfDayOnly) { 
			try {
				writer.write(sb.toString());
			} catch (IOException e) {
				throw new RuntimeException("Exception writing to CSV",e);
			}
		}
		sb.setLength(0);
	}
	
	public void newDay() {
		if (lastOfDayOnly && lastLineToday!=null) {
			try {
				writer.write(lastLineToday);
			} catch (IOException e) {
				throw new RuntimeException("Exception writing to CSV",e);
			}
		}
		lastLineToday = null;
	}
	
	public void setTimeZone(TimeZone tz) {
		isoFormat.setTimeZone(tz);
	}
	
	public File finish() {
		newDay();
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Exception closing CSV",e);
		}
		return file;
	}

	public String getSeparator() {
		return separator;
	}
	
}
