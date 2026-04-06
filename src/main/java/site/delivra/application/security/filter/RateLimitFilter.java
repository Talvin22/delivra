package site.delivra.application.security.filter;

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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int LOGIN_MAX_ATTEMPTS = 5;
    private static final long LOGIN_WINDOW_MS = 60_000L;

    private static final int REGISTER_MAX_ATTEMPTS = 3;
    private static final long REGISTER_WINDOW_MS = 300_000L;

    private final Map<String, Deque<Long>> loginAttempts = new ConcurrentHashMap<>();
    private final Map<String, Deque<Long>> registerAttempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String method = request.getMethod();
        String ip = request.getRemoteAddr();

        if ("POST".equals(method)) {
            if ("/auth/login".equals(path)) {
                if (isRateLimited(loginAttempts, ip, LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW_MS)) {
                    log.warn("Rate limit exceeded on login: ip={}", ip);
                    writeTooManyRequests(response);
                    return;
                }
            } else if ("/auth/register".equals(path) || "/companies/register".equals(path)) {
                if (isRateLimited(registerAttempts, ip, REGISTER_MAX_ATTEMPTS, REGISTER_WINDOW_MS)) {
                    log.warn("Rate limit exceeded on register: ip={}, path={}", ip, path);
                    writeTooManyRequests(response);
                    return;
                }
            }
        }

        chain.doFilter(req, res);
    }

    private boolean isRateLimited(Map<String, Deque<Long>> store, String ip, int maxAttempts, long windowMs) {
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = store.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxAttempts) {
                return true;
            }
            timestamps.addLast(now);
            return false;
        }
    }

    private void writeTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setContentType("application/json");
        response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
    }
}
