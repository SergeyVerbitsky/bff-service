package com.verbitsky.storage;

import jakarta.annotation.Nonnull;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.verbitsky.entity.SessionEntity;

import static com.verbitsky.storage.CacheConstants.ENTITY_SESSION_ID_KEY;
import static com.verbitsky.storage.CacheConstants.ENTITY_USER_LOGIN_KEY;
import static com.verbitsky.storage.CacheConstants.NULL_RESULT_CONDITION;
import static com.verbitsky.storage.CacheConstants.SESSION_ID_KEY;
import static com.verbitsky.storage.CacheConstants.USER_LOGIN_KEY;
import static com.verbitsky.storage.CacheConstants.USER_SESSION_BY_ID;
import static com.verbitsky.storage.CacheConstants.USER_SESSION_BY_LOGIN;

@Transactional
@Repository
public interface SessionModelRepo extends CrudRepository<SessionEntity, String> {
    @Cacheable(value = USER_SESSION_BY_ID, key = SESSION_ID_KEY, condition = NULL_RESULT_CONDITION)
    SessionEntity findBySessionId(String sessionId);

    @Cacheable(value = USER_SESSION_BY_LOGIN, key = USER_LOGIN_KEY, condition = NULL_RESULT_CONDITION)
    SessionEntity findByLogin(String login);

    @Nonnull
    @Override
    @Caching(put = {
            @CachePut(value = USER_SESSION_BY_ID, key = ENTITY_SESSION_ID_KEY),
            @CachePut(value = USER_SESSION_BY_LOGIN, key = ENTITY_USER_LOGIN_KEY)
    })
    <S extends SessionEntity> S save(@Nonnull S entity);

    @Override
    @Caching(evict = {
            @CacheEvict(value = USER_SESSION_BY_ID, key = ENTITY_SESSION_ID_KEY),
            @CacheEvict(value = USER_SESSION_BY_LOGIN, key = ENTITY_USER_LOGIN_KEY)
    })
    void delete(@Nonnull @Param("entity") SessionEntity entity);
}
