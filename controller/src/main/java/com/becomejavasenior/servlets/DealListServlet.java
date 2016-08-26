package com.becomejavasenior.servlets;

import com.becomejavasenior.entity.Deal;
import com.becomejavasenior.service.DealService;
import com.becomejavasenior.service.impl.DealServiceImpl;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DealListServlet", urlPatterns = "/dealList")
public class DealListServlet extends HttpServlet {

    private ConfigurableApplicationContext context;
    private DealService dealService;

    @Override
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        dealService = context.getBean(DealService.class);
    }

    @Override
    public void destroy() {
        context.close();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        List<Deal> dealList = dealService.getDealsForList();

        session.setAttribute("dealList", dealList);
        session.setAttribute("dealService", dealService);
        response.sendRedirect("/pages/dealList.jsp");

    }
}