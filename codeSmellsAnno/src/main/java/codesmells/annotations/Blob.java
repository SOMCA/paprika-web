package codesmells.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Blob {
	public int limitMethods() default 22;

	public int limitAttributes() default 13;

	public int limitLackOfCohesionMethods() default 40;

	public int currentMethods() default 0;

	public int currentAttributes() default 0;

	public int currentLackOfCohesionMethods() default 0;
}