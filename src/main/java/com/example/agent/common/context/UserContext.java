package com.example.agent.common.context;

import com.example.agent.common.exception.BizException;
import com.example.agent.model.enums.ActorType;

public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static LoginUser getRequiredUser() {
        LoginUser loginUser = HOLDER.get();
        if (loginUser == null) {
            throw new BizException(401, "未登录");
        }
        return loginUser;
    }

    public static LoginUser getRequiredAdminUser() {
        LoginUser loginUser = getRequiredUser();
        if (loginUser.getActorType() != ActorType.ADMIN) {
            throw new BizException(403, "无后台访问权限");
        }
        return loginUser;
    }

    public static LoginUser getRequiredAppUser() {
        LoginUser loginUser = getRequiredUser();
        if (loginUser.getActorType() != ActorType.APP) {
            throw new BizException(403, "无前端访问权限");
        }
        return loginUser;
    }
}
