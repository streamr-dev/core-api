package com.unifina.signalpath.remote;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple holder for mock java.sql.Statement
 * This class must be here (and not e.g. in test/unit/com/unifina/signalpath/remote)
 * 	so that the deserializer / class-loader finds it
 * @see SqlSpec where this class is used
 */
public class TestableSql extends Sql {
	public transient static Statement statement;

	@Override
	protected Statement createStatement() throws SQLException {
		return statement;
	}
}
