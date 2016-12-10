package com.unifina.signalpath.utils;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;
import com.unifina.utils.CSVWriter2;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class WriteToCsvFile extends ModuleWithUI {

	private final VariadicInput<Object> ins = new VariadicInput<>(this, new InputInstantiator.SimpleObject());

	private boolean includeTimestamps = true;
	private boolean writeHeader = true;
	private String separator = "\t";
	private boolean quoteColumns = false;
	private TimeFormat timeFormat = TimeFormat.MILLISECONDS_SINCE_EPOCH;

	private FileHolder fileHolder;
	private transient CSVWriter2 csvWriter;

	public WriteToCsvFile() {
		this(new FileHolder());
	}

	WriteToCsvFile(FileHolder fileHolder) {
		this.fileHolder = fileHolder;
	}

	@Override
	public void initialize() {
		super.initialize();
		openCsvWriter();
	}

	@Override
	public void sendOutput() {
		List<Object> row = new ArrayList<>();
		if (includeTimestamps) {
			row.add(timeFormat.formatTime(getGlobals().time));
		}
		row.addAll(ins.getValues());
		try {
			csvWriter.writeRow(row);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<String, Object> msg = new LinkedHashMap<>();
		msg.put("type", "csvRowCount");
		msg.put("value", csvWriter.getNumOfRowsWritten());
		pushToUiChannel(msg);
	}

	@Override
	public void clearState() {}

	@Override
	public void destroy() {
		super.destroy();

		try {
			csvWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map<String, Object> msg = new LinkedHashMap<>();
		msg.put("type", "csvFileReady");
		msg.put("file", fileHolder.getFileName());
		msg.put("kilobytes", fileHolder.getFileSizeInKilobytes());
		pushToUiChannel(msg);
	}

	/**
	 * Boilerplate for variadic inputs
	 */
	@Override
	public Input getInput(String name) {
		Input input = super.getInput(name);
		if (input == null) {
			input = ins.addEndpoint(name);
		}
		return input;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ins.onConfiguration(config);

		ModuleOptions options = ModuleOptions.get(config);

		ModuleOption writeHeaderOption = options.getOption("writeHeader");
		if (writeHeaderOption != null) {
			 writeHeader = writeHeaderOption.getBoolean();
		}
		ModuleOption includeTimestampsOption = options.getOption("includeTimestamps");
		if (includeTimestampsOption != null) {
			includeTimestamps = includeTimestampsOption.getBoolean();
		}
		ModuleOption separatorOption = options.getOption("separator");
		if (separatorOption != null) {
			separator = separatorOption.getString();
		}
		ModuleOption quoteColumnsOption = options.getOption("quoteColumns");
		if (quoteColumnsOption != null) {
			quoteColumns = quoteColumnsOption.getBoolean();
		}
		timeFormat = TimeFormat.fromModuleOptions(options);

	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createBoolean("writeHeader", writeHeader));
		options.addIfMissing(ModuleOption.createBoolean("includeTimestamps", includeTimestamps));
		options.addIfMissing(ModuleOption.createString("separator", separator));
		options.addIfMissing(ModuleOption.createBoolean("quoteColumns", quoteColumns));
		options.addIfMissing(timeFormat.asModuleOption());
		return config;
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		openCsvWriter();
	}

	private void openCsvWriter() {
		try {
			fileHolder.openNewFile();
			csvWriter = new CSVWriter2(fileHolder.getWriter(), separator, quoteColumns);

			if (writeHeader) {
				List<String> inputNames = new ArrayList<>();
				if (includeTimestamps) {
					inputNames.add("timestamp");
				}
				for (Input in : ins.getEndpoints()) {
					inputNames.add(in.getEffectiveName());
				}
				csvWriter.writeRow(inputNames);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class FileHolder implements Serializable {
		private static final Logger log = Logger.getLogger(FileHolder.class);
		private transient File file;
		private transient Writer writer;

		public void openNewFile() throws IOException {
			file = File.createTempFile("streamr_csv_", ".csv");
			writer = new FileWriter(file);
			log.info("Created temporary file " + file.getAbsolutePath() + " for writing.");
		}

		public Writer getWriter() {
			return writer;
		}

		public String getFileName() {
			return file.getName();
		}

		public long getFileSizeInKilobytes() {
			return file.length() / 1024;
		}
	}

	private enum TimeFormat {
		MILLISECONDS_SINCE_EPOCH,
		ISO_8601_LOCAL,
		ISO_8601_UTC;

		private static final DateFormat DF_LOCAL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		private static final DateFormat DF_UTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		static {
			DF_UTC.setTimeZone(TimeZone.getTimeZone("UTC"));
		}

		private ModuleOption asModuleOption() {
			return ModuleOption.createString("timeFormat", name()).
				addPossibleValue("ms since Unix Epoch", TimeFormat.MILLISECONDS_SINCE_EPOCH.name()).
				addPossibleValue("ISO 8601 local", TimeFormat.ISO_8601_LOCAL.name()).
				addPossibleValue("ISO 8601 UTC", TimeFormat.ISO_8601_UTC.name());
		}

		private String formatTime(Date date) {
			if (this == ISO_8601_LOCAL) {
				return DF_LOCAL.format(date);
			} else if (this == ISO_8601_UTC) {
				return DF_UTC.format(date);
			}
			return String.valueOf(date.getTime());
		}

		private static TimeFormat fromModuleOptions(ModuleOptions options) {
			ModuleOption option = options.getOption("timeFormat");
			return option == null ? MILLISECONDS_SINCE_EPOCH : valueOf(option.getString());
		}
	}
}