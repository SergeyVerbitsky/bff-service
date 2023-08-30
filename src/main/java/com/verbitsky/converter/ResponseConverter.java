package com.verbitsky.converter;

public interface ResponseConverter<T, R> {
    R convert(T objectToConvert);

    Class<T> getResponseType();

    Class<R> getResultType();
}
