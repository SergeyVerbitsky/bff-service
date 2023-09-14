package com.verbitsky.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.verbitsky.exception.ServiceException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConverterManagerImpl implements ConverterManager {
    private final Map<Class<?>, Map<Class<?>, ResponseConverter<?, ?>>> converters;

    @Autowired
    public ConverterManagerImpl(List<ResponseConverter<?, ?>> converters) {
        this.converters = converters.stream()
                .collect(Collectors.groupingBy(
                        ResponseConverter::getResponseType,
                        Collectors.toMap(ResponseConverter::getResultType, Function.identity())
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S, R> ResponseConverter<S, R> provideConverter(Class<S> typeFrom, Class<R> typeTo) {
        Map<Class<?>, ResponseConverter<?, ?>> innerMap = converters.get(typeFrom);
        if (innerMap == null) {
            throw new ServiceException(String.format("No converters found for type %s", typeFrom));
        }

        ResponseConverter<?, ?> converter = innerMap.get(typeTo);
        if (converter == null) {
            throw new ServiceException(String.format(
                    "No converter found for conversion from %s to %s", typeFrom, typeTo));
        }

        return (ResponseConverter<S, R>) converter;
    }
}










