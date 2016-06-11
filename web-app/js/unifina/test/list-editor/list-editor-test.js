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

    describe('ValueInList', function() {
        describe('construction', function() {
            it('should be able to parse a json object', function() {
                var val = new ListEditor.ValueInList("{\"foo\":5}")
                assert.deepEqual(val.get('value'), {foo:5})
            })
            it('should be able to parse a json array', function() {
                var val = new ListEditor.ValueInList("[5,6]")
                assert.deepStrictEqual(val.get('value'), [5,6])
            })
            it('should be able to parse numbers', function() {
                var val = new ListEditor.ValueInList("5")
                assert.strictEqual(val.get('value'), 5)
            })
            it('should leave a string untouched', function() {
                var val = new ListEditor.ValueInList("foo")
                assert.strictEqual(val.get('value'), "foo")
            })
        })

        describe('toJSON()', function() {
            it('should return the unwrapped value', function() {
                var val = new ListEditor.ValueInList("foo")
                assert.strictEqual(val.toJSON(), "foo")
            })
            it('should return the empty string for an empty string', function() {
                var val = new ListEditor.ValueInList("")
                assert.strictEqual(val.toJSON(), "")
            })
        })

        describe('isEmpty()', function() {
            it('should return true for empty strings', function() {
                var val = new ListEditor.ValueInList("")
                assert(val.isEmpty())
            })
            it('should return false for non-empty strings', function() {
                var val = new ListEditor.ValueInList("asd")
                assert(!val.isEmpty())
            })
        })
    })

    describe("ValueList", function() {
        describe("construction", function() {
            it("should accept list of model values", function() {
                var list = new ListEditor.ValueList([1,"2","foo"])
                assert.deepStrictEqual(
                    list.models.map(function(model) {
                        return model.get('value');
                    }),
                    [1,2,"foo"]
                )
            })
        })

        describe("toJSON()", function() {
            it("should filter out empty values", function() {
                var list = new ListEditor.ValueList([1,"2","", "foo", ""])
                assert.deepStrictEqual(list.toJSON(), [1,2,"foo"])
            })
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
            var list = new ListEditor.ValueList(["1", "2", "3"])

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
            var list = new ListEditor.ValueList(["1","2","3"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            assert.equal(fixture.find("table tbody tr").length, 4)

            fixture.find("table tbody tr:last input.value").val("four")
            fixture.find("table tbody tr:last input.value").trigger("change")

            var result = list.toJSON()
            assert.deepEqual(result, [1,2,3,"four"])
        })

        it('should ignore empty values', function () {
            var list = new ListEditor.ValueList(["1"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })
            editor.getAddButton().click()

            var result = list.toJSON()
            assert.deepEqual(result, [1])
        })

        it('should remove rows when remove button is clicked', function () {
            var list = new ListEditor.ValueList(["1","2","3"])

            var editor = new ListEditor({
                el: fixture,
                collection: list
            })

            fixture.find("table tbody tr:eq(1) .delete").click()
            assert.equal(fixture.find("table tbody tr").length, 2)

            var result = list.toJSON()
            assert.deepEqual(result, [1,3])
        })

    })

})