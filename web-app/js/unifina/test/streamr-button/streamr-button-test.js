var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrButton = require('../../streamr-button/streamr-button').StreamrButton


describe('streamr-button', function() {
	var button

	before(function() {
		global.$ = $
	})

	after(function() {
		global.$ = undefined
	})

	beforeEach(function() {
		$("body").html("<div id='parent'></div>")
	})

	afterEach(function() {
		$("body").empty()
		button = undefined
	})

	it('must set the parent, options and data right and call createButton when creating button', function() {
		var parent = $("#parent")
		var parent2 = parent[0]
		var data = {a: 1, b: 2}
		var options = {c: 3, d: 4}
		var options2 = {alwaysEnabled: true}
		button = new StreamrButton("#parent", data)
		var button2 = new StreamrButton(parent, data, options)
		var button3 = new StreamrButton(parent2, data, options2)

		assert.equal(button.parent[0], parent[0])
		assert.equal(button.options, undefined)
		assert.equal(button.data, data)
		assert.equal(button2.parent[0], parent[0])
		assert.equal(button2.options, options)
		assert.equal(button2.data, data)
		assert.equal(button3.parent[0], parent[0])
		assert.equal(button3.alwaysEnabled, true)
		assert.equal(button3.data, data)
	})

	describe("render", function() {
		beforeEach(function() {
			button = new StreamrButton("#parent", {}, {})
		})

		it('must create the button and add class "disabled" to it', function() {
			button.render()
			assert.equal($("#parent .button-module-button").length, 1)
			assert($("#parent .button-module-button").hasClass("disabled"))
		})

		it('must call getButtonNameFromData and setName', function(done) {
			var getButtonNameFromDataCalled = false
			button.getButtonNameFromData = function() {
				getButtonNameFromDataCalled = true
			}
			button.setName = function() {
				if(getButtonNameFromDataCalled === true) {
					done()
				}
			}
			button.render()
		})

		it('must not add class "disabled" if button.alwaysEnabled == true', function(){
			button.alwaysEnabled = true
			button.render()
			assert(!($("#parent .button-module-button").hasClass("disabled")))
		})

		it('must bind sendValue to button.click', function(done) {
			button.render()
			var superSendValue = StreamrButton.prototype.sendValue
			StreamrButton.prototype.sendValue = function() {
				StreamrButton.prototype.sendValue = superSendValue
				done()
			}
			$(".button-module-button").click()
		})
	})

	describe("updateState", function() {
		it('must call setName with the given state', function(done) {
			button = new StreamrButton("#parent", {}, {})
			button.setName = function(state) {
				if(state === "test")
					done()
			}
			button.updateState("test")
		})
	})

	describe("getButtonNameFromData", function() {
		beforeEach(function() {
			button = new StreamrButton("#parent", {}, {})
		})

		it('must return undefined if no correct data found', function() {
			var data = {
				a: "no params field"
			}
			var data2 = {
				params: [{
					name: "a"
				}, {
					name: "b"
				}]
			}
			assert.equal(button.getButtonNameFromData(data), undefined)
			assert.equal(button.getButtonNameFromData(data2), undefined)
			button.data = data
			assert.equal(button.getButtonNameFromData(), undefined)
			button.data = data2
			assert.equal(button.getButtonNameFromData(), undefined)
		})

		it('must return correct value', function() {
			var data = {
				params: [{
					name: "buttonName",
					value: "test"
				}]
			}
			var data2 = {
				params: [{
					name: "buttonName",
					value: "test"
				},{
					name: "buttonValue",
					value: "test2"
				}]
			}
			assert.equal(button.getButtonNameFromData(data), "test")
			assert.equal(button.getButtonNameFromData(data2), "test")
			button.data = data
			assert.equal(button.getButtonNameFromData(), "test")
			button.data = data2
			assert.equal(button.getButtonNameFromData(), "test")
		})
	})

	describe("receiveResponse", function() {
		beforeEach(function() {
			button = new StreamrButton("#parent", {}, {})
		})

		it('must not call setName if the response does not have key buttonName', function() {
			button.setName = function() {
				assert(false, "setName call when shouldn't!")
			}
			button.receiveResponse({
				a: 1,
				b: 2
			})
		})

		it('must call setName with correct value if there is buttonName in the payload', function(done) {
			button.setName = function(name) {
				assert.equal(name, "test")
				done()
			}
			button.receiveResponse({
				buttonName: "test"
			})
		})
	})

	describe("sendValue", function() {
		it('must trigger "input" when called', function(done) {
			button = new StreamrButton("#parent", {}, {})
			var superTrigger = $.fn.trigger
			$.fn.trigger = function(e) {
				assert.equal(e, "input")
				$.fn.trigger = superTrigger
				done()
			}
			button.sendValue()
		})
	})

	describe("setName", function() {
		beforeEach(function() {
			button = new StreamrButton("#parent", {}, {})
			button.name = ""
			button.render()
		})

		it('must set the name to "button" if undefined', function() {
			button.setName()
			assert.equal(button.name, "button")
			assert.equal($("#parent").text(), "button")
		})

		it('must set the name to "test" if it is set', function() {
			button.setName("test")
			assert.equal(button.name, "test")
			assert.equal($("#parent").text(), "test")
		})

		it('must trigger "update"', function(done) {
			$(button).one("update", function () {
				done()
			})
			button.setName()
		})
	})

	describe("disable", function() {
		it('must not add class if button.alwaysEnabled == true', function() {
			button = new StreamrButton("#parent", {}, {
				alwaysEnabled: true
			})
			button.render()
			button.disable()
			assert(!($("#parent .button-module-button").hasClass("disabled")))
		})
		it('must add class "disabled" to the button', function() {
			button = new StreamrButton("#parent", {}, {})
			button.render()
			button.disable()
			assert($("#parent .button-module-button").hasClass("disabled"))
		})
	})

	describe("enable", function() {
		it('must remove the class "disabled" when called', function() {
			button = new StreamrButton("#parent", {}, {})
			button.render()
			button.disable()
			button.enable()
			assert(!$("#parent .button-module-button").hasClass("disabled"))
		})
	})

})