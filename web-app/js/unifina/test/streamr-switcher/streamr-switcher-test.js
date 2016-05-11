
describe('streamr-switcher', function() {
	var assert
	var StreamrSwitcher
	var switcher

	before(function() {
		assert = require('assert')

		global.$ = require('jquery')(require("jsdom").jsdom().defaultView);
		StreamrSwitcher = require('../../streamr-switcher/streamr-switcher').StreamrSwitcher
	})

	after(function() {
		global.$ = undefined
	})

	beforeEach(function() {
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

	describe('updateState', function() {
		beforeEach(function() {
			switcher = new StreamrSwitcher("#parent", {}, {})
			switcher.render()
		})

		it('must set the switcher on if the value is true and switcher.value === false', function(done) {
			switcher.switcher.switcher = function(opt) {
				if(opt === "on")
					done()
				else if(opt === "off")
					assert(false, "switcher('off') called!")
			}
			switcher.updateState(true)
		})

		it('must set the switcher off if the value is false and switcher.value === true', function(done) {
			switcher.getValue = function(){return true}
			switcher.switcher.switcher = function(opt) {
				if(opt === "off")
					done()
				else if(opt === "on")
					assert(false, "switcher('on') called!")
			}
			switcher.updateState(false)
		})

		it('must not set the switcher on if the value is true and switcher.value === true', function() {
			switcher.getValue = function(){return true}
			switcher.switcher.switcher = function() {
				assert(false, "switcher() called!")
			}
			switcher.updateState(true)
		})

		it('must not set the switcher off if the value is true and switcher.value === false', function() {
			switcher.value = false
			switcher.switcher.switcher = function() {
				assert(false, "switcher() called!")
			}
			switcher.updateState(false)
		})

		it('must set switcher.mustSendValue to false and back to true', function(done) {
			var hasCalledSwitcher = false
			switcher.switcher.switcher = function() {
				assert(switcher.mustSendValue === false)
				hasCalledSwitcher = true
			}
			switcher.updateState(true)
			setTimeout(function() {
				assert(switcher.mustSendValue)
				assert(hasCalledSwitcher)
				done()
			}, 10)
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

	describe('receiveResponse', function() {
		beforeEach(function() {
			switcher = new StreamrSwitcher("#parent", {}, {})
		})

		it('must call updateState(false) if p.switcherValue === false', function(done) {
			switcher.updateState = function(value) {
				if(value === false)
					done()
			}
			switcher.receiveResponse({
				switcherValue: false
			})
		})

		it('must call updateState(true) if p.switcherValue === true', function(done) {
			switcher.updateState = function(value) {
				if(value === true)
					done()
			}
			switcher.receiveResponse({
				switcherValue: true
			})
		})

		it('must not call updateState() if p.switcherValue === anything else', function() {
			switcher.updateState = function() {
				assert(false, "updateState() called!")
			}
			switcher.receiveResponse({
				wrongValue: true
			})
			switcher.receiveResponse({
				switcherValue: "true"
			})
			switcher.receiveResponse({
				switcherValue: "false"
			})
			switcher.receiveResponse({
				switcherValue: 0
			})
			switcher.receiveResponse({
				switcherValue: undefined
			})
		})
	})
})