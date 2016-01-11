var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);
var StreamrSwitcher = require('../../streamr-switcher/streamr-switcher').StreamrSwitcher

describe('streamr-switcher', function() {
	var switcher

	before(function() {
		global.$ = $
	})

	after(function() {
		global.$ = undefined
	})

	beforeEach(function() {
		$.fn.switcher = function() {}
		$("body").html("<div id='parent'></div>")
	})

	afterEach(function() {
		$("body").empty()
		switcher = undefined
	})

	it('must set the parent, options and data right and call createButton when creating button', function() {
		var parent = $("#parent")
		var parent2 = parent[0]
		var data = {switcherValue: true}
		switcher = new StreamrSwitcher("#parent")
		var switcher2 = new StreamrSwitcher(parent)
		var switcher3 = new StreamrSwitcher(parent2, data)

		assert.equal(switcher.parent[0], parent[0])

		assert.equal(switcher2.parent[0], parent[0])

		assert.equal(switcher3.parent[0], parent[0])
		assert.equal(switcher3.checked, true)
	})

	describe('render', function() {
		beforeEach(function() {
			switcher = new StreamrSwitcher("#parent", {})
		})
		it('must create the switcher correctly', function() {
			switcher.render()
			assert.equal($("#parent input").length, 1)
		})
		it('must check the checkbox if data.switcherValue == true', function() {
			switcher = new StreamrSwitcher("#parent", {switcherValue: true})
			switcher.render()
			assert.equal($("#parent input").attr("checked"), "checked")
		})
		it('must call $.fn.switcher when created', function(done) {
			$.fn.switcher = function(data) {
				assert.equal(data.theme, "square")
				assert.equal(data.on_state_content, "1")
				assert.equal(data.off_state_content, "0")
				done()
			}
			switcher.render()
		})
	})

	describe('getDragCancelAreas', function() {
		it('must return right value', function() {
			switcher = new StreamrSwitcher("#parent", {}, {})
			assert.equal(switcher.getDragCancelAreas(), "div.switcher-inner")
		})
	})

	describe('getValue', function() {
		beforeEach(function() {
			switcher = new StreamrSwitcher("#parent", {}, {})
			switcher.render()
		})
		it('must return true if the checkbox is checked', function() {
			$("#parent input").prop("checked", true)
			assert.equal(switcher.getValue(), true)
		})
		it('must return false if the checkbox is checked', function() {
			$("#parent input").prop("checked", true)
			$("#parent input").prop("checked", false)
			assert.equal(switcher.getValue(), false)
		})
	})

	describe('toJSON', function() {
		it('must return the JSON right', function() {
			switcher = new StreamrSwitcher("#parent", {}, {})
			switcher.render()
			assert.equal(switcher.toJSON().toString(), {
				switcherValue: false
			}.toString())
			$("#parent input").prop("checked", true)
			assert.equal(switcher.toJSON().toString(), {
				switcherValue: true
			}.toString())
		})
	})

	describe('sendValue', function() {
		it('must trigger "input" with the right value', function(done) {
			var list = ["a", "b", "c"]
			var i = 0
			var f = function(e, value) {
				assert.equal(value, list[i])
				i++
				if(i === 3) {
					$(switcher).off("input", f)
					done()
				}
			}
			switcher = new StreamrSwitcher("#parent", {}, {})
			$(switcher).on("input", f)
			list.forEach(function(value) {
				switcher.sendValue(value)
			})
		})
	})
})