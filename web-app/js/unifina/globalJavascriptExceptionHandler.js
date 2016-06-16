window.onerror = (function(oldHandler) {
	return function myErrorHandler(errorMsg, url, lineNumber, column, errorObj) {
	  var reportUrl = (Streamr ? Streamr.projectWebroot : "/") + "javascriptError/logError";
	  var data = {
			  url: url,
			  errorMsg: errorMsg,
			  line: lineNumber,
			  column: column,
			  stack: (errorObj!=null ? errorObj.stack : null)
	  }
	  
	  var reportedSuccessfully = false;
	  
	  // Report the error to JavascriptErrorController
	  if ($) {
		  $.ajax({
			  type: "POST",
			  url: reportUrl,
			  data: data,
			  success: function() {
				  reportedSuccessfully = true;
			  },
			  async: false
			});
	  }
	  
/*	  if (reportedSuccessfully)
		  alert("Oops! An error occurred on this page. The bug has been reported. Sorry!");
	  else
		  alert("Oops! An error occurred on this page. Please let us know!");*/

	  // Call previously installed handler or return false to continue default handling
	  if (oldHandler)
		  return oldHandler(errorMsg, url, lineNumber, column, errorObj);
	  else return false;
	}
})(window.onerror);
