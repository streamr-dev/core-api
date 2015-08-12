// the Streamr global object is normally initialized in _layoutHead.gsp.
if (!Streamr)
	var Streamr = {}

Streamr.getResourceUrl = function(dir, file, absolute, cb) {
	$.ajax({
		url: Streamr.createLink('resource', 'index'),
		data: {
			dir: dir,
			file: file,
			absolute: absolute,
			dataType: 'text'
		},
		success: function(url) {
			cb(url)
		},
		error: function(xhr, status, error) {
			console.log("getResourceUrl: error fetching url for "+dir+"/"+file+": "+error)
		}
	})
}

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

