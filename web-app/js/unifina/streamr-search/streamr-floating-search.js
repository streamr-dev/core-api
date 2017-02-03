
function FloatingSearchBar(parent, options, moduleClicked) {
    var _this = this
    
    this.parent = parent
    this.options = options
    this.cb = moduleClicked
    
    this.parent.dblclick(function(e) {
        if (e.target !== this)
            return;
        var x = e.offsetX,
            y = e.offsetY
        _this.show(x, y)
    })
}

FloatingSearchBar.prototype.show = function (x, y) {
    var _this = this
    this.currentX = x
    this.currentY = y
    if (this.searchBar) {
        this.searchBar.show()
    } else {
        this.searchBar = $("<div/>", {
            class: 'streamr-floating-search-bar'
        })
        this.searchInput = $("<input/>", {
            type: 'text',
            class: 'form-control streamr-search-input',
            placeholder: "Type to search"
        })
        this.searchBar.append(this.searchInput)
        this.searchInput.blur(function() {
            _this.hide()
        })
        this.parent.append(this.searchBar)
        this.streamrSearch = new StreamrSearch(this.searchInput, [{
            name: "module",
            limit: 5
        }, {
            name: "stream",
            limit: 3
        }], {
            inBody: true
        }, function(item) {
            _this.hide()
            _this.cb(item, {
                x: _this.currentX,
                y: _this.currentY
            })
        })
    }
    this.searchInput.focus()
    this.searchBar.css({
        top: y + "px",
        left: x + "px"
    })
}

FloatingSearchBar.prototype.hide = function() {
    if (this.searchBar) {
        this.searchBar.hide()
    }
}