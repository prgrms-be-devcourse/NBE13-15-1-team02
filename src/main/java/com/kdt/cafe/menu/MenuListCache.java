package com.kdt.cafe.menu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

// 선택 심화: A06 전체 메뉴 목록 Redis 캐시
// 키 cafe:menus:all / 값 MenuResponse 배열 JSON 문자열 / TTL 설정값(초)
@Component
public class MenuListCache {

    private static final Logger log = LoggerFactory.getLogger(MenuListCache.class);

    static final String KEY = "cafe:menus:all";
    private static final TypeReference<List<MenuResponse>> MENU_LIST = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final Duration ttl;

    public MenuListCache(StringRedisTemplate redisTemplate,
                         @Value("${cafe.cache.menus.ttl-seconds:60}") long ttlSeconds) {
        if (ttlSeconds < 1) {
            throw new IllegalArgumentException("메뉴 캐시 TTL은 1초 이상의 정수여야 합니다.");
        }
        this.redisTemplate = redisTemplate;
        this.ttl = Duration.ofSeconds(ttlSeconds);
    }

    // 캐시에 있으면 목록, 없거나 Redis 오류면 빈 값 (호출한 쪽에서 DB 조회)
    public Optional<List<MenuResponse>> get() {
        try {
            String json = redisTemplate.opsForValue().get(KEY);
            if (json == null) {
                log.info("메뉴 캐시 MISS - DB에서 조회합니다. key={}", KEY);
                return Optional.empty();
            }
            log.info("메뉴 캐시 HIT - DB를 조회하지 않습니다. key={}", KEY);
            return Optional.of(jsonMapper.readValue(json, MENU_LIST));
        } catch (DataAccessException | JacksonException e) {
            log.warn("메뉴 캐시 조회 실패 - DB에서 조회합니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void put(List<MenuResponse> menus) {
        try {
            redisTemplate.opsForValue().set(KEY, jsonMapper.writeValueAsString(menus), ttl);
        } catch (DataAccessException | JacksonException e) {
            log.warn("메뉴 캐시 저장 실패. {}", e.getMessage());
        }
    }

    // 메뉴가 바뀌면 캐시 삭제. 트랜잭션 안이면 커밋이 끝난 뒤에 삭제
    // (커밋 전에 지우면 그 사이 다른 조회가 이전 데이터를 다시 캐시에 넣을 수 있음)
    public void evictAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    evict();
                }
            });
        } else {
            evict();
        }
    }

    void evict() {
        try {
            redisTemplate.delete(KEY);
            log.info("메뉴 캐시 삭제 key={}", KEY);
        } catch (DataAccessException e) {
            log.warn("메뉴 캐시 삭제 실패. {}", e.getMessage());
        }
    }
}