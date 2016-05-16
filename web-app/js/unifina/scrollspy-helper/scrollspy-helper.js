(function(exports) {

function ScrollSpyHelper(helpTreeEl, sidebarEl, offset) {
	var _this = this

	this.helpTreeEl = $(helpTreeEl)
	this.sidebarEl = $(sidebarEl)

	this.sidebar = $("<nav/>", {
		class: 'streamr-sidebar'
	})
	this.nav = $("<ul/>", {
		class: 'nav'
	})
	this.sidebar.append(this.nav)
	this.sidebarEl.append(this.sidebar)

	var lastLi
	$.each(this.helpTreeEl.find("h1,h2"), function(i, el){
		var text = $(el).text()
		var id = text.toLowerCase().replace(/\W/g, '')
		var href = "#" + id

		if ($(el).is("h1")) {
			var li = $("<li/>")
			var a = $("<a/>", {
				href: href,
				text: text
			})
			li.ul = $("<ul/>", {
				class: "nav"
			})
			li.append(a)
			li.append(li.ul)
			_this.nav.append(li)
			lastLi = li
			$(el).attr("id", id)
		} else if ($(el).is("h2")) {
			var li = $("<li/>")
			var a = $("<a/>", {
				href: href,
				text: text
			})
			li.append(a)
			lastLi.ul.append(li)
			$(el).attr("id", id)
		}
	})

	var offset = offset ? offset : 80
	$('body').scrollspy({
		offset: offset
	})
	// Scrollspy's offset doesn't affect to the links so the offset correction must me written manually
	this.sidebar.find("li a").click(function(event) {
		event.preventDefault()
		if($($(this).attr('href'))[0]){
    		$($(this).attr('href'))[0].scrollIntoView()
   		scrollBy(0, -(offset-30))
		}		
    	this.blur()
	});
}

exports.ScrollSpyHelper = ScrollSpyHelper

})(typeof(exports) !== 'undefined' ? exports : window)