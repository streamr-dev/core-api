var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrTable = require('../../streamr-table/streamr-table').StreamrTable

describe('streamr-table', function() {
	var table
	var $parent

	before(function() {
		global.$ = $
		global.window = {
			requestAnimationFrame: function(cb) {
				setTimeout(cb,0)
			}
		}
	})

	beforeEach(function() {
		$parent = $('<div></div>')
		table = new StreamrTable($parent, {})
		table.initTable()
	})

	it('should add tbody and thead', function(){
		assert($parent.find(".table-module-container tbody").length)
		assert($parent.find(".table-module-container thead").length)
	})

	describe('sending messages to table works correctly', function(){

		it('should add the headers when the message has them', function(){
			var hdrMsg = {hdr:{headers:["title1", "title2"]}}
			table.receiveResponse(hdrMsg)
			assert($($parent.find("table thead th")[0]).text() == "title1")
			assert($($parent.find("table thead th")[1]).text() == "title2")
		})

		it('should add rows to tbody when the message has field nr', function(){
			var msg = {nr:["value1", "value2"]}
			var msg2 = {nr:["value3", "value4"]}
			table.receiveResponse(msg)
			table.receiveResponse(msg2)
			assert($($($parent.find("table tbody tr")[0]).find("td")[0]).text() == "value3")
			assert($($($parent.find("table tbody tr")[0]).find("td")[1]).text() == "value4")
			assert($($($parent.find("table tbody tr")[1]).find("td")[0]).text() == "value1")
			assert($($($parent.find("table tbody tr")[1]).find("td")[1]).text() == "value2")
		})

		it('should clean the module when clean() called',function(){
			var hdrMsg = {hdr:{headers:["title1", "title2"]}}
			var msg = {nr:["value1", "value2"]}
			var msg2 = {nr:["value3", "value4"]}
			table.receiveResponse(hdrMsg)
			table.receiveResponse(msg)
			table.receiveResponse(msg2)
			assert($parent.find("tbody").children().length == 2)
			table.clean()
			assert($parent.find("tbody").children().length == 0)
		})

		it('should edit a td when msg has field e',function(){
			var msg = {nr:["value1", "value2"]}
			var msg2 = {nr:["value3", "value4"], id:"abc"}
			var eMsg = {id:"abc", e:"1", c:"edit1"}
			table.receiveResponse(msg)
			table.receiveResponse(msg2)
			assert($($($parent.find("table tbody tr")[0]).find("td")[0]).text() == "value3")
			assert($($($parent.find("table tbody tr")[0]).find("td")[1]).text() == "value4")
			assert($($($parent.find("table tbody tr")[1]).find("td")[0]).text() == "value1")
			assert($($($parent.find("table tbody tr")[1]).find("td")[1]).text() == "value2")
			table.receiveResponse(eMsg)
			assert($($($parent.find("table tbody tr")[0]).find("td")[0]).text() == "value3")
			assert($($($parent.find("table tbody tr")[0]).find("td")[1]).text() == "edit1")
			assert($($($parent.find("table tbody tr")[1]).find("td")[0]).text() == "value1")
			assert($($($parent.find("table tbody tr")[1]).find("td")[1]).text() == "value2")
		})

		it('should replace the contents when message has field nc', function() {
			table.receiveResponse({nr:["value1", "value2"]})
			assert($($($parent.find("table tbody tr")[0]).find("td")[0]).text() == "value1")
			assert($($($parent.find("table tbody tr")[0]).find("td")[1]).text() == "value2")
			table.receiveResponse({nc:[["A", "B"], ["C", "D"]]})
			assert($($($parent.find("table tbody tr")[0]).find("td")[0]).text() == "A")
			assert($($($parent.find("table tbody tr")[0]).find("td")[1]).text() == "B")
			assert($($($parent.find("table tbody tr")[1]).find("td")[0]).text() == "C")
			assert($($($parent.find("table tbody tr")[1]).find("td")[1]).text() == "D")
		})
	})

})