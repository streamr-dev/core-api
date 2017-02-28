package com.unifina.signalpath.custom

import spock.lang.Specification
import spock.lang.Unroll

class JavaScriptEngineSpec extends Specification {
	String exampleCode = """
			var a = 512;
			b = "hello world";
			c = true;
			d = {};
			this.e = 2;
			
			var doIt = function() {
			  return a * e;
			}
			
			var addThem = function(param1, param2) {
				return param1 + param2;
			}
			
			function getValueOfE() { return e; }
			
			this.start = function() { ++e; }
			
			function make_me_a_map(a, b, c) {
				return { a: a, b: b, c: c, d: d };
			}
			
			function listOfDigits() {
				return [3, 1, 4, 1, 5, 9, 2];
			}
			
			function joinIt(arr) {
				return arr.map(function(el) { return "'" + el + "'"; }).join(",");
			}
			
			function listifyObject(obj) {
				Object.keys(obj).forEach(function(key) {
					obj[key] = [obj[key]];
				});
				return obj;
			}
		"""

	void "given empty code detects no variables"() {
		expect:
		new JavaScriptEngine("").variables.empty
	}

	void "given empty code detects println and print"() {
		expect:
		new JavaScriptEngine("").functionNames == ["println", "print"]
	}

	void "throws JavaScriptException given code with syntax error"() {
		when:
		new JavaScriptEngine("var a=5;\nvar b = {};\n?*´+\$")

		then:
		def e = thrown(JavaScriptException)
		e.lineNumber == 3
	}

	@Unroll
	void "does not detect variable from declaration #code"(String code) {
		expect:
		new JavaScriptEngine(code).variables.empty
		where:
		code << ["var a;", "var a = undefined;"]
	}

	@Unroll
	void "detects #clazz with value #value from declaration '#code'"(String code, Class clazz, Object value) {
		when:
		def variables = new JavaScriptEngine(code).variables

		then:
		variables.size() == 1
		variables.first().name == "a"
		variables.first().clazz == clazz
		variables.first().defaultValue == value

		where:
		code                          | clazz   | value
		"var a = false;"              | Boolean | Boolean.FALSE
		"var a = true;"               | Boolean | Boolean.TRUE
		"var a = true && true;"       | Boolean | Boolean.TRUE
		"var a = true && false;"      | Boolean | Boolean.FALSE
		"var a = 0;"                  | Double  | 0d
		"var a = -3.141592;"          | Double  | -3.141592d
		"var a = Math.PI;"            | Double  | Math.PI
		"var a = 2/0;"                | Double  | Double.POSITIVE_INFINITY
		"var a = -2/0;"               | Double  | Double.NEGATIVE_INFINITY
		"var a = 0/0;"                | Double  | Double.NaN
		"var a = 'a';"                | String  | "a"
		"var a = \"hello\";"          | String  | "hello"
		"var a = \"hello\";"          | String  | "hello"
		"var a = 2 + \"abba\" + 3"    | String  | "2abba3"
		"var a = []"                  | List    | []
		"var a = [1, 'two', false]"   | List    | [1d, "two", Boolean.FALSE]
		"var a = [[[]]];"             | List    | [[[]]]
		"var a = {};"                 | Map     | [:]
		"var a = {a: 312, b: 'bb'};"  | Map     | [a: 312d, b: "bb"]
		"var a = {a: {a: 2}, b: []};" | Map     | [a: [a: 2d], b: []]
		"var a = null;"               | Object  | null
	}

	@Unroll
	void "detects function #name from declaration '#code'"(String name, String code) {
		when:
		def functionNames = new JavaScriptEngine(code).functionNames

		then:
		functionNames.size() == 3
		functionNames.contains(name)

		where:
		name       | code
		"start"    | "function start() {}"
		"report"   | "report = function() { console.log('hello world'); }"
		"getValue" | "var getValue = function() { return 0; }"
		"put"      | "this.put = function() { return 2; }"
		"stop"     | "var a = { stop: function() {} }; var stop = a.stop;"
	}

	void "detects multiple variable and functions"() {
		when:
		def jsEngine = new JavaScriptEngine(exampleCode)

		then:
		jsEngine.getFunctionNames() as Set == ["print", "println", "doIt", "addThem", "getValueOfE", "start",
											   "make_me_a_map", "listOfDigits", "joinIt", "listifyObject"] as Set
		jsEngine.getVariables()*.name as Set == ["a", "b", "c", "d", "e"] as Set
	}

	@Unroll
	void "invocation of #function with arguments #args returns #result"(String function, Object[] args, Object result) {
		expect:
		new JavaScriptEngine(exampleCode).invoke(function, args) == result

		where:
		function        | args                                   | result
		"doIt"          | []                                     | 1024d
		"addThem"       | [1, 2]                                 | 3
		"addThem"       | ["hello ", "world!"]                   | "hello world!"
		"getValueOfE"   | []                                     | 2
		"start"         | ["unused"]                             | null
		"make_me_a_map" | ["a", "b", 3]                          | [a: "a", b: "b", c: 3, d: [:]]
		"listOfDigits"  | []                   				     | [3, 1, 4, 1, 5, 9, 2]
		"joinIt"        | [[1, 2, "a", "b"]]                     | "'1','2','a','b'"
		"listifyObject" | [[a: "aaa", b: [bb: "2"], c: [1,2,3]]] | [a:["aaa"], b:[[bb: "2"]], c:[[1d, 2d, 3d]]]
	}

	void "variables at global scope can be re-assigned"() {
		def jsEngine = new JavaScriptEngine(exampleCode)

		when:
		jsEngine.define("a", 10)
		jsEngine.define("e", 4)

		then:
		jsEngine.invoke("doIt") == 40d
	}

	void "can keep state over consecutive invocations"() {
		def jsEngine = new JavaScriptEngine(exampleCode)
		assert jsEngine.invoke("getValueOfE") == 2

		when:
		10.times { jsEngine.invoke("start") }

		then:
		jsEngine.invoke("getValueOfE") == 12d
	}

	void "throws JavaScriptException when attempting to invoke non-existing method"() {
		def jsEngine = new JavaScriptEngine("")

		when:
		jsEngine.invoke("main")

		then:
		def e = thrown(JavaScriptException)
		e.lineNumber == -1
		e.message.contains("main")
		e.message.contains("no such method")
	}

	void "throws JavaScriptException when attempting to invoke identifier that is not a function"() {
		def jsEngine = new JavaScriptEngine("var main = 666;")

		when:
		jsEngine.invoke("main")

		then:
		def e = thrown(JavaScriptException)
		e.lineNumber == -1
		e.message.contains("main")
		e.message.contains("no such method")
	}

	void "throws JavaScriptException on runtime errors"() {
		def jsEngine = new JavaScriptEngine("""
			var msg = "debugMessage";
			function main() {
				console.log(msg);
			}
		""")

		when:
		jsEngine.invoke("main")

		then:
		def e = thrown(JavaScriptException)
		e.lineNumber == 4
		e.message.contains("console")
		e.message.contains("not defined")
	}
}
