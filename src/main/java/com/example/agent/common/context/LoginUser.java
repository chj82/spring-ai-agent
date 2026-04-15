package com.example.agent.common.context;

import com.example.agent.model.enums.ActorType;

public class LoginUser {

    private final String token;
    private final Long userId;
    private final String username;
    private final String nickname;
    private final ActorType actorType;
    private final boolean superAdmin;

    public LoginUser(String token, Long userId, String username, String nickname, ActorType actorType, boolean superAdmin) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.actorType = actorType;
        this.superAdmin = superAdmin;
    }

    public String getToken() {
        return token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }
}
