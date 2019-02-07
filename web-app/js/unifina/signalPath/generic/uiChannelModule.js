SignalPath.UIChannelModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    var subs = []

    /**
     * Subscribes to the given uiChannelIds (an array of strings) and with the
     * receiveResponse function as the message handler.
     */
    prot.subscribe = function(uiChannelIds) {
        uiChannelIds.forEach(function(uiChannelId) {
            subs.push(SignalPath.getConnection().subscribe(
                $.extend({}, prot.getUIChannelOptions(), {stream: uiChannelId}),
                prot.receiveResponse
            ))
        })
    }

    /**
     * Unsubscribes all active subscriptions.
     */
    prot.unsubscribe = function() {
        subs.forEach(function(sub) {
            SignalPath.getConnection().unsubscribe(sub)
        })
        subs = []
    }

    prot.getUIChannelOptions = function() {
        // Check if module options contain channel options
        if (prot.jsonData.options && prot.jsonData.options.uiResendLast) {
            return { resend_last: prot.jsonData.options.uiResendLast.value }
        }
        else return {}
    }

    /**
     * The function that handles incoming messages to this module.
     * Should be overridden in subclasses. The default implementation does nothing.
     */
    prot.receiveResponse = function() {}

    /**
     * Extracts an array of uiChannel ids from a json representation of this module.
     */
    prot.getUIChannelIdsFromJson = function(json) {
        return [json.uiChannel.id]
    }

    function onLoaded() {
        if (SignalPath.isRunning()) {
            prot.subscribe(prot.getUIChannelIdsFromJson(prot.jsonData))
        }
    }

    function onStopped() {
        prot.unsubscribe()
    }

    function onSubscribing(e, runningJson) {
        prot.subscribe(prot.getUIChannelIdsFromJson(runningJson))
    }

    function isSubscribed() {
        for (var i=0;i<subs.length;i++) {
            if (!subs[i].isSubscribed()) {
                return false
            }
        }
        return true
    }

    $(SignalPath).on('loaded', onLoaded)
    $(pub).on('stopped', onStopped)
    $(pub).on('subscribing', onSubscribing)

    $(prot).on('closed', function() {
        prot.unsubscribe()
        $(SignalPath).off('loaded', onLoaded)
        $(pub).off('stopped', onStopped)
    })

    return pub;
}
