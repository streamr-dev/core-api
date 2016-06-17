// the Streamr global object is normally initialized in _layoutHead.gsp.
if (!Streamr) {
	var Streamr = {}
}

$.pnotify.defaults.history = false
$.pnotify.defaults.styling = "fontawesome"
$.pnotify.defaults.icon = false
$.pnotify.defaults.closer = true
$.pnotify.defaults.sticker = false
$.pnotify.defaults.closer_hover = false

//Change the variable signs in underscore/lodash from <%= var %> to {{ var }}
_.templateSettings = {
	evaluate : /\{\[([\s\S]+?)\]\}/g, // {[ ]}
	escape : /\[\[([\s\S]+?)\]\]/g, // [[ ]]
	interpolate : /\{\{([\s\S]+?)\}\}/g // {{ }}
};

Streamr.createLink = function(optsOrController, action, id) {
	opts = optsOrController

	if (action) {
		opts = {
			controller: optsOrController,
			action: action,
			id: id
		}
	}

	if (opts.uri)
		return Streamr.projectWebroot + opts.uri

	var ctrl = opts.controller[0].toLowerCase() + opts.controller.slice(1)
	var url = Streamr.projectWebroot + ctrl

	if (opts.action)
		url += '/' + opts.action

	if (opts.id!==undefined) {
		url += '/' + opts.id
	}

	return url
}

Streamr.showError = function(msg, title, delay) {
	title = title || "Error"
	delay = delay || 4000
	$.pnotify({
		type: 'error',
		title: title,
		text: msg,
		delay: delay
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
		delay: delay
	})
}
