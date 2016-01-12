var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);
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
        var data = {textFieldValue: "test"}
        textField = new StreamrTextField("#parent")
        var textField2 = new StreamrTextField(parent)
        var textField3 = new StreamrTextField(parent2, data)

        assert.equal(textField.parent[0], parent[0])
        assert.equal(textField.value, "")

        assert.equal(textField2.parent[0], parent[0])
        assert.equal(textField2.value, "")

        assert.equal(textField3.parent[0], parent[0])
        assert.equal(textField3.value, "test")
    })

    describe('render', function() {
        beforeEach(function() {
            textField = new StreamrTextField("#parent", {})
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

        it('must save the width and height of the textarea', function() {
            textField.render()
            assert.equal($("textarea").css("height"), textField.height+"px")
            assert.equal($("textarea").css("width"), textField.width+"px")
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
            assert.equal(textField.width, 200)
            assert.equal(textField.height, 100)
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
