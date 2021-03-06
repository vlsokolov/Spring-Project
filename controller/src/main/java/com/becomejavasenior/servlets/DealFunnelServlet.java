package com.becomejavasenior.servlets;

import com.becomejavasenior.entity.Deal;
import com.becomejavasenior.entity.Stage;
import com.becomejavasenior.service.DealService;
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


@WebServlet(name = "DealFunnelServlet", urlPatterns = "/dealFunnel")
public class DealFunnelServlet extends HttpServlet {

//    private static final Logger logger = Logger.getLogger(DealFunnelServlet.class);

    private DealService dealService;
    private ConfigurableApplicationContext context;

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
        HttpSession session = request.getSession (false);

        List<Stage> stageList = dealService.getAllStage();
        session.setAttribute("stageList", stageList);
        session.setAttribute("dealService", dealService);

        List<Deal> dealList = dealService.getAll();
        session.setAttribute("dealList", dealList);

        response.sendRedirect("/pages/dealFunnel.jsp");


    }

}