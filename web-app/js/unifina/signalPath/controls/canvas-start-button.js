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
        if (this.adhoc) {
            SignalPath.startAdhoc()
        }
        else {
            SignalPath.start({
                clearState: this.clearState
            })
        }
    },

    stop: function() {
        SignalPath.stop()
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
