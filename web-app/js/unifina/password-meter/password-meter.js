function PasswordMeter(passwordId, minLength, userInputFunc) {
	var pwd_classes = ["veryweak", "weak", "medium", "strong", "strong"]
	var pwd_names = ["Please use a stronger password", "Barely acceptable", "Medium", "Strong", "Strong"]

	var $input = $("#"+passwordId)

	// Some CSS mods to make sure the meter and text become visible
	$input.parent().css("z-index", 0)
	$input.parent().css("position","relative")

	if ($input.parent().prev(".form-group").length)
		$input.parent().css("margin-top", "25px")

	$input.css("background-color", "transparent")

	// Create and insert the meter elements into the dom
	var $wrapper = $('<div class="strength_meter"/>')
	var $meter = $('<div class="input-lg"/>')
	var $text = $('<p/>')
	var $strength = $('<input type="hidden" name="pwdStrength" id="pwdStrength" value="0"/>')

	$wrapper.append($meter)
	$meter.append($text)
	$input.after($strength).after($wrapper)

	$input.keyup(function() {
		if (zxcvbn) {
			var password = $(this).val()
			var userInputs = (userInputFunc ? userInputFunc() : [])
			var result = zxcvbn(password, userInputs)
			var score = (password.length < minLength ? 0 : result.score)
			var text = (password.length < minLength ? "Too short" : pwd_names[score])
			var cls = pwd_classes[score]

			pwd_classes.forEach(function(it) {
				$text.removeClass(it)
				$meter.removeClass(it)
			})

			$text.text(text).addClass(cls)
			$meter.addClass(cls)
			$strength.val(score)
		}
	});
}
