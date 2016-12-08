package com.unifina.signalpath.remote

import com.unifina.signalpath.ModuleOption
import com.unifina.signalpath.ModuleOptions
import com.unifina.signalpath.ModuleWithSideEffects
import com.unifina.utils.Globals
import com.unifina.utils.testutils.ModuleTestHelper
import spock.lang.Specification

import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Statement

class SqlSpec extends Specification {
	Sql module
	List result

	void setup() {
		TestableSql.statement = Stub(Statement) {
			execute(_) >> { true }
			getResultSet() >> { getMockCursor(result) }
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
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.overrideGlobals { Globals globals ->
			globals.setRealtime(true)
			return globals
		}.test()
	}

	void "SQL module builds the connection URL correctly"() {
		expect:
		module.getConnectionURL() == "jdbc:postgresql://test.com/mydatabase"
	}

	void "SQL module does not execute queries in historical mode by default"() {
		String query = "select * from table"

		when:
		Map inputValues = [
				sql: [query],
		]
		Map outputValues = [
				errors: [null],
				result: [null]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.overrideGlobals { Globals globals ->
			globals.setRealtime(false)
			return globals
		}.test()

		0 * TestableSql.statement.execute(_)
	}

	void "SQL module executes queries in historical mode if configured to do so"() {
		String query = "select * from table"

		Map config = [:]
		ModuleOptions options = ModuleOptions.get(config)
		options.add(new ModuleOption(ModuleWithSideEffects.OPTION_ACTIVATE_IN_HISTORICAL_MODE, true, ModuleOption.OPTION_BOOLEAN))
		module.configure(config)
		result = [[a:1, b:2], [a:4, b:6]]

		when:
		Map inputValues = [
				sql: [query],
		]
		Map outputValues = [
				errors: [null],
				result: [result]
		]

		then:
		new ModuleTestHelper.Builder(module, inputValues, outputValues)
				.overrideGlobals { Globals globals ->
			globals.setRealtime(false)
			return globals
		}.test()
	}
}
