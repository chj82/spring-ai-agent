package com.example.agent.model.vo;

public class LoginUserVO {

    /** 用户ID */
    private Long userId;
    /** 登录账号 */
    private String username;
    /** 昵称 */
    private String nickname;
    /** 账号类型 */
    private String actorType;
    /** 是否超级管理员 */
    private Boolean superAdmin;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public Boolean getSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(Boolean superAdmin) {
        this.superAdmin = superAdmin;
    }
}
