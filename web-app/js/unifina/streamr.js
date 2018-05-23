/*global $ _ */

// the Streamr global object is normally initialized in _layoutHead.gsp.
if (!Streamr) {
    var Streamr = {}
}

$.pnotify.defaults.history = false
$.pnotify.defaults.styling = 'fontawesome'
$.pnotify.defaults.icon = false
$.pnotify.defaults.closer = true
$.pnotify.defaults.sticker = false
$.pnotify.defaults.closer_hover = false

//Change the variable signs in underscore/lodash from <%= var %> to {{ var }}
_.templateSettings = {
    evaluate : /\{\[([\s\S]+?)\]\}/g, // {[ ]}
    escape : /\[\[([\s\S]+?)\]\]/g, // [[ ]]
    interpolate : /\{\{([\s\S]+?)\}\}/g // {{ }}
}

Streamr.createLink = function(optsOrController, action, id) {
    var opts = optsOrController

    if (action) {
        opts = {
            controller: optsOrController,
            action: action,
            id: id
        }
    }

    if (opts.uri) {
        return Streamr.projectWebroot + opts.uri.replace(/^\//, '')
    }

    var ctrl = opts.controller[0].toLowerCase() + opts.controller.slice(1)
    var url = Streamr.projectWebroot + ctrl

    if (opts.action) {
        url += '/' + opts.action
    }

    if (opts.id!==undefined) {
        url += '/' + opts.id
    }

    return url
}

Streamr.showError = function(msg, title, delay) {
    $.pnotify({
        type: 'error',
        title: title || 'Error',
        text: msg,
        delay: delay || 4000
    })
}

Streamr.showInfo = function(msg, title, delay) {
    delay = delay || 4000
    $.pnotify({
        type: 'info',
        title: title,
        text: msg,
        delay: delay
    })
}

Streamr.showSuccess = function(msg, title, delay) {
    delay = delay || 4000
    $.pnotify({
        type: 'success',
        title: title,
        text: msg,
        delay: delay,
    })
}

// TODO: move into a module, or even download the whole https://github.com/mathiasbynens/he
Streamr.regexEscape = /["&'<>`]/g
Streamr.escapeMap = {
    '"': '&quot;',
    '&': '&amp;',
    '\'': '&#x27;',
    '<': '&lt;',
    // See https://mathiasbynens.be/notes/ambiguous-ampersands: in HTML, the
    // following is not strictly necessary unless it’s part of a tag or an
    // unquoted attribute value. We’re only escaping it to support those
    // situations, and for XML support.
    '>': '&gt;',
    // In Internet Explorer ≤ 8, the backtick character can be used
    // to break out of (un)quoted attribute values or HTML comments.
    // See http://html5sec.org/#102, http://html5sec.org/#108, and
    // http://html5sec.org/#133.
    '`': '&#x60;'
}

Streamr.escape = function(string) {
    return string.replace(Streamr.regexEscape, function($0) {
        // Note: there is no need to check `has(escapeMap, $0)` here.
        return Streamr.escapeMap[$0]
    })
}
