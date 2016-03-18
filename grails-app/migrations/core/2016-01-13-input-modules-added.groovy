package core
databaseChangeLog = {

	changeSet(author: "aapeli", id: "1452676788216-1") {
		sql("""
			INSERT INTO module_category (id,version,module_package_id,name,sort_order) VALUES
				(100,0, 1, 'Input', 140)
			;
		""");
		sql("""
			INSERT INTO module VALUES
				(218, 2, 100, 'com.unifina.signalpath.input.ButtonModule', 'Button', 'InputModule', NULL, 'module', 1, '{\\\\\\"params\\\\\\":{\\\\\\"buttonName\\\\\\":\\\\\\"The name which the button gets\\\\\\",\\\\\\"outputValue\\\\\\":\\\\\\"Value which is outputted when the button is clicked\\\\\\"},\\\\\\"paramNames\\\\\\":[\\\\\\"buttonName\\\\\\",\\\\\\"outputValue\\\\\\"],\\\\\\"inputs\\\\\\":{},\\\\\\"inputNames\\\\\\":[],\\\\\\"outputs\\\\\\":{},\\\\\\"outputNames\\\\\\":[],\\\\\\"helpText\\\\\\":\\\\\\"<p>The button module outputs the given value everytime the button is pressed. Module can be used any time, even during a run.</p>\\\\\\\\n\\\\\\"}', NULL, 'streamr-button'),
				(219, 2, 100, 'com.unifina.signalpath.input.SwitcherModule', 'Switcher', 'InputModule', NULL, 'module', 1, '{\\\\\\"params\\\\\\":{},\\\\\\"paramNames\\\\\\":[],\\\\\\"inputs\\\\\\":{},\\\\\\"inputNames\\\\\\":[],\\\\\\"outputs\\\\\\":{},\\\\\\"outputNames\\\\\\":[],\\\\\\"helpText\\\\\\":\\\\\\"<p>The module ouputs even 1 or 0 depending of the value of the switcher. The value can be changed during a run.</p>\\\\\\\\n\\\\\\"}', NULL, 'streamr-switcher'),
				(220, 3, 100, 'com.unifina.signalpath.input.TextFieldModule', 'TextField', 'InputModule', NULL, 'module', 1, '{\\\\\\"params\\\\\\":{},\\\\\\"paramNames\\\\\\":[],\\\\\\"inputs\\\\\\":{},\\\\\\"inputNames\\\\\\":[],\\\\\\"outputs\\\\\\":{},\\\\\\"outputNames\\\\\\":[],\\\\\\"helpText\\\\\\":\\\\\\"<p>The module outputs the value of the text field every time &#39;send&#39; is pressed.</p>\\\\\\\\n\\\\\\"}', NULL, 'streamr-text-field')
			;
		""");
	}
}
