package com.verbitsky.service.converter;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.verbitsky.api.converter.EntityDtoConverter;
import com.verbitsky.api.model.SessionModel;
import com.verbitsky.entity.SessionEntity;

import java.util.Set;

@Component
public class SessionModelEntityConverter implements EntityDtoConverter<SessionModel, SessionEntity> {
    @Override
    public SessionEntity convertToEntity(@NonNull SessionModel dto) {
        return SessionMapper.INSTANCE.toEntity(dto);
    }

    @Override
    public SessionModel convertToDto(@NonNull SessionEntity entity) {
        return SessionMapper.INSTANCE.toDto(entity);
    }

    @Override
    public Set<Class<?>> getSupportedTypes() {
        return Set.of(SessionModel.class, SessionEntity.class);
    }
}
