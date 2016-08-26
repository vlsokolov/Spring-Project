package com.becomejavasenior.service;

public interface ServiceFactory {

    CompanyService getCompanyService();

    ContactService getContactService();
}