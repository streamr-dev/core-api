package pages

import geb.*

abstract class GrailsPage extends Page {

    // To be overridden by subclasses
    static controller = null
    static action = null

    static at = {
        // delegate here is the original page _instance_ (i.e. the subclass)

        def expectedPageControllerName = delegate.class.controller
        if (expectedPageControllerName == null) {
            throw new IllegalStateException("${delegate.class} forgot to declare which controller it belongs to")
        }

        def expectedPageActionName = delegate.class.action
        if (expectedPageActionName == null) {
            throw new IllegalStateException("${delegate.class} forgot to declare which action it is")
        }

		def actualPageControllerName
		def actualPageActionName
		
		int retryCount = 100
		while (retryCount-- > 0) {
			try {
				waitFor { $("meta", name: "pageId").size()>0 }
		        actualPageControllerName = $("meta", name: "pageId").@content.split('\\.')[0]
		        actualPageActionName = $("meta", name: "pageId").@content.split('\\.')[1]
				retryCount = 0
			} catch (StaleElementReferenceException) {
				println "Got StaleElementRefenceException, retrying $expectedPageControllerName/$expectedPageActionName..."
				Thread.sleep(100)
			}
		}

        assert actualPageControllerName == expectedPageControllerName
        assert actualPageActionName == expectedPageActionName

        true // at checkers must return true
    }
}