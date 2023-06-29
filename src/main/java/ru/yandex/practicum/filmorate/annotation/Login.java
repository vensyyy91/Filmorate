package ru.yandex.practicum.filmorate.annotation;

import ru.yandex.practicum.filmorate.validator.LoginValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Constraint(validatedBy = LoginValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Login {
    String message() default "Логин не должен содержать пробелов";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
