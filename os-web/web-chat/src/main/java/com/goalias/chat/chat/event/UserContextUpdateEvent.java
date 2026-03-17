package com.goalias.chat.chat.event;

import org.springframework.context.ApplicationEvent;

public class UserContextUpdateEvent extends ApplicationEvent {

    private final Long userId;
    private final String describe;

    public UserContextUpdateEvent(Long userId,String describe) {
        super(userId);
        this.userId = userId;
        this.describe = describe;
    }

    public Long getUserId() { return userId; }
    public String getDescribe() { return describe; }
}

