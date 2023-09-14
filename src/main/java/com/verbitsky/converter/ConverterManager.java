package com.verbitsky.converter;

public interface ConverterManager {
    <S, R> ResponseConverter<S, R> provideConverter(Class<S> typeFrom, Class<R> typeTo);
}
