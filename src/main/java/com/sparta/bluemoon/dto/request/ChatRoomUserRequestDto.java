package com.sparta.bluemoon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@AllArgsConstructor
@Getter
@Setter

public class ChatRoomUserRequestDto {

    private String roomId;
    private Long userId;// 상대방


}
