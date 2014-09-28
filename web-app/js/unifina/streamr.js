// the Streamr global object is initialized in _layoutHead.gsp.

Streamr.createLink = function(optsOrController, action) {
	opts = optsOrController

	if (action) {
		opts = {
			controller: optsOrController,
			action: action
		}
	}

	if (opts.uri)
		return Streamr.projectWebroot + opts.uri

	var ctrl = opts.controller[0].toLowerCase() + opts.controller.slice(1)
	var url = Streamr.projectWebroot + ctrl

	if (opts.action)
		url += '/' + opts.action

	return url
}

