package com.unifina.signalpath.remote

import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement

class SqlSpec extends Specification {
	Sql module
	List result

	void setup() {
		TestableSql.statement = Stub(Statement) {
			executeQuery(_) >> { getMockCursor(result) }
		}
		module = new TestableSql();
		module.init()
		module.configure([
			params: [
				[name: "engine", value: "PostgreSQL"],
				[name: "host", value: "test.com"],
				[name: "database", value: "mydatabase"],
				[name: "username", value: "user"],
				[name: "password", value: "pass"],
			]
		])
	}

	private def getMockCursor(List<Map> results) {
		int i = 0
		return Stub(ResultSet) {
			getMetaData() >> Stub(ResultSetMetaData) {
				getColumnCount() >> { results[0].size() }
				getColumnName(_) >> { int j -> (results[0].keySet() as List)[j-1] }
			}
			getObject(_) >> { int j ->
				(results[i-1].values() as List)[j-1]
			}
			next() >> {
				return i++ < results.size()
			}
		}
	}

	void "SQL module sends SQL query results out correctly"() {
		String query = "select * from table"
		result = [[a:1, b:2], [a:4, b:6]]

		when:
		Map inputValues = [
			sql: [query, query],
		]
		Map outputValues = [
			errors: [null, null],
			result: [result, result]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues).test()
	}

	void "SQL module builds the connection URL correctly"() {
		expect:
		module.getConnectionURL() == "jdbc:postgresql://test.com/mydatabase"
	}
}
