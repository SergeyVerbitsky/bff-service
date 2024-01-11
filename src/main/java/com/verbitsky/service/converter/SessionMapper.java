package com.verbitsky.service.converter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.verbitsky.api.model.SessionModel;
import com.verbitsky.entity.SessionEntity;

@Mapper
public interface SessionMapper {
    SessionMapper INSTANCE = Mappers.getMapper(SessionMapper.class);

    @Mapping(target = "created", ignore = true)
    @Mapping(target = "updated", ignore = true)
    SessionEntity toEntity(SessionModel dto);

    SessionModel toDto(SessionEntity entity);
}
