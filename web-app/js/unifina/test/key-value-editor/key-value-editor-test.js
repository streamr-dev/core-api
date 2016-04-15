var assert = require('assert')
var jsdom = require("jsdom")

var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('backbone')
var Mustache = require('mustache')

describe('dashboard-editor', function() {

    var KeyValuePairEditor
    var KeyValuePairList
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

        KeyValuePairEditor = require('../../key-value-editor/key-value-editor').KeyValuePairEditor
        KeyValuePairList = require('../../key-value-editor/key-value-editor').KeyValuePairList
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

    describe("KeyValuePairEditor", function() {

        beforeEach(function () {

        })

        it('should render an empty table when initialized with no collection', function () {
            var editor = new KeyValuePairEditor({
                el: fixture
            })
            assert(fixture.find("table").length > 0)
            assert.equal(fixture.find("table tbody tr").length, 0)
        })

        it('should render a table row for each key-value pair', function () {
            var list = new KeyValuePairList()
            list.fromJSON({
                foo: "1",
                bar: "2",
                xyz: "3"
            })

            var editor = new KeyValuePairEditor({
                el: fixture,
                collection: list
            })
            assert.equal(fixture.find("table tbody tr").length, 3)
            assert.equal(fixture.find("table tbody tr:eq(2) input.key").val(), "xyz")
            assert.equal(fixture.find("table tbody tr:eq(2) input.value").val(), "3")
        })

        it('should should add an empty table row when the add button is clicked', function () {
            var editor = new KeyValuePairEditor({
                el: fixture
            })
            editor.getAddButton().click()
            assert.equal(fixture.find("table tbody tr").length, 1)
            assert.equal(fixture.find("table tbody tr input.key").val(), "")
            assert.equal(fixture.find("table tbody tr input.value").val(), "")
        })

        it('should update the list with new keys and values', function () {
            var list = new KeyValuePairList()
            list.fromJSON({
                foo: "1",
                bar: "2",
                xyz: "3"
            })

            var editor = new KeyValuePairEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            assert.equal(fixture.find("table tbody tr").length, 4)

            fixture.find("table tbody tr:last input.key").val("new")
            fixture.find("table tbody tr:last input.key").trigger("change")
            fixture.find("table tbody tr:last input.value").val("value")
            fixture.find("table tbody tr:last input.value").trigger("change")

            var result = list.toJSON()
            assert.equal(Object.keys(result).length, 4)
            assert.equal(result.foo, "1")
            assert.equal(result.bar, "2")
            assert.equal(result.xyz, "3")
            assert.equal(result.new, "value")
        })

        it('should ignore empty keys', function () {
            var list = new KeyValuePairList()
            list.fromJSON({
                foo: "1"
            })

            var editor = new KeyValuePairEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            var result = list.toJSON()
            assert.equal(Object.keys(result).length, 1)
            assert.equal(result.foo, "1")
        })

        it('should remove key-value pairs when remove button is clicked', function () {
            var list = new KeyValuePairList()
            list.fromJSON({
                foo: "1",
                bar: "2",
                xyz: "3"
            })

            var editor = new KeyValuePairEditor({
                el: fixture,
                collection: list
            })

            fixture.find("table tbody tr:eq(1) .delete").click()
            assert.equal(fixture.find("table tbody tr").length, 2)

            var result = list.toJSON()
            assert.equal(Object.keys(result).length, 2)
            assert.equal(result.bar, undefined)
        })

    })

})