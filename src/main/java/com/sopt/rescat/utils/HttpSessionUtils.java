package com.sopt.rescat.utils;

import com.sopt.rescat.domain.User;

import javax.servlet.http.HttpSession;

public class HttpSessionUtils {
    public static final String USER_SESSION_KEY = "Authorization";
    public static final String SESSION = "session";

    public static void setTokenInSession(HttpSession session, String token) {
        session.setAttribute(USER_SESSION_KEY, token);
    }

    public static User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute(HttpSessionUtils.USER_SESSION_KEY);
    }

    public static boolean isLoginUser(HttpSession session) {
        return session.getAttribute(USER_SESSION_KEY) != null;
    }

    public static void removeUserInSession(HttpSession session) {
        session.removeAttribute(USER_SESSION_KEY);
    }
}
