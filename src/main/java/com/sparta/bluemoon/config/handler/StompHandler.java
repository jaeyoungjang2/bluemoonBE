package com.sparta.bluemoon.config.handler;

import com.sparta.bluemoon.domain.User;
import com.sparta.bluemoon.repository.RedisRepository;
import com.sparta.bluemoon.repository.UserRepository;
import com.sparta.bluemoon.security.jwt.JwtDecoder;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompHandler implements ChannelInterceptor {

    private final RedisRepository redisRepository;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;


    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String jwtToken = "";
        if (StompCommand.CONNECT == accessor.getCommand()) {
            // 사용자 확인
            jwtToken = accessor.getFirstNativeHeader("token");
            String username = jwtDecoder.decodeUsername(jwtToken);
            User user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("사용자가 존재하지 않습니다.")
            );

            Long userId = user.getId();
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            redisRepository.saveMyInfo(sessionId, userId);

        } else if (StompCommand.CONNECT == accessor.getCommand()) { // Websocket 연결 종료

            String sessionId = (String) message.getHeaders().get("simpSessionId");
            Long userId = redisRepository.getMyInfo(sessionId);

            // 채팅방 퇴장 정보 저장
            if (redisRepository.existChatRoomUserInfo(userId)) {
                redisRepository.exitUserEnterRoomId(userId);
            }

            redisRepository.deleteMyInfo(sessionId);
        }
        return message;
    }
}