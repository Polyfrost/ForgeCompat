package cc.polyfrost.oneconfig.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Slider {
    String name();

    float min();

    float max();

    int step() default 0;

    boolean instant() default false;

    String description() default "";

    String category() default "General";

    String subcategory() default "";
}
