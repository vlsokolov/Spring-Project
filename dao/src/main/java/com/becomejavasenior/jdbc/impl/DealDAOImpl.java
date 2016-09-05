package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.DealDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;

@Repository
public class DealDAOImpl extends AbstractDAO<Deal> implements DealDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO deal (stage_id, responsible_user_id, company_id, created_by_id, " +
            "name, amount, deleted, date_create, primary_contact_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE deal SET stage_id = ?, responsible_user_id = ?, company_id = ?, created_by_id = ?, name = ?," +
            " amount = ?, deleted = ?, date_create = ?, primary_contact_id = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, name, stage_id, responsible_user_id," +
            " amount, company_id, date_create, created_by_id, primary_contact_id FROM deal WHERE NOT deleted";
    private static final String INSERT_DEAL_CONTACT_SQL = "INSERT INTO deal_contact (deal_id, contact_id) VALUES (?, ?)";
    private static final String SELECT_ALL_STAGE_DEALS_SQL = "SELECT id, name FROM stage_deals WHERE deleted = FALSE";
    private static final String SELECT_ALL_DEAL_BY_STAGE = "SELECT\n" +
            "  deal.id,\n" +
            "  deal.name,\n" +
            "  deal.amount,\n" +
            "  company.id AS companyId,\n" +
            "  company.name AS companyName\n" +
            "FROM deal\n" +
            "  JOIN company ON company.id = deal.company_id\n" +
            "WHERE deal.id IN (SELECT deal.id\n" +
            "                  FROM deal\n" +
            "                    JOIN stage_deals ON deal.stage_id = stage_deals.id WHERE stage_deals.name=?)";

    private static final String SELECT_ALL_CONTACT = "SELECT contact.id, contact.name FROM deal JOIN\n" +
            "deal_contact ON\n" +
            "deal.id=deal_contact.deal_id JOIN contact ON deal_contact.contact_id=contact.id\n" +
            "WHERE deal.name=?";
    private static final String SELECT_ALL_STAGES = "SELECT id, name FROM stage_deals LIMIT 4";

    private static final String SELECT_DEALS_FOR_LIST = "SELECT\n" +
            "  deal.name,\n" +
            "  deal.amount,\n" +
            "  stage_deals.name AS stage,\n" +
            "  contact.id AS contactId,\n" +
            "  contact.name AS contact,\n" +
            "  company.id AS companyId,\n" +
            "  company.name AS company\n" +
            "FROM deal\n" +
            "  JOIN stage_deals ON deal.stage_id = stage_deals.id\n" +
            "  JOIN contact ON deal.primary_contact_id = contact.id\n" +
            "  JOIN company ON contact.company_id = company.id\n";

    @Autowired
    private DataSource dataSource;

    private static final RowMapper<Deal> DealRowMapper = (resultSet, i) -> {
        Deal deal = new Deal();
        Company company = new Company();
        Contact contact = new Contact();
        Stage stage = new Stage();

        deal.setName(resultSet.getString("name"));
        deal.setAmount(resultSet.getBigDecimal("amount"));
        stage.setName(resultSet.getString("stage"));
        deal.setStage(stage);
        contact.setId(resultSet.getInt("contactId"));
        contact.setName(resultSet.getString("contact"));
        company.setId(resultSet.getInt("companyId"));
        company.setName(resultSet.getString("company"));
        contact.setCompany(company);
        deal.setPrimaryContact(contact);
        return deal;
    };


    @Override
    public List<Deal> getDealsForList() {
        return jdbcTemplate.query(SELECT_DEALS_FOR_LIST, DealRowMapper);
    }

    private static final RowMapper<Deal> DealForStageRowMapper = (resultSet, i) -> {
        Deal deal = new Deal();
        Company company = new Company();

        deal.setId(resultSet.getInt("id"));
        deal.setName(resultSet.getString("name"));
        deal.setAmount(resultSet.getBigDecimal("amount"));
        company.setId(resultSet.getInt("companyId"));
        company.setName(resultSet.getString("companyName"));
        deal.setCompany(company);
        return deal;
    };


    @Override
    public List<Deal> getDealsByStage(String stage) {
        return jdbcTemplate.query(SELECT_ALL_DEAL_BY_STAGE, DealForStageRowMapper);
    }

    private static final RowMapper<Contact> ContactRowMapper = (resultSet, i) -> {
        Contact contact = new Contact();
        contact.setId(resultSet.getInt("id"));
        contact.setName(resultSet.getString("name"));
        return contact;
    };

    @Override
    public List<Contact> getContactsByDealName(String dealName) {
        return jdbcTemplate.query(SELECT_ALL_CONTACT, ContactRowMapper);
    }

    private static final RowMapper<Stage> StageRowMapper = (resultSet, i) -> {
        Stage stage = new Stage();
        stage.setId(resultSet.getInt("id"));
        stage.setName(resultSet.getString("name"));
        return stage;
    };

    @Override
    public List<Stage> getAllStage() {
        return jdbcTemplate.query(SELECT_ALL_STAGES, StageRowMapper);
    }

    @Override
    public int insert(Deal deal) {

        if (deal.getId() != 0) {
            throw new DatabaseException("deal id must be obtained from DB");
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setInt(1, deal.getStage().getId());
            statement.setObject(2, deal.getResponsibleUser() == null ? null : deal.getResponsibleUser().getId());
            statement.setInt(3, deal.getCompany().getId());
            statement.setInt(4, deal.getCreator().getId());
            statement.setString(5, deal.getName());
            statement.setBigDecimal(6, deal.getAmount());
            statement.setBoolean(7, deal.isDelete());
            statement.setTimestamp(8, new java.sql.Timestamp(deal.getDateCreate() == null ? System.currentTimeMillis() : deal.getDateCreate().getTime()));
            statement.setObject(9, deal.getPrimaryContact() == null ? null : deal.getPrimaryContact().getId(), Types.INTEGER);
            return statement;
        };

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        deal.setId(id);
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "deal"/*, logger*/);
    }

    @Override
    public void update(Deal deal) {

        if (deal.getId() == 0) {
            throw new DatabaseException("deal must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, deal.getStage().getId());
            statement.setObject(2, deal.getResponsibleUser() == null ? null : deal.getResponsibleUser().getId());
            statement.setInt(3, deal.getCompany().getId());
            statement.setInt(4, deal.getCreator().getId());
            statement.setString(5, deal.getName());
            statement.setBigDecimal(6, deal.getAmount());
            statement.setBoolean(7, deal.isDelete());
            statement.setTimestamp(8, new java.sql.Timestamp(deal.getDateCreate().getTime()));
            statement.setObject(9, deal.getPrimaryContact() == null ? null : deal.getPrimaryContact().getId(), Types.INTEGER);
            statement.setInt(10, deal.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    private static final RowMapper<Deal> AllDealRowMapper = (resultSet, i) -> {
        Deal deal = new Deal();
        User responsibleUser = new User();
        User creator = new User();
        Company company = new Company();
        Stage stage = new Stage();

        deal.setId(resultSet.getInt(FIELD_ID));
        responsibleUser.setId(resultSet.getInt("responsible_user_id"));
        deal.setResponsibleUser(responsibleUser);
        company.setId(resultSet.getInt("company_id"));
        deal.setCompany(company);
        creator.setId(resultSet.getInt("created_by_id"));
        deal.setCreator(creator);
        deal.setName(resultSet.getString(FIELD_NAME));
        deal.setAmount(resultSet.getBigDecimal("amount"));
        deal.setDelete(false);
        deal.setDateCreate(resultSet.getTimestamp("date_create"));
        stage.setId(resultSet.getInt("stage_id"));
        deal.setStage(stage);
        if (resultSet.getObject("primary_contact_id") != null) {
            Contact primaryContact = new Contact();
            primaryContact.setId(resultSet.getInt("primary_contact_id"));
            deal.setPrimaryContact(primaryContact);
        }
        return deal;
    };
    @Override
    public List<Deal> getAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, AllDealRowMapper);
    }

    @Override
    public Deal getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", AllDealRowMapper, id);
    }

    @Override
    public void addContactToDeal(Deal deal, Contact contact) {
        if (deal != null && deal.getId() > 0 && contact != null && contact.getId() > 0) {
            PreparedStatementSetter preparedStatementSetter = statement -> {
                statement.setInt(1, deal.getId());
                statement.setInt(2, contact.getId());
            };
            jdbcTemplate.update(INSERT_DEAL_CONTACT_SQL, preparedStatementSetter);
        }
    }

    @Override
    public Map<Integer, String> getStageDealsList() {
        return jdbcTemplate.query(SELECT_ALL_STAGE_DEALS_SQL, rs -> {
            Map<Integer,String> stageDeals= new HashMap<>();
            while(rs.next()){
                stageDeals.put(rs.getInt("id"),rs.getString("name"));
            }
            return stageDeals;
        });
    }
}