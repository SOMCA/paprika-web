package codesmells.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Blob {
	public int limitMethod() default 22;

	public int limitAttribute() default 13;

	public int limitLackOfCohesionMethod() default 40;

	public int currentMethod() default 0;

	public int currentAttribute() default 0;

	public int currentLackOfCohesionMethod() default 0;
}