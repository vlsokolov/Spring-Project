package com.becomejavasenior.servlets;

import com.becomejavasenior.entity.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;

@WebFilter(filterName = "AuthenticationFilter", urlPatterns = "/*")
public class AuthenticationFilter implements Filter {

    private static final String LOGIN_PAGE = "/login";
    private static final String REGISTER_PAGE = "/register";
    private static final String LOCALE_CHANGE_PARAM = "language";

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        // nothing to init
    }

    @Override
    public void destroy() {
        // nothing to destroy
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpSession session = ((HttpServletRequest) request).getSession(false);
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        User user = null;

        String locale = request.getParameter(LOCALE_CHANGE_PARAM);
        if(locale == null) {
            locale = "ru";
        }

        request.setAttribute("javax.servlet.jsp.jstl.fmt.locale.request", new Locale(locale));
        if (session != null) {
            user = (User) session.getAttribute("user");
            session.setAttribute("javax.servlet.jsp.jstl.fmt.locale.session", new Locale(locale));
        }
        if (user != null || LOGIN_PAGE.equals(requestURI) || REGISTER_PAGE.equals(requestURI)) {
            chain.doFilter(request, response);
        } else {
            request.setAttribute("fromPage", requestURI);
            request.getRequestDispatcher(LOGIN_PAGE).forward(request, response);
        }

    }
}