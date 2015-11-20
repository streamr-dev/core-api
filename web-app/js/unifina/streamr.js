// the Streamr global object is normally initialized in _layoutHead.gsp.
if (!Streamr)
	var Streamr = {}

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

Streamr.showError = function(msg, title) {
	title = title || "Error"
	$.pnotify({
		type: 'error',
		title: title,
		text: msg,
		delay: 4000
	})
}

Streamr.showInfo = function(msg, title) {
	$.pnotify({
		type: 'info',
		title: title,
		text: msg,
		delay: 4000
	})
}

Streamr.showSuccess = function(msg, title) {
	$.pnotify({
		type: 'success',
		title: title,
		text: msg,
		delay: 4000
	})
}
