package com.becomejavasenior.service.impl;

import com.becomejavasenior.service.ServiceFactory;

public class ServiceFactoryImpl implements ServiceFactory {
    public CompanyServiceImpl getCompanyService() {
        return new CompanyServiceImpl();
    }

    public ContactServiceImpl getContactService() {
        return new ContactServiceImpl();
    }

    public DealServiceImpl getDealService() {
        return new DealServiceImpl();
    }

    public UserServiceImpl getUserService() {
        return new UserServiceImpl();
    }
}