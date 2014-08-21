
function SearchControl(streamUrl, modulesUrl, $elem) {

	function modulesTypeAhead(q, cb) {
		var re = new RegExp(q, 'i')
		var matches = modules.filter(function(mod) {
			return re.test(mod.name) || re.test(mod.alternativeNames)
		})
		cb(matches.slice(0, 5))
	}

	function streamsTypeAhead(q, cb) {
		$.get(streamUrl + '?term='+q, function(result) {
			cb(result.slice(0, 5))
		})
	}

	var modules = []

	$.get(modulesUrl, function(ds) {
		modules = ds
	})

	$(document).bind('keydown', 'alt+s', function(e) {
		$elem.focus()
		e.preventDefault()
	})

	$elem.typeahead({
		highlight: true,
		hint: false
	}, {
		name: 'modules',
		displayKey: 'name',
		source: modulesTypeAhead,
		templates: {
			header: '<strong>Modules</strong>'
		}
	}, {
		name: 'streams',
		displayKey: 'name',
		source: streamsTypeAhead,
		templates: {
			header: '<strong>Streams</strong>'
		}
	})

	$elem.on('typeahead:selected', function(e, mod) {
		$elem.val('')

		if (mod.module) { // is stream, specifies module
			SignalPath.addModule(mod.module, { params: [{ name: 'stream', value: mod.id }] })
		} else { // is module
			SignalPath.addModule(mod.id, {})
		}
	})
}
