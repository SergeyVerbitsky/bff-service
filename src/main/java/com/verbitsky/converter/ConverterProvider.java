package com.verbitsky.converter;

public interface ConverterProvider {

    <T, R> ResponseConverter<T, R> provideConverter(Class<T> typeFrom, Class<R> typeTo);
}
