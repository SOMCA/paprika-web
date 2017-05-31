package codesmells.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Hbr {
	public int limitInstructions() default 26;

	public int limitCyclomatic_complexity() default 5;

	public int currentInstructions() default 0;

	public int currentCyclomatic_complexity() default 0;
}