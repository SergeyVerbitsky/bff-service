package com.verbitsky.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConverterProviderImpl implements ConverterProvider {
    private final Map<Class<?>, Map<Class<?>, ResponseConverter<?, ?>>> converters;

    @Autowired
    public ConverterProviderImpl(List<ResponseConverter<?, ?>> converters) {
        this.converters = converters.stream()
                .collect(Collectors.groupingBy(
                        ResponseConverter::getResponseType,
                        Collectors.toMap(ResponseConverter::getResultType, Function.identity())
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, R> ResponseConverter<T, R> provideConverter(Class<T> typeFrom, Class<R> typeTo) {
        Map<Class<?>, ResponseConverter<?, ?>> innerMap = converters.get(typeFrom);
        if (innerMap == null) {
            throw new SecurityException("No converters found for type " + typeFrom);
        }

        ResponseConverter<?, ?> converter = innerMap.get(typeTo);
        if (converter == null) {
            throw new SecurityException("No converter found for conversion from " + typeFrom + " to " + typeTo);
        }

        return (ResponseConverter<T, R>) converter;
    }
}










