
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
	var emptyTemplate = 'No streams or modules found.'

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
			header: '<span class="tt-dataset-header">Modules</span>',
			suggestion: function(item) {
				return "<p><span class='tt-suggestion-name'>"+item.name+"</span></p>"
			}
		}
	}, {
		name: 'streams',
		displayKey: 'name',
		source: streamsTypeAhead,
		templates: {
			header: '<span class="tt-dataset-header">Streams</span>',
			suggestion: function(item) {
				if (item.description)
					return"<p><span class='tt-suggestion-name'>"+item.name+"</span><br><span class='tt-suggestion-description'>"+item.description+"</span></p>" 
				else return "<p><span class='tt-suggestion-name'>"+item.name+"</span></p>"
			}
		}
	})

	$elem.on('typeahead:selected', function(e, mod) {
		$elem.typeahead('val', '')

		if (mod.module) { // is stream, specifies module
			SignalPath.addModule(mod.module, { params: [{ name: 'stream', value: mod.id }] })
		} else { // is module
			SignalPath.addModule(mod.id, {})
		}
	})
}
