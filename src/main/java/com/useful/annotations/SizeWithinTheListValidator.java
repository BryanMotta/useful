package com.useful.annotations;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class SizeWithinTheListValidator implements ConstraintValidator<SizeWithinTheListValidation, List<String>> {

    private String key;
    private int min;
    private int max;

    @Override
    public void initialize(SizeWithinTheListValidation annotation) {
        this.key = annotation.key();
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext constraintValidatorContext) {
        boolean math = value.stream().anyMatch(v -> v.length() >= min && v.length() <= max);
        if (math) return true;
        constraintValidatorContext.buildConstraintViolationWithTemplate(key).addConstraintViolation();
        return false;
    }

    public void setMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }


}
