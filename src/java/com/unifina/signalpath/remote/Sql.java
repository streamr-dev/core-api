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

	private StringParameter loginUrl = new StringParameter(this, "database", "");
	private StringParameter loginUser = new StringParameter(this, "username", "");
	private StringParameter loginPassword = new StringParameter(this, "password", "");

	private StringInput sqlString = new StringInput(this, "sql");

	private ListOutput errors = new ListOutput(this, "errors");
	private ListOutput rows = new ListOutput(this, "result");

	private transient Connection db;

	@Override
	public void init() {
		addInput(loginUrl);
		addInput(loginUser);
		addInput(loginPassword);
		addInput(sqlString);
		addOutput(errors);
		addOutput(rows);
	}

	@Override
	public void sendOutput() {
		List<String> err = new ArrayList<>();
		Statement statement = null;
		ResultSet cursor = null;
		try {
			statement = createStatement();
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
		}

		if (err.size() > 0) {
			errors.send(err);
		}
	}

	// separated method so it can be mocked in a test
	protected Statement createStatement() throws SQLException {
		if (db == null) {
			db = DriverManager.getConnection(loginUrl.getValue(), loginUser.getValue(), loginPassword.getValue());
		}
		return db.createStatement();
	}

	@Override
	public void clearState() { }

	@Override
	public void destroy() {
		if (db != null) { try { db.close(); } catch (SQLException e) { } }
	}
}
