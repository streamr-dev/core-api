var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrTextField = require('../../streamr-text-field/streamr-text-field').StreamrTextField


describe('streamr-text-field', function() {
    var textField

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
    })

    it('must set the parent, options and data right and call createButton when creating textField', function() {
        var parent = $("#parent")
        var parent2 = parent[0]
        var data = {
            textFieldValue: "test",
            textFieldWidth: 100,
            textFieldHeight: 200
        }
        var options = {
            widthLocked: true
        }
        textField = new StreamrTextField("#parent")
        var textField2 = new StreamrTextField(parent, undefined, options)
        var textField3 = new StreamrTextField(parent2, data)

        assert.equal(textField.parent[0], parent[0])
        assert.equal(textField.value, "")
        assert.equal(textField.widthLocked, undefined)

        assert.equal(textField2.parent[0], parent[0])
        assert.equal(textField2.value, "")
        assert.equal(textField2.widthLocked, true)

        assert.equal(textField3.parent[0], parent[0])
        assert.equal(textField3.value, "test")
        assert.equal(textField3.width, 100)
        assert.equal(textField3.height, 200)
    })

    describe('render', function() {
        beforeEach(function() {
            textField = new StreamrTextField("#parent", {
                textFieldWidth: 200,
                textFieldHeight: 100
            })
        })

        it('must create the elements', function() {
            textField.render()
            var container = $("#parent div.form-inline")
            assert.equal(container.length, 1)
            var textArea = container.find("textarea.form-control")
            assert.equal(textArea.length, 1)
            var sendButton = container.find("button.send-btn")
            assert.equal(sendButton.length, 1)

            assert($("#parent").hasClass("text-field"))
        })

        it('must set the width and height of the textarea', function(done) {
            textField.setSize = function(width, height) {
                assert.equal(width, 200)
                assert.equal(height, 100)
                done()
            }
            textField.render()
        })

        it('must call bindEvents', function(done) {
            textField.bindEvents = function() {
                done()
            }
            textField.render()
        })
    })

    describe('bindEvents', function() {
        beforeEach(function() {
            textField = new StreamrTextField("#parent", {})
            textField.render()
        })

        it('must update textField.value with keyup event', function() {
            assert.equal(textField.value, "")
            $("textarea").val("test")
            $("textarea").keyup()
            assert.equal(textField.value, "test")

            $("textarea").val("still testing")
            $("textarea").keyup()
            assert.equal(textField.value, "still testing")
        })

        it('must call sendvalue with sendButton.click', function(done) {
            textField.sendValue = function(value) {
                if(value === "test")
                    done()
            }
            textField.value = "test"
            $(".send-btn").click()
        })

        it('must update the width and height with mouseup event if they have changed', function() {
            $("textarea").width(200)
            $("textarea").height(100)
            $("textarea").mouseup()
            assert(textField.width >= 200) // textField.width is outer width
            assert(textField.height >= 100) // textField.height is outer height
        })
    })

    describe('updateState', function() {
        it('must set the given value to the value of textArea', function() {
            textField = new StreamrTextField("#parent")
            textField.render()
            textField.updateState("test")
            assert.equal($("textarea").val(), "test")
        })
    })

    describe('setSize', function() {
        beforeEach(function() {
            textField = new StreamrTextField("#parent")
            textField.render()
        })

        it('must call textArea.width and textArea.height when setSize called', function(done) {
            var widthCalled = false
            textField.textArea.width = function(width) {
                if(width === 100)
                    widthCalled = true
            }
            textField.textArea.height = function(height) {
                if(widthCalled && height === 100)
                    done()
            }
            textField.setSize(100, 100)
        })

        it('must not call textArea.width if textField.widthLocked === true', function() {
            textField.widthLocked = true
            textField.textArea.width = function(width) {
                assert(false, "width() called!")
            }
            textField.setSize(100, 100)
        })

        it('must not call anything if the values are not set', function() {
            textField.textArea.width = function() {
                assert(false, "width() called!")
            }
            textField.textArea.height = function() {
                assert(false, "height() called!")
            }
            textField.setSize()
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
                    $(textField).off("input", f)
                    done()
                }
            }
            textField = new StreamrTextField("#parent")
            $(textField).on("input", f)
            list.forEach(function(value) {
                textField.sendValue(value)
            })
        })
    })

    describe('toJSON', function() {
        it('must return the JSON right', function() {
            textField = new StreamrTextField("#parent")
            textField.render()
            assert.equal(textField.toJSON().toString(), {
                textFieldValue: false
            }.toString())
            $("#parent input").prop("checked", true)
            assert.equal(textField.toJSON().toString(), {
                textFieldValue: true
            }.toString())
        })
    })
})
