package com.unifina.signalpath;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used to exclude some declared endpoints from endpoint autodetection happening in AbstractSignalPathModule#init()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ExcludeInAutodetection {

}
