package com.verbitsky.converter;

import org.springframework.core.convert.converter.Converter;

public interface ResponseConverter<S, R> extends Converter<S, R> {
    //Todo refactor this (see backend app)
    Class<S> getResponseType();

    Class<R> getResultType();
}
