package jellyqueen.rescat.utils;

import jellyqueen.rescat.domain.User;
import jellyqueen.rescat.domain.enums.Role;
import jellyqueen.rescat.exception.UnAuthenticationException;

import javax.servlet.http.HttpSession;

public class HttpSessionUtils {
    public static final String USER_SESSION_KEY = "loginedUser";

    public static void setUserInSession(HttpSession session, User loginUser) {
        session.setAttribute(USER_SESSION_KEY, loginUser);
    }

    public static User getUserFromSession(HttpSession session) {
        return (User) session.getAttribute(HttpSessionUtils.USER_SESSION_KEY);
    }

    public static boolean isLoginUser(HttpSession session) {
        return session.getAttribute(USER_SESSION_KEY) != null;
    }

    private static void checkLoginUser(HttpSession session) {
        if (!isLoginUser(session))
            throw new UnAuthenticationException("user", "로그인이 필요합니다.");
    }

    public static User getAdminUserIfPresent(HttpSession session) {
        checkLoginUser(session);

        User user = getUserFromSession(session);
        if (user.getRole() != Role.ADMIN)
            throw new UnAuthenticationException("user", "관리자 계정이 아닙니다.");

        return user;
    }

    public static void checkAdminUser(HttpSession session) {
        checkLoginUser(session);

        if (getUserFromSession(session).getRole() != Role.ADMIN)
            throw new UnAuthenticationException("user", "관리자 계정이 아닙니다.");
    }

    public static void removeUserInSession(HttpSession session) {
        session.removeAttribute(USER_SESSION_KEY);
    }
}
