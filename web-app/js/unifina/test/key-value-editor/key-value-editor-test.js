var assert = require('assert')
var jsdom = require("jsdom")

var window = jsdom.jsdom().defaultView
var $ = require('jquery')(window);
var _ = require('underscore')
var Backbone = require('backbone')
var Mustache = require('mustache')

describe('key-value-editor', function() {

    var KeyValuePairEditor
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

        global.ListEditor = require('../../list-editor/list-editor')
        KeyValuePairEditor = require('../../key-value-editor/key-value-editor')
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

    describe("KeyValuePair", function() {
        it('is empty when the key is an empty string', function() {
            var kvp = new KeyValuePairEditor.KeyValuePair({key: '', value: 'foo'})
            assert(kvp.isEmpty())
        })
        it('is not empty when the key is not an empty string', function() {
            var kvp = new KeyValuePairEditor.KeyValuePair({key: 'asd', value: ''})
            assert(!kvp.isEmpty())
        })
    })

    describe("KeyValuePairList", function() {
        describe("constructor", function() {
            it("should create an empty list with no arguments", function() {
                kvpList = new KeyValuePairEditor.KeyValuePairList()
                assert.equal(kvpList.models.length, 0)
            })
            it("should accept an object as argument and produce a list of key-value pairs", function() {
                kvpList = new KeyValuePairEditor.KeyValuePairList({foo: 'bar', xyz: 5})
                assert.equal(kvpList.models.length, 2)
                assert.equal(kvpList.models[0].get('key'), 'foo')
                assert.equal(kvpList.models[0].get('value'), 'bar')
                assert.equal(kvpList.models[1].get('key'), 'xyz')
                assert.equal(kvpList.models[1].get('value'), 5)
            })
        })

        describe("toJSON()", function() {
            it("should return a map", function() {
                kvpList = new KeyValuePairEditor.KeyValuePairList({foo: 'bar', xyz: 5})
                assert.deepStrictEqual(kvpList.toJSON(), {foo: 'bar', xyz: 5})
            })
            it("should filter out empty keys", function() {
                kvpList = new KeyValuePairEditor.KeyValuePairList()
                kvpList.add(new KeyValuePairEditor.KeyValuePair({key:'', value:4}))
                kvpList.add(new KeyValuePairEditor.KeyValuePair({key:'foo', value:''}))
                assert.deepStrictEqual(kvpList.toJSON(), {foo: ''})
            })
        })
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
            var list = new KeyValuePairEditor.KeyValuePairList({
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
            var list = new KeyValuePairEditor.KeyValuePairList({
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

            assert.deepStrictEqual(
                list.toJSON(),
                {
                    foo: "1",
                    bar: "2",
                    xyz: "3",
                    new: "value"
                }
            )
        })

        it('should render stringifications of non-string object values', function() {
            var list = new KeyValuePairEditor.KeyValuePairList({bar: {foo: 4}, xyz: 5, truth: true})
            new KeyValuePairEditor({
                el: fixture,
                collection: list
            })

            assert.equal(
                fixture.find("table tbody tr:eq(0) input.value").val(),
                JSON.stringify({foo: 4})
            )
            assert.equal(
                fixture.find("table tbody tr:eq(1) input.value").val(),
                "5"
            )
            assert.equal(
                fixture.find("table tbody tr:eq(2) input.value").val(),
                "true"
            )
        })

        it('should ignore empty keys', function () {
            var list = new KeyValuePairEditor.KeyValuePairList({
                foo: "1"
            })

            var editor = new KeyValuePairEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            assert.deepStrictEqual(
                list.toJSON(),
                {
                    foo: "1",
                }
            )
        })

        it('should remove key-value pairs when remove button is clicked', function () {
            var list = new KeyValuePairEditor.KeyValuePairList({
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

            assert.deepStrictEqual(
                list.toJSON(),
                {
                    foo: "1",
                    xyz: "3"
                }
            )
        })

    })

})