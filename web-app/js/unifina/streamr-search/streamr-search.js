
function StreamrSearch(el, modules, options, select) {
    var _this = this
    this.el = $(el)
    var options = $.extend(true, {
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
        }
    }, options)
    this.streamrSearchMenu = $("<div/>", {
        class: 'streamr-search-menu streamr-search-empty',
        id: "streamr-search-menu-" + Date.now()
    })
    if(options.inBody) {
        $("body").append(this.streamrSearchMenu)
    } else {
        var container = $("<div/>", {
            class: "twitter-typeahead"
        })
        container.insertAfter(this.el)
        container.append(this.el)
        container.append(this.streamrSearchMenu)
    }
    options.menu = this.streamrSearchMenu
    var args = []
    // Typeahead needs the options first
    args.push(options)
    var moduleNameToFunction = {
        "module": _ModuleSearchModule,
        "stream": _StreamModuleSearch
    }
    modules.forEach(function(mod) {
        var moduleFunction = moduleNameToFunction[mod.name]
        if(moduleFunction) {
            args.push(moduleFunction(mod.limit))
        }
    })
    this.el.typeahead.apply(this.el, args)
    this.redrawMenu()
    this.el.on('typeahead:selected', function(e, item) {
        select(item)
    })
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

StreamrSearch.prototype.redrawMenu = function() {
    var _this = this
    if(this.streamrSearchMenu) {
        this.streamrSearchMenu.offset({
            top: _this.el.offset().top + _this.el.outerHeight(),
            left: _this.el.offset().left
        })
    }
}

var _ModuleSearchModule = function(limit){
    var modules
    $.get(Streamr.createLink({ uri: "api/v1/modules" }), function(ds) {
        modules = ds
    })
    var getSortScore = function(module, term){
        var name = module.name.toLowerCase()
        term = term.toLowerCase()
        if(name === term)
            return 0
        else if(name.indexOf(term) === 0)
            return 1
        else
            return 2
    }
    return {
        name: 'Modules',
        displayKey: 'name',
        source: function(q, sync, async) {
            var re = new RegExp(q, 'i')
            if(modules !== undefined && modules.length) {
                var matches = modules.filter(function (mod) {
                    return re.test(mod.name) || re.test(mod.alternativeNames)
                })
                matches.sort(function (a, b) {
                    return (getSortScore(a, q) - getSortScore(b, q))
                })
                sync(matches.slice(0, limit ? limit : 5))
            }
        },
        templates: {
            header: '<p class="streamr-search-dataset-header">Modules</p>',
            suggestion: function(module) {
                return "<div><p class='streamr-search-suggestion-name'>" + module.name + "</p></div>"
            }
        }
    }
}

var _StreamModuleSearch = function (limit) {
    return {
        name: 'Streams',
        displayKey: 'name',
        source: function (q, sync, async) {
            $.get(Streamr.createLink({uri: "api/v1/streams"}) + '?term='+q, function(result) {
                async(result.slice(0, limit ? limit : 5))
            })
        },
        templates: {
            header: '<p class="streamr-search-dataset-header">Streams</p>',
            suggestion: function(stream) {
                var el = "<div><p class='streamr-search-suggestion-name'>" + stream.name + "</p>"
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