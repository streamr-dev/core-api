/**
 * This helper provides click & confirmation handling for button toolbars
 */

function Toolbar($elem) {
	$elem.find('.btn[data-action]').click(function(e) {
		var btn = $(this)
		var $form = ($elem.is("form") ? $elem : $elem.parent("form"))
		
		if ($form)	
			$form.attr('action', btn.data('action'))

		if (btn.hasClass('confirm')) {
			bootbox.confirm({
				message: btn.data('confirm') ? btn.data('confirm') : "Are you sure?",
				callback: function(result) {
					if (result) {
						if (!$form)
							throw "Confirm buttons need to be inside a form that gets submitted"
						$form.submit()
					}
				},
				className: "bootbox-sm"
			});
			
			// Prevent default action
			return false 
		}
		return true
	})
}
