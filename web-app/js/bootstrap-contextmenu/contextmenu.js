(function ($, window) {
	$.fn.contextMenu = function(options) {
		var _showing = false
		var $menu = $(options.selector)

		function hide() {
			$menu.hide()
			_showing = false
		}

		$(this).on('contextmenu', function (e) {
			_showing = true
			var $target = $(e.target)

			$menu.data('target', $target)

			if (options.before($menu, $target) === false)
				return;

			$menu
				.show()
				.css({
					position: 'absolute',
					left: getLeftLocation(e),
					top: getTopLocation(e)
				})
				.off('click')

			$('li a', $menu)
				.on('click', function(clickEvent) {
					hide()
					options.onSelected($menu, $(clickEvent.target))
					clickEvent.stopPropagation()
					clickEvent.preventDefault()
					return false
				})

			return false;
		}) 

		$(document).click(hide)

		function getLeftLocation(e) {
			var mouseWidth = e.pageX
			var pageWidth = $(window).width()
			var menuWidth = $menu.width()

			if (mouseWidth + menuWidth > pageWidth &&
				menuWidth < mouseWidth) {
					return mouseWidth - menuWidth;
			}

			return mouseWidth;
		}

		function getTopLocation(e) {
			var mouseHeight = e.pageY
			var pageHeight = $(window).height()
			var menuHeight = $menu.height()

			if (mouseHeight + menuHeight > pageHeight &&
				menuHeight < mouseHeight) {
				return mouseHeight - menuHeight;
			}

			return mouseHeight;
		}
	}
})(jQuery, window)
