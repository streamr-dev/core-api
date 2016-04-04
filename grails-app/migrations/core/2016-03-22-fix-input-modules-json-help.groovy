package core
databaseChangeLog = {

	changeSet(author: "eric", id: "random-modules") {
		def fixes = [
			[
		    	name: "Button",
				fixedJsonHelp: '{"params":{"buttonName":"The name which the button gets","outputValue":"Value which is outputted when the button is clicked"},"paramNames":["buttonName","outputValue"],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>The button module outputs the given value everytime the button is pressed. Module can be used any time, even during a run.</p>"}'
			],
			[
		    	name: "Switcher",
				fixedJsonHelp: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>The module ouputs even 1 or 0 depending of the value of the switcher. The value can be changed during a run.</p>"}'
			],
			[
			    name: "TextField",
				fixedJsonHelp: '{"params":{},"paramNames":[],"inputs":{},"inputNames":[],"outputs":{},"outputNames":[],"helpText":"<p>The module outputs the value of the text field every time &#39;send&#39; is pressed.</p>"}'
			]
		]

		grailsChange {
			change {
				fixes.each {
					sql.executeUpdate("""
						UPDATE module
						SET
							json_help = ?
						WHERE
							name = ?""", [it.fixedJsonHelp, it.name])
				}
			}
		}
	}
}
