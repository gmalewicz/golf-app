package com.greg.golf.entity.helpers;

import java.util.stream.IntStream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OneOfValidator implements ConstraintValidator<OneOf, Integer> {
	
	private int[] possibleValues;
	
	@Override
    public void initialize(OneOf constraintAnnotation) {
        this.possibleValues = constraintAnnotation.value();
    }
	
	public boolean isValid(Integer value, ConstraintValidatorContext context) {

		return IntStream.of(possibleValues).anyMatch(x -> x == value);
	}
}
