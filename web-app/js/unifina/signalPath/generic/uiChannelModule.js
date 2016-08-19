SignalPath.UIChannelModule = function(data,canvas,prot) {
    prot = prot || {};
    var pub = SignalPath.GenericModule(data,canvas,prot)

    var sub

    prot.subscribe = function() {
        if (SignalPath.isRunning()) {
            sub = SignalPath.getConnection().subscribe(
                prot.jsonData.uiChannel.id,
                prot.receiveResponse,
                $.extend({}, prot.getUIChannelOptions(), {canvas: SignalPath.getId()})
            )
        }
    }

    prot.unsubscribe = function() {
        if (sub) {
            SignalPath.getConnection().unsubscribe(sub)
            sub = undefined
        }
    }

    prot.getUIChannelOptions = function() {
        // Check if module options contain channel options
        if (prot.jsonData.options && prot.jsonData.options.uiResendAll && prot.jsonData.options.uiResendAll.value) {
            return { resend_all: true }
        }
        else if (prot.jsonData.options && prot.jsonData.options.uiResendLast) {
            return { resend_last: prot.jsonData.options.uiResendLast.value }
        }
        else return { resend_all: true }
    }

    prot.receiveResponse = function() {}

    $(SignalPath).on('started loaded', prot.subscribe)
    $(SignalPath).on('stopped', prot.unsubscribe)
    $(prot).on('closed', function() {
        prot.unsubscribe()
        $(SignalPath).off('started loaded', prot.subscribe)
        $(SignalPath).off('stopped', prot.unsubscribe)
    })

    return pub;
}
