var assert = require('assert')
var jsdom = require("jsdom")

var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('backbone')
var Mustache = require('mustache')

describe('list-editor', function() {

    var ListEditor
    var fixture

    before(function(){
        global.$ = $
        global._ = _
        global.window = window
        global.document = window.document
        global.Backbone = Backbone
        Backbone.$ = $
        global.jQuery = $
        global.Mustache = Mustache

        ListEditor = require('../../list-editor/list-editor')
    })

    beforeEach(function() {
        fixture = $("<div id='#fixture'></div>")
        $("body").append(fixture)
    })

    afterEach(function(){
        $("body").empty()
    })

    after(function(){
        global.$ = undefined
        global._ = undefined
        global.window = undefined
        global.document = undefined
        global.Backbone = undefined
        Backbone.$ = undefined
        global.jQuery = undefined
        global.Mustache = undefined
    })

    describe("ValueInList", function() {
        it('should take a primitive value as constructor argument', function() {
            var val = new ListEditor.ValueInList("foo")
            assert.equal(val.get('value'), "foo")
        })
        it('should convert back to the primitive value with toJSON', function() {
            var val = new ListEditor.ValueInList("foo")
            assert.equal(val.toJSON(), "foo")
        })
        it('should be able to parse a json object', function() {
            var val = new ListEditor.ValueInList("{\"foo\":5}")
            assert.equal(val.toJSON(), {foo:5})
        })
        it('should be able to parse a json array', function() {
            var val = new ListEditor.ValueInList("[5,6]")
            assert.equal(val.toJSON(), [5,6])
        })
        it('should be able to parse numbers', function() {
            var val = new ListEditor.ValueInList("5")
            assert.equal(val.toJSON(), 5)
        })
    })

    describe("ListEditor", function() {

        beforeEach(function () {

        })

        it('should render an empty table when initialized with no collection', function () {
            var editor = new ListEditor({
                el: fixture
            })
            assert(fixture.find("table").length > 0)
            console.log(fixture.find("table tbody tr").html())
            assert.equal(fixture.find("table tbody tr").length, 0)
        })

        it('should render a table row for each list item', function () {
            var list = new ListEditor.ValueList()
            list.fromJSON(["1", "2", "3"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })
            assert.equal(fixture.find("table tbody tr").length, 3)
            assert.equal(fixture.find("table tbody tr:eq(2) input.value").val(), "3")
        })

        it('should should add an empty table row when the add button is clicked', function () {
            var editor = new ListEditor({
                el: fixture
            })
            editor.getAddButton().click()
            assert.equal(fixture.find("table tbody tr").length, 1)
            assert.equal(fixture.find("table tbody tr input.value").val(), "")
        })

        it('should update the list with new keys and values', function () {
            var list = new ListEditor.ValueList()
            list.fromJSON(["1","2","3"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            assert.equal(fixture.find("table tbody tr").length, 4)

            fixture.find("table tbody tr:last input.value").val("four")
            fixture.find("table tbody tr:last input.value").trigger("change")

            var result = list.toJSON()
            assert.equal(result.length, 4)
            assert.equal(result[0], "1")
            assert.equal(result[1], "2")
            assert.equal(result[2], "3")
            assert.equal(result[3], "four")
        })

        it('should ignore empty values', function () {
            var list = new ListEditor.ValueList()
            list.fromJSON(["1"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            var result = list.toJSON()
            assert.equal(result.length, 1)
            assert.equal(result[0], "1")
        })

        it('should remove rows when remove button is clicked', function () {
            var list = new ListEditor.ValueList()
            list.fromJSON(["1","2","3"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })

            fixture.find("table tbody tr:eq(1) .delete").click()
            assert.equal(fixture.find("table tbody tr").length, 2)

            var result = list.toJSON()
            assert.equal(result.length, 2)
            assert.equal(result[0], "1")
            assert.equal(result[1], "2")
        })

    })

})