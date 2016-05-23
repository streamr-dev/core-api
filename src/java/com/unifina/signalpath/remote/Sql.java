package com.unifina.signalpath.remote;

import com.unifina.signalpath.*;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * Module that lets user make SQL queries to given database server
 */
public class Sql extends AbstractSignalPathModule {
	private transient Logger log = Logger.getLogger(Sql.class);

	private EngineParameter engine = new EngineParameter(this, "DB engine");
	private StringParameter loginUrl = new StringParameter(this, "Database", "");
	private StringParameter loginUser = new StringParameter(this, "Username", "");
	private StringParameter loginPassword = new StringParameter(this, "Password", "");

	private StringInput sqlString = new StringInput(this, "SQL command");

	private ListOutput errors = new ListOutput(this, "errors");
	private ListOutput rows = new ListOutput(this, "result");

	@Override
	public void init() {
		addInput(engine);
		addInput(loginUrl);
		addInput(loginUser);
		addInput(loginPassword);
		addInput(sqlString);
		addOutput(errors);
		addOutput(rows);

		try {
			// see https://docs.oracle.com/javase/7/docs/api/java/sql/DriverManager.html#registerDriver(java.sql.Driver)
			Class.forName(engine.getValue());
		} catch (ClassNotFoundException e) {
			log.error("Could not initialize SQL module! Database engine " + engine.getValue() + " not found.");
		}
	}

	@Override
	public void sendOutput() {
		List<String> err = new ArrayList<>();
		Connection connection = null;
		Statement statement = null;
		ResultSet cursor = null;
		try {
			connection = DriverManager.getConnection(loginUrl.getValue(), loginUser.getValue(), loginPassword.getValue());
			statement = connection.createStatement();
			cursor = statement.executeQuery(sqlString.getValue());

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
		} catch (SQLException e) {
			err.add(e.toString());
		} finally {
			if (cursor != null) { try { cursor.close(); } catch (SQLException e) { err.add(e.toString()); } }
			if (statement != null) { try { statement.close(); } catch (SQLException e) { err.add(e.toString()); } }
			if (connection != null) { try { connection.close(); } catch (SQLException e) { err.add(e.toString()); } }
		}
		errors.send(err);
	}

	@Override
	public void clearState() {
	}

	public static class EngineParameter extends StringParameter {
		public EngineParameter(AbstractSignalPathModule owner, String name) {
			super(owner, name, "MySQL"); //this.getValueList()[0]);
		}
		private List<PossibleValue> getValueList() {
			return Arrays.asList(
				new PossibleValue("MySQL", "com.mysql.jdbc.Driver")
			);
		}
		@Override public Map<String, Object> getConfiguration() {
			Map<String, Object> config = super.getConfiguration();
			config.put("possibleValues", getValueList());
			return config;
		}
	}
}
