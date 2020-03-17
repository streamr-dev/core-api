package grails.plugin.springsecurity.annotation;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.*;

/**
 * Specify the property file key with this annotation, and the AST transform
 * class will replace with an @Secured annotation with the associated role names.
 *
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@GroovyASTTransformationClass("grails.plugin.grails.plugin.springsecurity.annotation.AuthoritiesTransformation")
public @interface Authorities {
	/**
	 * The property file key; the property value will be a comma-delimited list of role names.
	 * @return the key
	 */
	String value();
}
