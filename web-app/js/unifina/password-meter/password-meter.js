function PasswordMeter(passwordId, meterId, hiddenInputId, minLength, userInputFunc) {
	var pwd_classes = ["veryweak", "weak", "medium", "strong", "strong"]
	var pwd_names = ["Please use a stronger password", "Please use a stronger password", "Medium", "Strong", "Strong"]
	
	var $pwd_input = $("#"+passwordId)
	var $el = $("#"+meterId)
	var $pwd_text = $el.find("p")
	var $pwd_meter = $el.find("div")
	var $pwd_strength = $("#"+hiddenInputId)
	
	$pwd_input.keyup(function() {
		if (zxcvbn) {
			var password = $(this).val()
			var userInputs = (userInputFunc ? userInputFunc() : [])	
			var result = zxcvbn(password, userInputs)
			var score = (password.length < minLength ? 0 : result.score)
			var text = (password.length < minLength ? "Too short" : pwd_names[score])
			var cls = pwd_classes[score]
			
			pwd_classes.forEach(function(it) {
				$pwd_text.removeClass(it)
				$pwd_meter.removeClass(it)
			})
			
			$pwd_text.html(text).addClass(cls)
			$pwd_meter.addClass(cls)
			$pwd_strength.val(score)
		}
	});	
}
