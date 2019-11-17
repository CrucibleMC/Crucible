package net.cubespace.Yamler.Config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializeOptions {
	String[] configHeader() default {};

	boolean skipFailedObjects() default false;

	ConfigMode configMode() default ConfigMode.FIELD_IS_KEY;
}
