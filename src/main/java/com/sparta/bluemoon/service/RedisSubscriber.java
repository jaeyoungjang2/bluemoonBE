package com.sparta.bluemoon.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.bluemoon.dto.ChatMessageDto;
import com.sparta.bluemoon.dto.response.AlarmResponseDto;
import com.sparta.bluemoon.dto.response.MessageResponseDto;
import com.sparta.bluemoon.dto.response.UnreadMessageCount;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis에서 메시지가 발행(publish)되면 대기하고 있던 onMessage가 해당 메시지를 받아 처리한다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            System.out.println("onmessage에서 잡아서 진행합니다");
            // redis에서 발행된 데이터를 받아 deserialize
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            // ChatMessageDto 객채로 맵핑
            ChatMessageDto roomMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);

            // Websocket 구독자에게 채팅 메시지 Send

            //알람메세지
            if (roomMessage.getType().equals(ChatMessageDto.MessageType.ENTER)) {
                System.out.println("Enter에 걸린게 맞나요?");
                AlarmResponseDto alarmResponseDto = new AlarmResponseDto(roomMessage);
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getOtherUserId(), alarmResponseDto);
            //안읽은 메세지
            }else if(roomMessage.getType().equals(ChatMessageDto.MessageType.UNREAD_MESSAGE_COUNT)){
                System.out.println("UNREAD_MESSAGE_COUNT에 걸린게 맞나요?");
                UnreadMessageCount unreadMessageCount = new UnreadMessageCount(roomMessage);
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getOtherUserId(), unreadMessageCount);
            //채팅 메세지
            }else {
                System.out.println("아니요 걸리지 않았습니다.");
                MessageResponseDto messageResponseDto = new MessageResponseDto(roomMessage);
                messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), messageResponseDto);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}