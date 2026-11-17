package cc.polyfrost.oneconfig.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Button {
    String name();

    String text();

    String description() default "";

    int size() default 1;

    String category() default "General";

    String subcategory() default "";
}
