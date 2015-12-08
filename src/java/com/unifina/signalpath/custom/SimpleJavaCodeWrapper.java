package com.unifina.signalpath.custom;

public class SimpleJavaCodeWrapper extends AbstractJavaCodeWrapper {
	
/*
public class [[CLASSNAME]] extends AbstractCustomModule {
 */
	
	@Override
	protected String getHeader() {
		return "public class [[CLASSNAME]] extends AbstractCustomModule {\n" +
				"public [[CLASSNAME]]() { super(); }\n";
	}

/*
// Define inputs and outputs here
// TimeSeriesInput input = new TimeSeriesInput(this,"in");
// TimeSeriesOutput output = new TimeSeriesOutput(this,"out");

public void initialize() {
  // Initialize local variables
}

public void sendOutput() {
  //Write your module code here
}

public void clearState() {
  // Clear internal state
}
 */
	@Override
	protected String getDefaultCode() {
		return "// Define inputs and outputs here\r\n" + 
				"// TimeSeriesInput input = new TimeSeriesInput(this,\"in\");\r\n" + 
				"// TimeSeriesOutput output = new TimeSeriesOutput(this,\"out\");\r\n" + 
				"\r\n" + 
				"public void initialize() {\r\n" + 
				"  // Initialize local variables\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"public void sendOutput() {\r\n" + 
				"  //Write your module code here\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"public void clearState() {\r\n" + 
				"  // Clear internal state\r\n" + 
				"}";
	}
	
	@Override
	protected String getFooter() {
		return "}"; 
	}

}
