(function(exports) {

    var Module = Backbone.AssociatedModel.extend({
        toggle: function() {
            this.set('checked', !this.get('checked'))
        }
    })

    var ModuleList = Backbone.Collection.extend({
        model: Module
    })

    var ModuleView = Backbone.View.extend({
        tagName: 'li',
        className: 'module',
        template: _.template($('#module-template').html()),

        events: {
            'click .module-title': 'toggleChecked'
        },

        initialize: function() {
            if (this.model.get('checked')) {
                this.$el.addClass('checked')
            }
            this.listenTo(this.model, 'change:checked', this.render)
        },

        render: function() {
            this.$el.html(this.template(this.model.toJSON()))
            if (this.model.get('checked')) {
                this.$el.addClass('checked')
            } else {
                this.$el.removeClass('checked')
            }
            return this
        },

        toggleChecked: function() {
            this.model.toggle()
            if (this.model.get('checked')) {
                this.$el.trigger('checked', [this.model])
            } else {
                this.$el.trigger('unchecked', [this.model])
            }
        }
    })

    var Canvas = Backbone.AssociatedModel.extend({
        relations: [
            {
                type: Backbone.Many,
                key: 'modules',
                collectionType: ModuleList,
                relatedModel: Module
            }
        ],

        getChecked: function() {
            return _.filter(this.get('modules').models, function(module) {
                return module.get('checked')
            })
        }
    })

    var CanvasList = Backbone.Collection.extend({
        model: Canvas
    })

    var CanvasView = Backbone.View.extend({
        tagName: 'li',
        className: 'canvas mm-dropdown mm-dropdown-root',
        template: _.template($('#canvas-template')
            .html()),

        events: {
            'click .canvas-title': 'openClose'
        },

        initialize: function() {
            this.model.get('modules')
                .on('change:checked', function() {
                    this.render()
                }, this)
            this.render()
        },

        render: function() {
            if (!this.$el.children().length) {
                this.$el.append(this.template(this.model.toJSON()))
                this.$el.append(this.renderModules())
            }

            if (this.model.getChecked().length) {
                this.$el.find('.howmanychecked')
                    .html(this.model.getChecked().length)
            } else {
                this.$el.find('.howmanychecked')
                    .empty()
            }

            if (this.model.get('state') == 'stopped') {
                this.$el.addClass('stopped')
            }
            return this
        },

        renderModules: function() {
            var list = $('<ul/>', {
                class: 'mmc-dropdown-delay animated fadeInLeft'
            })

            var filteredModules = _.filter(this.model.get('modules').models, function(module) {
                return module.get('uiChannel') && module.get('uiChannel').webcomponent
            })

            _.each(filteredModules, function(module) {
                var moduleView = new ModuleView({
                    model: module
                })
                list.append(moduleView.render().el)
            }, this)

            return list
        },

        openClose: function() {
            this.$el.toggleClass('open')
        }
    })

    var DashboardItem = Backbone.AssociatedModel.extend({
        makeSmall: function() {
            if (this.get('size') != 'small') {
                this.set('size', 'small')
            }
        },

        makeMedium: function() {
            if (this.get('size') != 'medium') {
                this.set('size', 'medium')
            }
        },

        makeLarge: function() {
            if (this.get('size') != 'large') {
                this.set('size', 'large')
            }
        }
    })

    var DashboardItemList = Backbone.Collection.extend({
        model: DashboardItem
    })

    var DashboardItemView = Backbone.View.extend({
        tagName: 'li',
        className: 'dashboarditem',
        template: _.template($('#streamr-widget-template')
            .html()),
        titlebarTemplate: _.template($('#titlebar-template')
            .html()),

        events: {
            'click .delete-btn': 'deleteDashBoardItem',
            'click .titlebar-clickable': 'toggleEdit',
            'click .edit-btn': 'toggleEdit',
            'click .close-edit': 'toggleEdit',
            'keypress .name-input': 'toggleEdit',
            'blur .name-input': 'toggleEdit',
            'click .make-small-btn': 'changeSize',
            'click .make-medium-btn': 'changeSize',
            'click .make-large-btn': 'changeSize'
        },

        initialize: function(options) {
            this.baseUrl = options.baseUrl
        },

        render: function() {
            var _this = this

            this.smallClass = 'small-size col-xs-12 col-sm-6 col-md-4 col-lg-3 col-centered'
            this.mediumClass = 'medium-size col-xs-12 col-sm-12 col-md-8 col-lg-6 col-centered'
            this.largeClass = 'large-size col-xs-12 col-centered'

            var webcomponent = this.model.get('webcomponent')
            this.$el.html(this.template(this.model.toJSON()))
            if (webcomponent == 'streamr-label' ||
                webcomponent == 'streamr-button' ||
                webcomponent == 'streamr-switcher') {
                if (!this.model.get('size')) {
                    this.model.set('size', 'small')
                }
            } else {
                if (!this.model.get('size')) {
                    this.model.set('size', 'medium')
                }
            }
            if (webcomponent !== undefined) {
                var templateName = '#' + webcomponent + '-template'
                var template = _.template($(templateName).html())
                // Can't use the dashboard item id in the url because it might be unsaved
                var url = this.baseUrl + 'api/v1/canvases/' + this.model.get('canvas') + '/modules/' + this.model.get('module')
                this.$el.find('.widget-content')
                    .append(template({
                        url: url
                    }))
            } else {
                throw 'No webcomponent defined for uiChannel ' + this.model.get('uiChannel').id + '!'
            }

            var titlebar = this.titlebarTemplate(this.model.toJSON())
            this.$el.find('.title')
                .append(titlebar)
            this.initSize()
            this.$el.find('.make-' + this.model.get('size') + '-btn')
                .parent()
                .addClass('checked')

            // Pass error events from webcomponents onto the model
            this.$el.find('.streamr-widget')
                .on('error', function(e) {
                    _this.model.trigger('error', e.originalEvent.detail.message, _this.model.get('title'))
                })

            this.streamrWidget = this.$el.find('.streamr-widget')

            this.bindEvents()

            return this
        },

        deleteDashBoardItem: function() {
            this.model.collection.remove(this.model)
        },

        bindEvents: function() {
            var _this = this
            this.listenTo(this.model, 'resize', function() {
                _this.streamrWidget[0].dispatchEvent(new Event('resize'))
            })
            this.$el.on('drop', function(e, index) {
                _this.model.collection.trigger('orderchange')
            })
        },

        remove: function() {
            this.streamrWidget[0].dispatchEvent(new Event('remove'))
            Backbone.View.prototype.remove.apply(this)
        },

        toggleEdit: function(e) {
            var _this = this
            this.editOff = function() {
                if (this.$el.hasClass('editing')) {
                    this.model.set('title', this.$el.find('.name-input').val())
                    this.$el.find('.titlebar').text(this.model.get('title'))
                    this.$el.find('.titlebar-clickable').text(this.model.get('title'))
                    this.$el.removeClass('editing')
                }
            }
            this.editOn = function() {
                if (!(this.$el.hasClass("editing"))) {
                    this.$el.addClass("editing")
                    setTimeout(function() {
                        _this.$el.find('.name-input').focus()
                    }, 0)
                }
            }
            if (e.type == 'click') {
                if ($(e.currentTarget)
                        .hasClass('edit-btn') || $(e.currentTarget)
                        .hasClass('titlebar-clickable')) {
                    _this.editOn()
                } else if ($(e.currentTarget).hasClass('close-edit')) {
                    _this.editOff()
                }
            } else if (e.type == 'focusout' || e.type == 'blur') { //='blur'
                this.editOff()
            } else if (e.keyCode == 13) {
                this.editOff()
            }

        },

        initSize: function() {
            this.$el.removeClass(this.smallClass + ' ' + this.mediumClass + ' ' + this.largeClass)
            var size = this.model.get('size')
            if (size == 'small' || size == 'medium' || size == 'large') {
                if (size == 'small') {
                    this.$el.addClass(this.smallClass)
                } else if (size == 'medium') {
                    this.$el.addClass(this.mediumClass)
                } else if (size == 'large') {
                    this.$el.addClass(this.largeClass)
                }
            } else {
                throw new Error('Module size not found')
            }
        },

        changeSize: function(e) {
            this.$el.find('.make-' + this.model.get('size') + '-btn')
                .parent()
                .removeClass('checked')
            if ($(e.target).hasClass('make-small-btn')) {
                this.model.makeSmall()
            } else if ($(e.target).hasClass('make-medium-btn')) {
                this.model.makeMedium()
            } else if ($(e.target).hasClass('make-large-btn')) {
                this.model.makeLarge()
            }
            this.initSize()
            this.$el.find('.make-' + this.model.get('size') + '-btn')
                .parent()
                .addClass('checked')
            this.$el.find('.widget-content')
                .children()[0]
                .dispatchEvent(new Event('resize'))
        }
    })

    var SidebarView = Backbone.View.extend({
        template: _.template($('#sidebar-template')
            .html()),

        events: {
            'checked': 'syncDashboardItems',
            'unchecked': 'syncDashboardItems',
            'click .save-button': 'save'
        },

        initialize: function(options) {
            var _this = this

            this.el = options.el

            var requiredOptions = ['dashboard', 'menuToggle', 'canvases', 'baseUrl']

            requiredOptions.forEach(function(requiredOption) {
                if (options[requiredOption] === undefined) {
                    throw 'Required option is missing: ' + requiredOption
                }
            })

            this.dashboard = options.dashboard
            this.menuToggle = options.menuToggle
            this.baseUrl = options.baseUrl
            this.canvases = new CanvasList(options.canvases)
            this.syncCheckedState()

            this.listenTo(this.dashboard.get('items'), 'remove', function(item) {
                this.syncCheckedState()
            })

            this.dashboard.get('items')
                .on('change', function() {
                    _this.dashboard.saved = false
                })
            this.menuToggle.click(function() {
                if ($('body').hasClass('editing')) {
                    _this.setEditMode(false)
                } else {
                    _this.setEditMode(true)
                }
            })

            if (this.dashboard.get('items').models.length) {
                this.setEditMode(options.edit)
            } else {
                this.setEditMode(true)
            }

            this.$el.find(".dashboard-name")
                .change(function() {
                    _this.dashboard.saved = false
                })

            this.dashboard.on('invalid', function() {
                Streamr.showError(_this.dashboard.validationError, 'Invalid value')
            })

            this.render()
        },

        // Synchronize the "checked" state of sidebar modules from the DashboardItemList
        syncCheckedState: function() {
            var checked = {}

            _.each(this.dashboard.get('items').models, function(item) {
                if (checked[item.get('canvas')] === undefined) {
                    checked[item.get('canvas')] = {}
                }

                checked[item.get('canvas')][item.get('module')] = true
            })
            _.each(this.canvases.models, function(canvas) {
                _.each(canvas.get('modules').models, function(module) {
                    module.set('checked', module.get('uiChannel') && checked[canvas.get('id')] && checked[canvas.get('id')][module.get('hash')])
                })
            })
        },

        render: function() {
            var _this = this
            this.$el.append(this.template(this.dashboard.toJSON()))
            this.titleInput = this.$el.find("input.dashboard-name.title-input")
            this.list = this.$el.find("#rsp-list")
            this.saveButton = this.$el.find("#saveButton")
            this.shareButton = $(".share-button")
            this.deleteButton = this.$el.find("#deleteDashboardButton")
            _.each(this.canvases.models, function(canvas) {
                var canvasView = new CanvasView({
                    model: canvas
                })
                this.list.append(canvasView.el)
            }, this)
            new ConfirmButton(this.$el.find("#deleteDashboardButton"), {
                title: "Are you sure?",
                message: "Really delete dashboard " + _this.dashboard.get("name") + "?"
            }, function(response) {
                if (response) {
                    Backbone.sync("delete", _this.dashboard, {
                        success: function() {
                            // we dont want to accept exiting the page when we have just removed the whole dashboard
                            $(window)
                                .off("beforeunload")
                            Streamr.showSuccess("Dashboard deleted succesfully!", "", 2000)
                            setTimeout(function() {
                                window.location = _this.baseUrl + "dashboard/list"
                            }, 2000)
                        },
                        error: function(a, b, c) {
                            Streamr.showError()
                        }
                    })
                }
            })
            this.checkPermissions()
            this.trigger('ready')
        },

        checkPermissions: function() {
            var _this = this
            if (this.dashboard && this.dashboard.id) {
                $.getJSON(this.baseUrl + "api/v1/dashboards/" + this.dashboard.id + "/permissions/me", callback)
            } else {
                callback([])
            }
            function callback(permissions) {
                permissions = _.map(permissions, function(p) {
                    return p.operation
                })
                if (_.contains(permissions, "share")) {
                    _this.shareButton.removeAttr("disabled")
                    _this.shareButton.data('url', Streamr.createLink({uri: "api/v1/dashboards/" + _this.dashboard.get('id')}))
                    _this.shareButton.data('name', _this.dashboard.get('name'))
                } else {
                    _this.shareButton.addClass("forbidden")
                }
                if (_.contains(permissions, "write")) {
                    _this.saveButton.removeAttr("disabled")
                    _this.deleteButton.removeAttr("disabled")
                } else {
                    _this.saveButton.addClass("forbidden")
                    _this.deleteButton.addClass("forbidden")
                }
            }
        },

        setEditMode: function(active) {
            if (active || active === undefined && !$('body').hasClass('mmc')) {
                $('body').addClass('mme')
                $('body').removeClass('mmc')
                $('body').addClass('editing')
            } else {
                $('body').addClass('mmc')
                $('body').removeClass('mme')
                $('body').removeClass('editing')
            }

            $('body').trigger('classChange')

            _.each(this.dashboard.get('items').models, function(item) {
                item.trigger('resize')
            }, this)
        },

        // When modules become checked or unchecked, sync the dashboard items
        syncDashboardItems: function(event, module) {
            var canvas = module.collection.parents[0]

            if (event.type == 'checked') {
                this.dashboard.get('items')
                    .add({
                        dashboard: this.dashboard.get('id'),
                        title: module.get('uiChannel').name,
                        canvas: canvas.get('id'),
                        module: module.get('hash'),
                        webcomponent: module.get('uiChannel').webcomponent
                    })
            }
            if (event.type == 'unchecked') {
                var list = _.filter(this.dashboard.get('items').models, function(item) {
                    return item.get('canvas') === canvas.get('id') && item.get('module') === module.get('hash')
                }, this)
                this.dashboard.get('items')
                    .remove(list)
            }
        },

        save: function() {
            var _this = this

            this.dashboard.save({}, {
                success: function() {
                    _this.dashboard.trigger('saved')
                    _this.dashboard.saved = true
                    document.title = _this.dashboard.get('name')
                    Streamr.showSuccess('Dashboard ' + _this.dashboard.get('name') + ' saved successfully', 'Saved!')
                    window.history.replaceState({}, undefined, _this.baseUrl + 'dashboard/editor/' + _this.dashboard.get('id'))
                },
                error: function(model, response) {
                    Streamr.showError(response.responseText, 'Error while saving')
                },
                // save() returns the complete model with id, and
                // Backbone wants to re-render the view. Since we don't need that, we don't want any events to be triggered.
                silent: true
            })
        }
    })

    var Dashboard = Backbone.AssociatedModel.extend({
        relations: [
            {
                type: Backbone.Many,
                key: 'items',
                collectionType: DashboardItemList,
                relatedModel: DashboardItem
            }
        ],

        validate: function() {
            if (!this.get('name')) {
                return 'Dashboard name can\'t be empty'
            }
        },

        initialize: function() {
            var _this = this
            this.saved = true
            this.listenTo(this, 'change', function() {
                _this.saved = false
            })
        }
    })

    var DashboardView = Backbone.View.extend({

        initialize: function(options) {
            var _this = this

            var requiredOptions = ['baseUrl']

            requiredOptions.forEach(function(requiredOption) {
                if (options[requiredOption] === undefined) {
                    throw 'Required option is missing: ' + requiredOption
                }
            })
            this.baseUrl = options.baseUrl

            this.dashboardItemViews = []
            // Avoid needing jquery ui in tests
            if (this.$el.sortable) {
                this.$el.sortable({
                    cancel: '.non-draggable, .titlebar-edit, .panel-heading-controls',
                    items: '.dashboarditem',
                    stop: function(event, ui) {
                        ui.item.trigger('drop')
                    }
                })
                this.$el.droppable()
            }
            _.each(this.model.get('items').models, this.addDashboardItem, this)

            this.model.get('items').on('add', this.addDashboardItem, this)
            this.model.get('items').on('add', this.updateOrders, this)
            this.model.get('items').on('remove', this.removeDashboardItem, this)
            this.model.get('items').on('remove', this.updateOrders, this)
            this.model.get('items').on('orderchange', this.updateOrders, this)

            // Pass errors on items onto this view
            this.model.get('items').on('error', function(error, itemTitle) {
                _this.trigger('error', error, itemTitle)
            })

            $('body').on('classChange', function() {
                //This is called after the classChange
                //editing -> !editing
                if (!($('body').hasClass('editing'))) {
                    if (!_this.model.saved) {
                        Streamr.showInfo('The dashboard has changes which are not saved', 'Not saved')
                    }
                    _this.disableSortable()
                    //!editing -> editing
                } else {
                    _this.enableSortable()
                }
            })
        },

        addDashboardItem: function(model) {
            var DIView = new DashboardItemView({
                model: model,
                baseUrl: this.baseUrl
            })
            this.$el.append(DIView.render().el)
            this.dashboardItemViews.push(DIView)
        },

        removeDashboardItem: function(model) {
            var viewsToRemove = _.filter(this.dashboardItemViews, function(DIView) {
                return DIView.model === model
            })
            viewsToRemove.forEach(function(view) {
                view.remove()
            })
            this.dashboardItemViews = _.without(this.dashboardItemViews, viewsToRemove)
        },

        updateOrders: function() {
            _.each(this.dashboardItemViews, function(item) {
                item.model.set('ord', item.$el.index())
            }, this)
        },

        enableSortable: function() {
            if (this.$el.sortable) {
                this.$el.sortable('option', 'disabled', false)
            }
        },

        disableSortable: function() {
            if (this.$el.sortable) {
                this.$el.sortable('option', 'disabled', true)
            }
        }
    })

    exports.Dashboard = Dashboard
    exports.DashboardView = DashboardView
    exports.SidebarView = SidebarView

})(typeof (exports) !== 'undefined' ? exports : window)