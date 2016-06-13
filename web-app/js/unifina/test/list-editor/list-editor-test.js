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
        describe('isEmpty()', function() {
            it('should return true for empty strings', function() {
                var val = new ListEditor.ValueInList({value: ""})
                assert(val.isEmpty())
            })
            it('should return false for other objects', function() {
                var val = new ListEditor.ValueInList({value: "asd"})
                assert(!val.isEmpty())
            })
        })
    })

    describe("ValueList", function() {
        describe("construction", function() {
            it("should accept a list of primitive values", function() {
                var list = new ListEditor.ValueList([1,"2","foo"])
                assert.deepStrictEqual(
                    list.models.map(function(model) {
                        return model.get('value');
                    }),
                    [1,"2","foo"]
                )
            })
            it("should accept models in the list", function() {
                var list = new ListEditor.ValueList([1,new ListEditor.ValueInList({value:"2"})])
                assert.deepStrictEqual(
                    list.models.map(function(model) {
                        return model.get('value');
                    }),
                    [1,"2"]
                )
            })
        })

        describe("toJSON()", function() {
            it("should unwrap values", function() {
                var list = new ListEditor.ValueList([
                    new ListEditor.ValueInList({value: 1}),
                    new ListEditor.ValueInList({value: "2"}),
                    new ListEditor.ValueInList({value: "foo"})
                ])
                assert.deepStrictEqual(list.toJSON(), [1,"2","foo"])
            })
            it("should filter out empty values", function() {
                var list = new ListEditor.ValueList([
                    new ListEditor.ValueInList({value: 1}),
                    new ListEditor.ValueInList({value: ""}),
                    new ListEditor.ValueInList({value: "foo"}),
                    new ListEditor.ValueInList({value: ""})
                ])
                assert.deepStrictEqual(list.toJSON(), [1,"foo"])
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

        it('should update the list with new values', function () {
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

        it('should render stringifications of non-string objects', function() {
            var list = new ListEditor.ValueList([{foo: 4}, 5, true])
            new ListEditor({
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