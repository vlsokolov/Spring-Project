package com.becomejavasenior.servlets;

import com.becomejavasenior.entity.Company;
import com.becomejavasenior.service.CompanyService;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class ViewCompaniesServlet extends HttpServlet {

    private CompanyService companyService;
    private ConfigurableApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
        companyService = context.getBean(CompanyService.class);
    }

    @Override
    public void destroy() {
        context.close();
        super.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Company> companyList = companyService.getAll();
        companyList.sort((Company company1, Company company2) -> company1.getName().compareTo(company2.getName()));
        request.setAttribute("companyList", companyList);

        RequestDispatcher requestDispatcher = getServletContext().getRequestDispatcher("/WEB-INF/pages/viewcompanies.jsp");
        requestDispatcher.forward(request, response);
    }
}