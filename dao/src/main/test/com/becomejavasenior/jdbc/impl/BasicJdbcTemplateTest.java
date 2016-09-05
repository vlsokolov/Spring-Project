package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.jdbc.entity.*;
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

    @Autowired
    protected DealDAO dealDAO;

    @Autowired
    protected FileDAO fileDAO;

    @Autowired
    protected LanguageDAO languageDAO;

    @Autowired
    protected NoteDAO noteDAO;

    @Autowired
    protected RightsDAO rightsDAO;

    @Autowired
    protected StageDAO stageDAO;

    @Autowired
    protected TagDAO tagDAO;

    @Autowired
    protected TaskDAO taskDAO;

    @Autowired
    protected VisitHistoryDAO visitHistoryDAO;

    @Autowired
    protected CurrencyDAO currencyDAO;
}
