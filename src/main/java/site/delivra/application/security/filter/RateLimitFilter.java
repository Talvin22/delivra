package site.delivra.application.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();

    private Bucket newLoginBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(5, Duration.ofMinutes(1)))
                .build();
    }

    private Bucket newRegisterBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.simple(3, Duration.ofMinutes(5)))
                .build();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        Bucket bucket = null;
        if ("POST".equals(method)) {
            if ("/auth/login".equals(path)) {
                bucket = loginBuckets.computeIfAbsent(ip, k -> newLoginBucket());
            } else if ("/auth/register".equals(path) || "/companies/register".equals(path)) {
                bucket = registerBuckets.computeIfAbsent(ip, k -> newRegisterBucket());
            }
        }

        if (bucket != null && !bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded: ip={}, path={}", ip, path);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}
