package com.bas.auction.core.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

@Configuration
class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                // message types other than MESSAGE and SUBSCRIBE
                .nullDestMatcher().authenticated()
                // anyone can access the errors
                .simpDestMatchers("/user/queue/errors").permitAll()
                // matches any destination that starts with /app/
                .simpDestMatchers("/app/**").authenticated()
                // matches any destination for SimpMessageType.SUBSCRIBE that starts with /user/ or /topic/friends/
                .simpSubscribeDestMatchers("/user/**", "/topic/**").authenticated()

                // (i.e. cannot send messages directly to /topic/, /queue/)
                // (i.e. cannot subscribe to /topic/messages/* to get messages sent to /topic/messages-user<id>)
                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
                // catch all
                .anyMessage().denyAll();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}
