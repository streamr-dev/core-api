/**
 * Events triggered on the button:
 * start-confirmed
 * stop-confirmed
 */
var CanvasStartButton = Backbone.View.extend({
    initialize: function(options) {
        var _this = this

        this.signalPath = options.signalPath
        this.settings = options.settings
        this.startContent = options.startContent || "Start"
        this.stopContent = options.stopContent || "Stop"
        this.adhoc = options.adhoc || false
        this.clearState = options.clearState || false
        this.$clickElement = options.clickElement || this.$el

        $(this.signalPath).on('loaded', function() {
            if (!_this.adhoc)
                _this.setRunning(_this.signalPath.isRunning())
        })

        $(this.signalPath).on('error stopped stopping', function() {
            _this.setRunning(false);
        })

        $(this.signalPath).on('starting', function() {
            _this.setRunning(true)
        })

        this.$clickElement.click(function() {
            _this.startOrStop()
        })
    },

    startOrStop: function() {
        if (this.running) {
            this.stop()
        }
        else {
            this.start()
        }
    },

    start: function() {
        var _this = this

        var callback = function() {
            _this.trigger('start-confirmed')
        }

        if (this.adhoc) {
            this.signalPath.startAdhoc(callback)
        }
        else {
            this.signalPath.start({
                clearState: this.clearState
            }, callback)
        }
    },

    stop: function() {
        var _this = this

        var callback = function() {
            _this.trigger('stop-confirmed')
        }

        if (this.adhoc) {
            this.signalPath.stop(callback)
        }
        else {
            // Ask for confirmation
            bootbox.confirm({
                message: "Are you sure you want to stop canvas "+this.signalPath.getName()+"?",
                callback: function(result) {
                    if (result) {
                        _this.signalPath.stop(callback)
                    }
                },
                className: "stop-confirmation-dialog bootbox-sm"
            });
        }
    },

    setRunning: function(running) {
        if (running) {
            this.running = true
            this.$el.html(this.stopContent)
        }
        else {
            this.running = false
            this.$el.html(this.startContent)
        }
    }
})
