package ru.noties.gsonflatten;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The pattern is simple, delimit with `::`
 * Created by Dimitry Ivanov on 29.10.2015.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Flatten {
    String value();
}
