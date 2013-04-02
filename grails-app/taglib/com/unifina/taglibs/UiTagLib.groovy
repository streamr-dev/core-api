package com.unifina.taglibs

import java.text.SimpleDateFormat;


class UiTagLib {
	
	static namespace = "ui"
	
	/**
	* Renders a jQuery ui datepicker.
	*
	* @attr name REQUIRED the field name/id
	* @attr value a Date object that represents the initial value
	*/
	def datePicker = {attrs, body->
		def name = attrs.name
		def id = attrs.id ?: name
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd")
		def date = df.format(attrs.value ?: new Date())
		
		// If you want to disable weekends, add this to the datepicker options:
		// beforeShowDay: \$.datepicker.noWeekends
		
		def str = """
			<input type="text" name="$name" id="$id" value="$date"/>
			<script type="text/javascript">
				\$(document).ready(function() {
					\$("#${id}").datepicker({
						dateFormat: "yy-mm-dd",
						firstDay: 1
					});
				});
			</script>
		"""
		out << str
	}

}
