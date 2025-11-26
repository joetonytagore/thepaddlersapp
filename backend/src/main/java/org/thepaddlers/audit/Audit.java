package org.thepaddlers.audit;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Audit {
    String action();
    String entity() default "";
}

