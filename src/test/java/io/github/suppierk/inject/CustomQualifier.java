package io.github.suppierk.inject;

import jakarta.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** For testing if it works. */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomQualifier {}
