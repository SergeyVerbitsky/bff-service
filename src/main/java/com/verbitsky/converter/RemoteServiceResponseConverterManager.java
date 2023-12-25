package com.verbitsky.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.verbitsky.api.converter.ServiceResponseConverter;
import com.verbitsky.api.converter.ServiceResponseConverterManager;
import com.verbitsky.api.exception.ServiceException;
import com.verbitsky.api.model.ApiModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RemoteServiceResponseConverterManager implements ServiceResponseConverterManager {
    private final Map<Class<?>, Map<Class<?>, ServiceResponseConverter<?, ?>>> converters;

    @Autowired
    public RemoteServiceResponseConverterManager(List<ServiceResponseConverter<?, ?>> converters) {
        this.converters = converters.stream()
                .collect(Collectors.groupingBy(
                        ServiceResponseConverter::getResponseType,
                        Collectors.toMap(ServiceResponseConverter::getResultType, Function.identity())
                ));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S extends ApiModel, R> ServiceResponseConverter<S, R> provideConverter(Class<S> typeFrom, Class<R> typeTo) {
        if (Objects.isNull(typeFrom) || Objects.isNull(typeTo)) {
            throw new IllegalArgumentException("Received wrong method parameters (null)");
        }

        Map<Class<?>, ServiceResponseConverter<?, ?>> innerMap = converters.get(typeFrom);
        if (innerMap == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("No suitable converters found for type %s", typeFrom.getSimpleName()));
        }

        ServiceResponseConverter<?, ?> converter = innerMap.get(typeTo);
        if (converter == null) {
            throw new ServiceException(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("No suitable converter found for conversion from %s to %s",
                            typeFrom.getSimpleName(), typeTo.getSimpleName()));
        }

        return (ServiceResponseConverter<S, R>) converter;
    }
}










