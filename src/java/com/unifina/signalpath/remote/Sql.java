package com.unifina.signalpath.remote;

import com.unifina.signalpath.*;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * Module that lets user make SQL queries to given database server
 */
public class Sql extends AbstractSignalPathModule {

	private static final Logger log = Logger.getLogger(Sql.class);

	private EngineParameter engine = new EngineParameter(this, "engine");
	private StringParameter loginUrl = new StringParameter(this, "host", "");
	private StringParameter loginUser = new StringParameter(this, "username", "");
	private StringParameter loginPassword = new StringParameter(this, "password", "");
	private StringParameter database = new StringParameter(this, "database", "");

	private StringInput sqlString = new StringInput(this, "sql");

	private ListOutput errors = new ListOutput(this, "errors");
	private ListOutput rows = new ListOutput(this, "result");

	transient private Connection db;

	private boolean historicalWarningShown = false;
	private boolean executeInHistoricalMode = false;


	@Override
	public void init() {
		addInput(engine);
		addInput(loginUrl);
		addInput(database);
		addInput(loginUser);
		addInput(loginPassword);
		addInput(sqlString);
		addOutput(errors);
		addOutput(rows);
	}

	@Override
	public void sendOutput() {
		if (getGlobals().isRealtime() || executeInHistoricalMode) {
			List<String> err = new ArrayList<>();
			Statement statement = null;
			ResultSet cursor = null;
			try {
				statement = createStatement();
				boolean hasResult = statement.execute(sqlString.getValue());

				if (hasResult) {
					cursor = statement.getResultSet();

					ResultSetMetaData meta = cursor.getMetaData();
					int propCount = meta.getColumnCount();

					List<Map<String, Object>> ret = new ArrayList<>();
					while (cursor.next()) {
						Map<String, Object> row = new HashMap<>();
						for (int i = 1; i <= propCount; i++) {
							row.put(meta.getColumnName(i), cursor.getObject(i));
						}
						ret.add(row);
					}
					rows.send(ret);
				} else {
					Map countMap = Collections.singletonMap("updateCount", statement.getUpdateCount());
					rows.send(Collections.singletonList(countMap));
				}
			} catch (SQLException e) {
				err.add(e.toString());
			} finally {
				if (cursor != null) {
					try {
						cursor.close();
					} catch (SQLException e) {
						err.add(e.toString());
					}
				}
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						err.add(e.toString());
					}
				}
			}

			if (err.size() > 0) {
				errors.send(err);
			}
		} else if (!historicalWarningShown && getGlobals().getUiChannel()!=null && parentSignalPath != null) {
			// Show notification about historical mode unless it's already been shown
			getGlobals().getUiChannel().push(new NotificationMessage(this.getName()+": Statements are not executed in historical mode by default. This can be changed in module options."), parentSignalPath.getUiChannelId());
			historicalWarningShown = true;
		}
	}

	// separated method so it can be mocked in a test
	protected Statement createStatement() throws SQLException {
		if (db == null || !db.isValid(1)) {
			db = DriverManager.getConnection(getConnectionURL(), loginUser.getValue(), loginPassword.getValue());
		}
		return db.createStatement();
	}

	public String getConnectionURL() {
		String url = engine.getUrlProtocol() + "://" + loginUrl.getValue();
		if (!database.getValue().isEmpty()) {
			url += "/" + database.getValue();
		}
		return url;
	}

	@Override
	public void clearState() { }

	@Override
	public void destroy() {
		if (db != null) { try { db.close(); } catch (SQLException e) { } }
	}

	@Override
	public Map<String, Object> getConfiguration() {
		Map<String, Object> config = super.getConfiguration();

		ModuleOptions options = ModuleOptions.get(config);
		options.add(new ModuleOption("executeInHistoricalMode", executeInHistoricalMode, ModuleOption.OPTION_BOOLEAN));

		return config;
	}

	@Override
	protected void onConfiguration(Map<String, Object> config) {
		super.onConfiguration(config);
		ModuleOptions options = ModuleOptions.get(config);
		if (options.getOption("executeInHistoricalMode") != null) {
			executeInHistoricalMode = options.getOption("executeInHistoricalMode").getBoolean();
		}
	}

	public static class EngineParameter extends StringParameter {
		public EngineParameter(AbstractSignalPathModule owner, String name) {
			super(owner, name, "MySQL"); //this.getValueList()[0]);
		}

		private List<PossibleValue> getValueList() {
			return Arrays.asList(
				new PossibleValue("MySQL", "MySQL"),
				new PossibleValue("PostgreSQL", "PostgreSQL")
			);
		}

		@Override public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();
			config.put("possibleValues", getValueList());
			return config;
		}

		public String getUrlProtocol() {
			switch (getValue()) {
				case "MySQL": return "jdbc:mysql";
				case "PostgreSQL": return "jdbc:postgresql";
				default:
					log.error("Unexpected value in EngineParameter: "+getValue()+", falling back to MySQL");
					return "jdbc:mysql";
			}
		}
	}
}
