package site.delivra.application.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_GEOCODING = "geocoding";
    public static final String CACHE_ROUTES = "routes";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache geocodingCache = new CaffeineCache(CACHE_GEOCODING,
                Caffeine.newBuilder()
                        .maximumSize(1000)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .build());

        CaffeineCache routesCache = new CaffeineCache(CACHE_ROUTES,
                Caffeine.newBuilder()
                        .maximumSize(500)
                        .expireAfterWrite(30, TimeUnit.MINUTES)
                        .build());

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(geocodingCache, routesCache));
        return manager;
    }
}
