package com.useful.annotations;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = {SizeWithinTheListValidator.class})
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SizeWithinTheListValidation {

    String message() default "Invalid size within the list";

    int min() default 0;

    int max() default 2147483647;

    String key() default "invalid.size.within.the.list";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
