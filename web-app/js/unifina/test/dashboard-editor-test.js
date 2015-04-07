var assert = require('assert')
var $ = require('jquery')(require("jsdom").jsdom().parentWindow);
var _ = require('../../underscore/underscore')
var Backbone = require('../../backbone/backbone')

global.$ = $
global._ = _
global.Backbone = Backbone

var db = require('../dashboard/dashboard-editor')

describe('dashboard-editor', function() {

	before(function() {
	})

	beforeEach(function() {

	})

	describe("Dashboard", function() {

		var dashboard
		var dashboardJson = {
			id: 1,
			title: "Test",
			items: [
				{id: 1, title: "Item1", uiChannel: {id:'uiChannel-id-1', module: {id:1}}},
				{id: 2, title: "Item1", uiChannel: {id:'uiChannel-id-2', module: {id:2}}}
			]
		}

		beforeEach(function() {
			dashboard = new Dashboard(dashboardJson)
		})

		it('should return the same JSON representation it was initialized with', function() {
			assert.deepEqual(dashboard.toJSON(), dashboardJson)
		})


	})



})