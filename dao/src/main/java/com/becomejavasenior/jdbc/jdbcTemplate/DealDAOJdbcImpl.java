package com.becomejavasenior.jdbc.jdbcTemplate;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.DealDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;


public class DealDAOJdbcImpl extends GenericDAOJdbc<Deal> implements DealDAO {

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

    private DataSource dataSource;

    public DealDAOJdbcImpl(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public List<Deal> getDealsForList() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_DEALS_FOR_LIST, new DealForListRowMapper());
    }

    @Override
    public List<Deal> getDealsByStage(String stage) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_DEAL_BY_STAGE, new DealByStageRowMapper());
    }

    @Override
    public List<Contact> getContactsByDealName(String dealName) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_CONTACT, new ContactByDealNameRowMapper());
    }

    @Override
    public List<Stage> getAllStage() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_STAGES, new StagesRowMapper());
    }

    private static class StagesRowMapper implements RowMapper<Stage>{
        public Stage mapRow(final ResultSet resultSet, final int i) throws SQLException{
            Stage stage = new Stage();
            stage.setId(resultSet.getInt("id"));
            stage.setName(resultSet.getString("name"));
            return stage;
        }
    }
    @Override
    public int insert(Deal deal) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (deal.getId() != 0) {
            throw new DatabaseException("deal id must be obtained from DB");
        }
        int id;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int insertCount = jdbcTemplate.update(connection -> {
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);

                statement.setInt(1, deal.getStage().getId());
                statement.setObject(2, deal.getResponsibleUser() == null ? null : deal.getResponsibleUser().getId());
                statement.setInt(3, deal.getCompany().getId());
                statement.setInt(4, deal.getCreator().getId());
                statement.setString(5, deal.getName());
                statement.setBigDecimal(6, deal.getAmount());
                statement.setBoolean(7, deal.isDelete());
                statement.setTimestamp(8, new Timestamp(deal.getDateCreate() == null ? System.currentTimeMillis() : deal.getDateCreate().getTime()));
                statement.setObject(9, deal.getPrimaryContact() == null ? null : deal.getPrimaryContact().getId(), Types.INTEGER);
                return statement;
            }, keyHolder);

            if (insertCount == 1){
                id = keyHolder.getKey().intValue();
                deal.setId(id);
            } else {
                throw new DatabaseException("Can't get deal id from database.");
            }
            //logger.log(Level.INFO, "INSERT NEW DEAL " + deal.toString());

            if (deal.getContacts() != null && deal.getContacts().size() > 0) {
            for (Contact contact : deal.getContacts()) {
                addContactToDeal(deal, contact);
            }
        }
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "deal"/*, logger*/);
    }

    @Override
    public void update(Deal deal) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (deal.getId() == 0) {
            throw new DatabaseException("deal must be created before update");
        }

        jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(UPDATE_SQL);

                statement.setInt(1, deal.getStage().getId());
                statement.setObject(2, deal.getResponsibleUser() == null ? null : deal.getResponsibleUser().getId());
                statement.setInt(3, deal.getCompany().getId());
                statement.setInt(4, deal.getCreator().getId());
                statement.setString(5, deal.getName());
                statement.setBigDecimal(6, deal.getAmount());
                statement.setBoolean(7, deal.isDelete());
                statement.setTimestamp(8, new Timestamp(deal.getDateCreate().getTime()));
                statement.setObject(9, deal.getPrimaryContact() == null ? null : deal.getPrimaryContact().getId(), Types.INTEGER);
                statement.setInt(10, deal.getId());
                return statement;
                });
            //logger.log(Level.INFO, "UPDATE DEAL " + deal.toString());
    }

    @Override
    public List<Deal> getAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_SQL, new DealAllRowMapper());
    }

    @Override
    public Deal getById(int id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", new Object[]{id}, new DealAllRowMapper());
    }

    @Override
    public void addContactToDeal(Deal deal, Contact contact) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        if (deal != null && deal.getId() > 0 && contact != null && contact.getId() > 0) {
            jdbcTemplate.update(connection -> {
                 PreparedStatement statement = connection.prepareStatement(INSERT_DEAL_CONTACT_SQL);
                    statement.setInt(1, deal.getId());
                    statement.setInt(2, contact.getId());
                    return statement;
                });
        }
    }

    @Override
    public Map<Integer, String> getStageDealsList() {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        Map<Integer, String> stageDeals = new HashMap<>();

        List<Stage> stages = jdbcTemplate.query(SELECT_ALL_STAGE_DEALS_SQL, new StagesRowMapper());

        for (Stage stage: stages){
            stageDeals.put(stage.getId(), stage.getName());
        }

        return stageDeals;
    }

    private static class DealForListRowMapper implements RowMapper<Deal>{
        @Override
        public Deal mapRow(final ResultSet resultSet, final int i) throws SQLException{

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
        }
    }

    private static class DealByStageRowMapper implements RowMapper<Deal>{
        public Deal mapRow(final ResultSet resultSet, final int i) throws SQLException{
            Deal deal = new Deal();
            Company company = new Company();

            deal.setId(resultSet.getInt("id"));
            deal.setName(resultSet.getString("name"));
            deal.setAmount(resultSet.getBigDecimal("amount"));
            company.setId(resultSet.getInt("companyId"));
            company.setName(resultSet.getString("companyName"));
            deal.setCompany(company);
            return deal;
        }
    }

    private static class ContactByDealNameRowMapper implements RowMapper<Contact>{
        public Contact mapRow(final ResultSet resultSet, final int i) throws SQLException{
            Contact contact = new Contact();
            contact.setId(resultSet.getInt("id"));
            contact.setName(resultSet.getString("name"));
            return contact;
        }
    }

    private static class DealAllRowMapper implements RowMapper<Deal>{
        public Deal mapRow(final ResultSet resultSet, final int i) throws SQLException{
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
        }
    }
}