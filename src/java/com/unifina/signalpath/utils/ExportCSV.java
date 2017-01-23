package com.unifina.signalpath.utils;

import com.unifina.service.SerializationService;
import com.unifina.signalpath.Input;
import com.unifina.signalpath.ModuleOption;
import com.unifina.signalpath.ModuleOptions;
import com.unifina.signalpath.ModuleWithUI;
import com.unifina.signalpath.variadic.InputInstantiator;
import com.unifina.signalpath.variadic.VariadicInput;
import com.unifina.utils.RFC4180CSVWriter;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExportCSV extends ModuleWithUI {

	public static final long MIN_MS_BETWEEN_UI_UPDATES = 1000;

	private static final Logger log = Logger.getLogger(ExportCSV.class);

	private final VariadicInput<Object> ins = new VariadicInput<>(this, new InputInstantiator.SimpleObject());

	private boolean includeTimestamps = true;
	private boolean writeHeader = true;
	private TimeFormat timeFormat = TimeFormat.ISO_8601_UTC;

	private Context context;
	private transient RFC4180CSVWriter csvWriter;
	private transient TimeFormatter timeFormatter;
	private long lastMessageSentAt = -MIN_MS_BETWEEN_UI_UPDATES;

	public ExportCSV() {
		this(new Context());
	}

	ExportCSV(Context context) {
		this.context = context;
	}

	@Override
	public void sendOutput() {
		if (csvWriter == null) {
			openCsvWriter();
		}
		if (timeFormatter == null) {
			timeFormatter = timeFormat.getTimeFormatter(getGlobals().getUserTimeZone());
		}

		List<Object> row = new ArrayList<>();
		if (includeTimestamps) {
			row.add(timeFormatter.getDate(getGlobals().time));
		}
		row.addAll(ins.getValues());
		csvWriter.writeRow(row);

		if (context.currentTimeInMillis() - lastMessageSentAt >= MIN_MS_BETWEEN_UI_UPDATES) {
			Map<String, Object> msg = new LinkedHashMap<>();
			msg.put("type", "csvUpdate");
			msg.put("rows", csvWriter.getNumOfRowsWritten());
			msg.put("kilobytes", context.getFileSizeInKilobytes());
			pushToUiChannel(msg);
			lastMessageSentAt = context.currentTimeInMillis();
		}
	}

	@Override
	public void clearState() {
		csvWriter = null;
		lastMessageSentAt = -MIN_MS_BETWEEN_UI_UPDATES;
	}

	@Override
	public void destroy() {
		super.destroy();

		try {
			if (csvWriter != null && context != null) {
				csvWriter.close();

				Map<String, Object> msg = new LinkedHashMap<>();
				msg.put("type", "csvUpdate");
				msg.put("file", context.getFileName());
				msg.put("rows", csvWriter.getNumOfRowsWritten());
				msg.put("kilobytes", context.getFileSizeInKilobytes());
				pushToUiChannel(msg);
			} else {
				Map<String, Object> msg = new LinkedHashMap<>();
				msg.put("type", "csvUpdate");
				msg.put("rows", 0L);
				msg.put("kilobytes", 0L);
				pushToUiChannel(msg);
				log.warn("destroy: csvWriter or context is null, no filename can be pushed to UI");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

		timeFormat = TimeFormat.fromModuleOptions(options);
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();
		ModuleOptions options = ModuleOptions.get(config);
		options.addIfMissing(ModuleOption.createBoolean("writeHeader", writeHeader));
		options.addIfMissing(ModuleOption.createBoolean("includeTimestamps", includeTimestamps));
		options.addIfMissing(timeFormat.asModuleOption());
		return config;
	}

	@Override
	public void afterDeserialization(SerializationService serializationService) {
		super.afterDeserialization(serializationService);
		openCsvWriter();
		timeFormatter = timeFormat.getTimeFormatter(getGlobals().getUserTimeZone());
	}

	private void openCsvWriter() {
		context.openNewFile();
		csvWriter = new RFC4180CSVWriter(context.getWriter());

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
	}

	public static class Context implements Serializable {
		private static final Logger log = Logger.getLogger(Context.class);
		private transient File file;
		private transient Writer writer;

		public void openNewFile() {
			try {
				file = File.createTempFile("streamr_csv_", ".csv");
				writer = new FileWriter(file);
				log.info("Created temporary file " + file.getAbsolutePath() + " for writing.");
			} catch (IOException e) {
				throw new RuntimeException("Failed to open temporary file for writing");
			}
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

		public long currentTimeInMillis() {
			return System.currentTimeMillis();
		}
	}

	private interface TimeFormatter {
		String getDate(Date date);
	}

	private enum TimeFormat {
		MILLISECONDS_SINCE_EPOCH,
		ISO_8601_LOCAL,
		ISO_8601_UTC;

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

		private static TimeFormat fromModuleOptions(ModuleOptions options) {
			ModuleOption option = options.getOption("timeFormat");
			return option == null ? ISO_8601_UTC : valueOf(option.getString());
		}

		private TimeFormatter getTimeFormatter(final TimeZone localTimeZone) {
			if (this == ISO_8601_LOCAL) {
				return new TimeFormatter() {
					private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

					{
						df.setTimeZone(localTimeZone);
					}

					@Override
					public String getDate(Date date) {
						return df.format(date);
					}
				};
			} else if (this == ISO_8601_UTC) {
				return new TimeFormatter() {
					@Override
					public String getDate(Date date) {
						return DF_UTC.format(date);
					}
				};
			} else {
				return new TimeFormatter() {
					@Override
					public String getDate(Date date) {
						return String.valueOf(date.getTime());
					}
				};
			}
		}
	}
}