package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.jdbc.entity.CompanyDAO;
import com.becomejavasenior.jdbc.entity.ContactDAO;
import com.becomejavasenior.jdbc.entity.UserDAO;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:dao-context.xml")
public class BasicJdbcTemplateTest {

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected CompanyDAO companyDAO;

    @Autowired
    protected ContactDAO contactDAO;

    @Autowired
    protected UserDAO userDAO;
}
