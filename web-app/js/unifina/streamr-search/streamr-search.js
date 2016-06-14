(function(exports) {

/*
    Params:
        el - html element, jquery element or jquery selector of the input
        modules - list of modules used, has keys name and limit, where limit is the number of search results shown, e.g. [{name: "module", limit: 5}]
        options - options can contain any typeahead.js options, and fields
            'inBody' - if true, the search result menu is rendered in body, default false
            'emptyAfterSelection' - if true, the input is cleared after a item is selected
        onSelect: the callback to be executed when a search result is selected
 */
function StreamrSearch(el, modules, options, onSelected) {
    var _this = this
    this.el = $(el)
    this.options = $.extend(true, {
        highlight: true,
        hint: true,
        autocomplete: true,
        classNames: {
            input: "streamr-search-input",
            hint: "streamr-search-hint",
            menu: "streamr-search-menu",
            dataset: "streamr-search-dataset",
            suggestion: "streamr-search-suggestion",
            empty: "streamr-search-empty",
            open: "streamr-search-open",
            cursor: "streamr-search-cursor",
            highlight: "streamr-search-highlight"
        },
        inBody: false,
        emptyAfterSelection: true
    }, options)
    this.streamrSearchMenu = $("<div/>", {
        class: 'streamr-search-menu streamr-search-empty',
        id: "streamr-search-menu-" + Date.now()
    })
    if(this.options.inBody) {
        $("body").append(this.streamrSearchMenu)
    } else {
        var container = $("<div/>", {
            class: "twitter-typeahead"
        })
        container.insertAfter(this.el)
        container.append(this.el)
        container.append(this.streamrSearchMenu)
    }
    this.options.menu = this.streamrSearchMenu
    this.streamrSearchMenu.css("min-width", this.el.outerWidth())
    var args = []
    // Typeahead needs the options first
    args.push(this.options)
    var moduleNameToFunction = {
        "module": ModuleSearchModule,
        "stream": StreamSearchModule
    }
    modules.forEach(function(mod) {
        var moduleFunction = moduleNameToFunction[mod.name]
        if(moduleFunction) {
            args.push(moduleFunction(mod.limit))
        }
    })
    this.el.typeahead.apply(this.el, args)

    this.el.on('typeahead:selected', function(e, item) {
        if(_this.options.emptyAfterSelection)
            _this.setValue('')
        onSelected(item)
    })
    if (this.options.inBody) {
        this.el.on('typeahead:render', function() {
            _this.redrawMenu()
        })
    }
    // Typeahead has removed the 'autoselect: true' option, so this is a workaround for it
    this.el.keydown(function(e) {
        if(e.keyCode == 13) {
            _this.streamrSearchMenu.find(".streamr-search-suggestion").eq(0).click()
        }
    })
}

StreamrSearch.prototype.setValue = function(value) {
    this.el.typeahead('val', value)
}

StreamrSearch.prototype.getElement = function() {
    return this.el
}

StreamrSearch.prototype.redrawMenu = function() {
    var _this = this
    if(this.streamrSearchMenu && this.options.inBody) {
        var top = _this.el.offset().top + _this.el.outerHeight() + 3
        var left = _this.el.offset().left
        this.streamrSearchMenu.offset({
            top: top,
            left: left
        })
    }
}

var getSortScore = function(string, query){
    string = string.toLowerCase()
    query = query.toLowerCase()
    // If the real name of the module contains the term, the earlier the better. Else sorted last.
    if(string === query)
        return -1
    if(string.indexOf(query) >= 0)
        return string.indexOf(query)
    else return Infinity
}

var ModuleSearchModule = function(limit){
    var modules
    $.get(Streamr.createLink({ uri: "api/v1/modules" }), function(ds) {
        modules = ds
    })

    return {
        name: 'Modules',
        displayKey: 'name',
        source: function(q, sync, async) {
            var re = new RegExp(q, 'i')
            if(modules !== undefined && modules.length) {
                var matches = modules.filter(function (mod) {
                    if(re.test(mod.name) || re.test(mod.alternativeNames)) {
                        // This is set only so the result type can be checked later (e.g. "module" or "stream")
                        mod.resultType = "module"
                        return true
                    }
                    return false
                })
                matches.sort(function (a, b) {
                    return (getSortScore(a.name, q) - getSortScore(b.name, q))
                })
                sync(matches.slice(0, limit ? limit : 5))
            }
        },
        templates: {
            header: '<p class="streamr-search-dataset-header">Modules</p>',
            suggestion: function(module) {
                return "<div data-type='module'><p class='streamr-search-suggestion-name'>" + module.name + "</p></div>"
            }
        },
        limit: Infinity
    }
}

var StreamSearchModule = function (limit) {
    return {
        name: 'Streams',
        displayKey: 'name',
        source: function (q, sync, async) {
            $.get(Streamr.createLink({uri: "api/v1/streams"}) + '?' + $.param({public: true, search: q}), function(result) {
                result.sort(function (a, b) {
                    return (getSortScore(a.name, q) - getSortScore(b.name, q))
                })
                result = result.slice(0, limit ? limit : 5)
                result.forEach(function(r) {
                    // This is set only so the result type can be checked later (e.g. "module" or "stream")
                    r.resultType = "stream"
                })
                async(result)
            })
        },
        templates: {
            header: '<p class="streamr-search-dataset-header">Streams</p>',
            suggestion: function(stream) {
                var el = "<div data-type='stream'><p class='streamr-search-suggestion-name'>" + stream.name + "</p>"
                if(stream.description)
                    el += "<p class='streamr-search-suggestion-description'>"+stream.description+"</p>"
                el += "</div>"
                return el
            }
        },
        // This is needed because of a bug in typeahead
        limit: Infinity
    }
}

exports.StreamrSearch = StreamrSearch

})(typeof(exports) !== 'undefined' ? exports : window)