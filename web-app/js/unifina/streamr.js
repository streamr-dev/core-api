// the Streamr global object is initialized in _layoutHead.gsp.

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

