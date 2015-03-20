package com.jparkie.hawleyretainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Retain the annotated field in a Map encapsulated by a retained fragment with the field's name as the key. The retained value is casted accordingly upon retrieval.
 * <pre>
 *     <code>
 *         {@literal @}HawleyRetain
 *         Observable<Response> mNetworkObservable;
 *     </code>
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface HawleyRetain {}