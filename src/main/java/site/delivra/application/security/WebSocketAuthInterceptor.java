package site.delivra.application.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import site.delivra.application.service.model.AuthenticationConstants;

import java.util.List;

/**
 * Intercepts STOMP CONNECT frames and validates the JWT token.
 * Clients must send: CONNECT with header Authorization: Bearer <token>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtTokenProvider.validateToken(token)) {
                    String username = jwtTokenProvider.getUsername(token);
                    String userId = jwtTokenProvider.getUserId(token);
                    List<String> roles = jwtTokenProvider.getRoles(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    auth.setDetails(userId);

                    accessor.setUser(auth);
                    log.debug("WebSocket authenticated: user={}, sessionId={}", username, accessor.getSessionId());
                } else {
                    log.warn("WebSocket CONNECT rejected: invalid JWT token");
                    throw new org.springframework.security.authentication.BadCredentialsException("Invalid JWT token");
                }
            } else {
                log.warn("WebSocket CONNECT rejected: missing Authorization header");
                throw new org.springframework.security.authentication.BadCredentialsException("Missing Authorization header");
            }
        }

        return message;
    }
}
