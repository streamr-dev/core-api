var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);
var StreamrTextField = require('../../streamr-textfield/streamr-textfield').StreamrTextField


describe('streamr-button', function() {
    var textfield

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

    it('must set the parent, options and data right and call createButton when creating textfield', function() {
        var parent = $("#parent")
        var parent2 = parent[0]
        var data = {textFieldValue: "test"}
        textfield = new StreamrTextField("#parent")
        var textfield2 = new StreamrTextField(parent)
        var textfield3 = new StreamrTextField(parent2, data)

        assert.equal(textfield.parent[0], parent[0])
        assert.equal(textfield.value, "")

        assert.equal(textfield2.parent[0], parent[0])
        assert.equal(textfield2.value, "")

        assert.equal(textfield3.parent[0], parent[0])
        assert.equal(textfield3.value, "test")
    })
})
