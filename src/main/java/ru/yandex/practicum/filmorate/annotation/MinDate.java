package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.DateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = DateValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinDate {
    String value();
    String message() default "Дата должна быть не раньше {value}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
