var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().defaultView);
var StreamrSearch = require('../../streamr-search/streamr-search').StreamrSearch


describe('streamr-search', function() {
	var search

	before(function() {
		global.$ = $
		global.Streamr = {}
		global.Streamr.createLink = function(opt) {
			return opt.uri
		}
	})

	after(function() {
		global.$ = undefined
	})

	beforeEach(function() {
		$("body").html("<div id='parent'><input style='width:100px;height:10px' id='search'/></div>")
		$.fn.typeahead = function(){}
		global.$.get = function(){}
	})

	afterEach(function() {
		$("body").empty()
		search = undefined
	})


	describe('ModuleSearchModule', function() {
		it('must first get the modules', function(){
			global.$.get = function(url, cb) {
				assert.equal("api/v1/modules", url)
			}
			search = new StreamrSearch($("#search"), ["module"])
		})
		it('must return right kind of json', function() {
			$.fn.typeahead = function(options, module, stream) {
				assert.equal(stream, undefined)
				assert.equal(module.name, "Modules")
				assert.equal(module.displayKey, "name")
				assert.equal(module.limit, Infinity)
			}
			search = new StreamrSearch($("#search"), [{name: "module"}])
		})
		it('must search correctly with ModuleSearchModule.source', function() {
			var modules = [{name: "aaa"},{name: "baa"},{name: "xyz"},{name: "a"},{name: "bbb",alternativeNames: "aaa"}, {name: "bba"}]
			global.$.get = function(url, cb) {
				cb(modules)
			}
			$.fn.typeahead = function(opt, module) {
				module.source("a", function(arr) {
					assert.equal(JSON.stringify(arr), JSON.stringify([{
						name: "a",
						resultType: "module"
					},{
						name: "aaa",
						resultType: "module"
					},{
						name: "baa",
						resultType: "module"
					}, {
						name: "bba",
						resultType: "module"
					},{
						name: "bbb",
						alternativeNames: "aaa",
						resultType: "module"
					}]))
				})
			}
			search = new StreamrSearch($("#search"), [{name: "module"}])
		})
		it('must limit the search results correctly', function() {
			var modules = [{name: "aaa"},{name: "baa"},{name: "xyz"},{name: "a"},{name: "bbb",alternativeNames: "aaa"}, {name: "bba"}]
			global.$.get = function(url, cb) {
				cb(modules)
			}
			$.fn.typeahead = function(opt, module) {
				module.source("a", function(arr) {
					assert.equal(JSON.stringify(arr), JSON.stringify([{
						name: "a",
						resultType: "module"
					},{
						name: "aaa",
						resultType: "module"
					},{
						name: "baa",
						resultType: "module"
					}]))
				})
			}
			search = new StreamrSearch($("#search"), [{name: "module", limit: 3}])
		})
	})
	describe('StreamSearchModule', function() {
		it('must return right kind of json', function() {
			$.fn.typeahead = function(options, stream) {
				assert.equal(stream.name, "Streams")
				assert.equal(stream.displayKey, "name")
				assert.equal(stream.limit, Infinity)
			}
			search = new StreamrSearch($("#search"), [{name: "stream"}])
		})
		it('must search correctly with ModuleSearchModule.source', function() {
			global.$.get = function(url, cb) {
				if(url == "api/v1/streams?public=true&search=a")
					cb([{name: "aaa"},{name: "baa"},{name: "xyz"},{name: "a"},{name: "bbba"}, {name: "bba"}])
				else
					cb(false)
			}
			$.fn.typeahead = function(opt, stream) {
				stream.source("a", function() {
					assert(false)
				}, function(arr) {
					assert.equal(JSON.stringify(arr), JSON.stringify([{
						name: "a",
						resultType: "stream"
					},{
						name: "aaa",
						resultType: "stream"
					},{
						name: "baa",
						resultType: "stream"
					}, {
						name: "bba",
						resultType: "stream"
					},{
						name: "bbba",
						resultType: "stream"
					}]))
				})
			}
			search = new StreamrSearch($("#search"), [{name: "stream"}])
		})
		it('must search correctly with ModuleSearchModule.source', function() {
			global.$.get = function(url, cb) {
				if(url == "api/v1/streams?public=true&search=a")
					cb([{name: "aaa"},{name: "baa"},{name: "xyz"},{name: "a"},{name: "bbba"}, {name: "bba"}])
				else
					cb(false)
			}
			$.fn.typeahead = function(opt, stream) {
				stream.source("a", function() {
					assert(false)
				}, function(arr) {
					assert.equal(JSON.stringify(arr), JSON.stringify([{
						name: "a",
						resultType: "stream"
					},{
						name: "aaa",
						resultType: "stream"
					},{
						name: "baa",
						resultType: "stream"
					}]))
				})
			}
			search = new StreamrSearch($("#search"), [{name: "stream", limit: 3}])
		})
	})

	describe('constructor', function () {
		it('must use the correct default options', function() {
			search = new StreamrSearch($("#search"), [])
			assert.equal(search.options.highlight, true)
			assert.equal(search.options.hint, true)
			assert.equal(search.options.autocomplete, true)
			assert.equal(search.options.inBody, false)
			assert.equal(search.options.emptyAfterSelection, true)
			assert.deepEqual({
				input: "streamr-search-input",
				hint: "streamr-search-hint",
				menu: "streamr-search-menu",
				dataset: "streamr-search-dataset",
				suggestion: "streamr-search-suggestion",
				empty: "streamr-search-empty",
				open: "streamr-search-open",
				cursor: "streamr-search-cursor",
				highlight: "streamr-search-highlight"
			}, search.options.classNames)
		})
		it('must use the options given as a parameter', function() {
			search = new StreamrSearch($("#search"), [], {
				highlight: false,
				autocomplete: false,
				classNames: {
					empty: "test",
					cursor: "test2"
				}
			})
			assert.equal(search.options.highlight, false)
			assert.equal(search.options.hint, true)
			assert.equal(search.options.autocomplete, false)
			assert.equal(search.options.inBody, false)
			assert.equal(search.options.emptyAfterSelection, true)
			assert.deepEqual({
				input: "streamr-search-input",
				hint: "streamr-search-hint",
				menu: "streamr-search-menu",
				dataset: "streamr-search-dataset",
				suggestion: "streamr-search-suggestion",
				empty: "test",
				open: "streamr-search-open",
				cursor: "test2",
				highlight: "streamr-search-highlight"
			}, search.options.classNames)
		})
		it('creates a search menu with random id', function() {
			var superNow = global.Date.now
			global.Date.now = function() {
				return "test"
			}
			search = new StreamrSearch($("#search"), [])
			assert(search.streamrSearchMenu.hasClass("streamr-search-menu"))
			assert(search.streamrSearchMenu.hasClass("streamr-search-empty"))
			assert(search.streamrSearchMenu.is("#streamr-search-menu-test"))
			global.Date.now = superNow
		})
		it('must append the menu to body if inBody == true', function() {
			search = new StreamrSearch($("#search"), [], {inBody: true})
			assert.equal($("body > .streamr-search-menu").length, 1)
		})
		it('must create own container and replace the input with it if inBody == false', function() {
			search = new StreamrSearch($("#search"), [])
			assert.equal($("#parent > input").length, 0)
			assert.equal($("#parent > .twitter-typeahead").length, 1)
			assert.equal($("#parent > .twitter-typeahead > input").length, 1)
			assert.equal($("#parent > .twitter-typeahead > .streamr-search-menu").length, 1)
		})
		it('must use the created menu as options.menu', function() {
			search = new StreamrSearch($("#search"), [], {inBody: true})
			search.options.menu.hasClass('streamr-search-menu')
			search = new StreamrSearch($("#search"), [], {inBody: false})
			search.options.menu.hasClass('streamr-search-menu')
		})
		it('it must set the min-width of the menu', function() {
			search = new StreamrSearch($("#search"), [], {inBody: true})
			// The width of the input is set to 100, but the outerWidth is a little bit wider
			assert.equal(search.streamrSearchMenu.css("min-width"), "106px")
		})
		it('must call typeahead with correct el and options', function() {
			$.fn.typeahead = function(options) {
				assert($(this).is("#search"))
				assert.equal(options.highlight, false)
			}
			search = new StreamrSearch($("#search"), [], {highlight: false})
		})
		describe('event typeahead:selected', function() {
			afterEach(function() {
				$("#search").off("typeahead:selected")
			})
			it('must call setValue("") if emptyAfterSelection == true', function(done) {
				search = new StreamrSearch($("#search"), [], {emptyAfterSelection: true}, function(){})
				search.setValue = function(value) {
					if(value == "")
						done()
				}
				$("#search").trigger("typeahead:selected")
			})
			it('must call callback', function(done) {
				search = new StreamrSearch($("#search"), [], {}, function() {
					done()
				})
				search.setValue = function(){}
				$("#search").trigger("typeahead:selected")
			})
			it('must not call setValue("") if emptyAfterSelection == false', function(done) {
				search = new StreamrSearch($("#search"), [], {emptyAfterSelection: false}, function(){
					done()
				})
				search.setValue = function() {
					assert(false)
				}
				$("#search").trigger("typeahead:selected")
			})
		})
		describe('event typeahead:render', function() {
			afterEach(function() {
				$("#search").off("typeahead:render")
			})
			it('must call redrawMenu if inBody == true', function(done) {
				search = new StreamrSearch($("#search"), [], {inBody: true}, function(){})
				search.redrawMenu = function() {
					done()
				}
				$("#search").trigger("typeahead:render")
			})
			it('must not call redrawMenu if inBody == false', function() {
				search = new StreamrSearch($("#search"), [], {}, function(){})
				search.redrawMenu = function() {
					assert(false)
				}
				$("#search").trigger("typeahead:render")
			})
		})
	})

	describe('setValue', function() {
		it('must set the value', function(done) {
			search = new StreamrSearch($("#search"), [])
			$.fn.typeahead = function(method, value) {
				assert.equal(method, 'val')
				assert.equal(value, 'test')
				done()
			}
			search.setValue("test")
		})
	})

	describe('getElement', function() {
		it('must return the right element', function() {
			search = new StreamrSearch($("#search"), [])
			assert(search.getElement().is("#search"))
		})
	})

	describe('redrawMenu', function() {
		it('must do nothing if inBody == false', function() {
			search = new StreamrSearch($("#search"), [], {inBody: false})
			search.streamrSearchMenu.offset = function() {
				assert(false)
			}
			search.redrawMenu()
		})
		it('must call offset() with right params', function(done) {
			$("#search").offset({
				top: 100,
				left: 100
			})
			search = new StreamrSearch($("#search"), [], {inBody: true})
			search.el.offset = function() {
				return {
					top: 10,
					left: 10
				}
			}
			search.el.outerHeight = function() {
				return 10
			}
			search.streamrSearchMenu.offset = function(obj) {
				assert.deepEqual({
					top: 23,
					left: 10
				}, obj)
				done()
			}
			search.redrawMenu()
		})
	})

})