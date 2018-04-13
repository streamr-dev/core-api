/*global Streamr Backbone bootbox $ */

/**
 * Events triggered on the button:
 * start-confirmed
 * start-error
 * stop-confirmed
 * stop-error
 */
var CanvasStartButton = Backbone.View.extend({
    initialize: function(options) {
        var _this = this

        this.signalPath = options.signalPath
        this.settings = options.settings
        this.startContent = options.startContent || 'Start'
        this.stopContent = options.stopContent || 'Stop'
        this.adhoc = options.adhoc || false
        this.clearState = options.clearState || false
        this.$clickElement = options.clickElement || this.$el

        $(this.signalPath).on('loaded', function() {
            if (!_this.adhoc) {
                _this.setRunning(_this.signalPath.isRunning())
            }
        })

        $(this.signalPath).on('new error stopped stopping', function() {
            _this.setRunning(_this.signalPath.isRunning())
        })

        $(this.signalPath).on('starting started', function() {
            _this.setRunning(true)
        })

        this.$clickElement.click(function() {
            _this.startOrStop()
        })
    },

    startOrStop: function() {
        if (this.running) {
            this.stop()
        } else {
            this.start()
        }
    },

    start: function() {
        var _this = this

        var callback = function(response, err) {
            if (err) {
                _this.trigger('start-error', err)
            } else {
                _this.trigger('start-confirmed')
            }
        }

        if (this.adhoc) {
            this.signalPath.startAdhoc({}, callback)
        } else {
            var doStart = function() {
                _this.signalPath.start({
                    clearState: _this.clearState
                }, callback)
            }

            if (this.signalPath.isDirty()) {
                // Ask for confirmation
                bootbox.confirm({
                    message: 'Canvas needs to be saved before starting.',
                    callback: function(confirmed) {
                        if (confirmed) {
                            // Let's save or save as
                            if (_this.signalPath.isSaved()) {
                                _this.signalPath.save(doStart)
                            } else {
                                _this.signalPath.saveAs(_this.signalPath.getName(), doStart)
                            }
                        }
                    },
                    className: 'save-on-start-confirmation-dialog bootbox-sm',
                    buttons: {
                        'cancel': {
                            label: 'Cancel',
                            className: 'btn-default'
                        },
                        'confirm': {
                            label: 'Save & Start',
                            className: 'btn-primary'
                        }
                    },
                })
            } else {
                doStart()
            }

        }
    },

    stop: function() {
        var _this = this

        var callback = function(response, err) {
            if (err) {
                _this.trigger('stop-error', err)
            } else {
                _this.trigger('stop-confirmed')
            }
        }

        if (this.adhoc) {
            this.signalPath.stop(callback)
        } else {
            // Ask for confirmation
            bootbox.confirm({
                message: 'Are you sure you want to stop canvas '+Streamr.escape(this.signalPath.getName())+'?',
                callback: function(confirmed) {
                    if (confirmed) {
                        _this.signalPath.stop(callback)
                    }
                },
                className: 'stop-confirmation-dialog bootbox-sm'
            })
        }
    },

    setRunning: function(running) {
        if (running) {
            this.running = true
            this.$el.html(this.stopContent)
        } else {
            this.running = false
            this.$el.html(this.startContent)
        }
    }
})
