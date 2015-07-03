// configuration for plugin testing - will not be included in the plugin zip

// Most of the config comes from UnifinaCoreDefaultConfig.groovy

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}


/**
 * com.unifina.feed.file.S3FileStorageAdapter config
 */
// The following are used with S3FileStorageAdapter
unifina.feed.s3FileStorageAdapter.accessKey = "AKIAJ5FFWRZLSQB6ASIQ"
unifina.feed.s3FileStorageAdapter.secretKey = "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"
unifina.feed.s3FileStorageAdapter.bucket = "streamr-data-us"
