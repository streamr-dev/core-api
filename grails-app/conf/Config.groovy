// configuration for plugin testing - will not be included in the plugin zip

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

// Example config for Kafka
unifina.kafka.metadata.broker.list = "192.168.10.81:9092"
unifina.kafka.producer.type = "async"
unifina.kafka.serializer.class = "kafka.serializer.StringEncoder"
unifina.kafka.request.required.acks = "1"
unifina.kafka.zookeeper.connect = "192.168.10.81:2181"
unifina.kafka.group.id = "unifina"

unifina.feed.fileStorageAdapter = "com.unifina.feed.file.HTTPFileStorageAdapter"