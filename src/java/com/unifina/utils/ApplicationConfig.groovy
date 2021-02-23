package com.unifina.utils

import grails.util.Holders
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode

@GrailsCompileStatic
class ApplicationConfig {
    static String getString(String key) {
        return MapTraversal.getString(Holders.getConfig(), key)
    }
}