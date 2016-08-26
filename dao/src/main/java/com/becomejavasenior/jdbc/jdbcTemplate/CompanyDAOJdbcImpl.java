package com.becomejavasenior.jdbc.jdbcTemplate;

import com.becomejavasenior.entity.Company;
import com.becomejavasenior.entity.Tag;
import com.becomejavasenior.entity.User;
import com.becomejavasenior.jdbc.entity.CompanyDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;

public class CompanyDAOJdbcImpl extends GenericDAOJdbc<Company> implements CompanyDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO company (name, phone, email, address, responsible_user_id," +
            " web, deleted, created_by_id, date_create) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE company SET name = ?, phone = ?, email = ?, address = ?, responsible_user_id = ?," +
            " web = ?, deleted = ?, created_by_id = ?, date_create = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, name, phone, email, address, responsible_user_id, web," +
            " created_by_id, date_create\nFROM company WHERE NOT deleted";
    private final String INSERT_COMPANY_TAG_SQL = "INSERT INTO contact_company_tag (tag_id, company_id) VALUES (?, ?)";

    private DataSource dataSource;

    public CompanyDAOJdbcImpl(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public int insert(Company company) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (company.getId() != 0) {
            throw new DatabaseException("company id must be obtained from DB");
        }
        int id;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int insertCount = jdbcTemplate.update(connection -> {
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, company.getName());
            statement.setString(2, company.getPhone());
            statement.setString(3, company.getEmail());
            statement.setString(4, company.getAddress());
            statement.setObject(5, company.getResponsibleUser() == null ? null : company.getResponsibleUser().getId(), Types.INTEGER);
            statement.setString(6, company.getWeb());
            statement.setBoolean(7, company.isDelete());
            statement.setObject(8, company.getCreator() == null ? null : company.getCreator().getId(), Types.INTEGER);
            statement.setTimestamp(9, new Timestamp(company.getDateCreate() == null ? System.currentTimeMillis() : company.getDateCreate().getTime()));
            return statement;
            }, keyHolder);

            if (insertCount == 1){
                id = keyHolder.getKey().intValue();
                company.setId(id);
            } else {
                throw new DatabaseException("Can't get company id from database.");
            }
            //logger.log(Level.INFO, "INSERT NEW COMPANY " + company.toString());
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "company"/*, logger*/);
    }

    @Override
    public void update(Company company) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (company.getId() == 0) {
            throw new DatabaseException("company must be created before update");
        }

        jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(UPDATE_SQL);

                    statement.setString(1, company.getName());
                    statement.setString(2, company.getPhone());
                    statement.setString(3, company.getEmail());
                    statement.setString(4, company.getAddress());
                    statement.setInt(5, company.getResponsibleUser().getId());
                    statement.setString(6, company.getWeb());
                    statement.setBoolean(7, company.isDelete());
                    statement.setInt(8, company.getCreator().getId());
                    statement.setTimestamp(9, new Timestamp(company.getDateCreate().getTime()));
                    statement.setInt(10, company.getId());
                    return statement;
                });
            //logger.log(Level.INFO, "UPDATE COMPANY " + company.toString());
    }

    @Override
    public List<Company> getAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_SQL, new CompanyRawMapper());
    }

    @Override
    public Company getById(int id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", Company.class, new CompanyRawMapper());
    }

    @Override
    public void companyTag(Company company, Tag tag) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(INSERT_COMPANY_TAG_SQL);

                    statement.setInt(1, tag.getId());
                    statement.setInt(2, company.getId());
                    return statement;
                });
            //logger.log(Level.INFO, "INSERT NEW COMPANY " + company.toString());
    }

    private static class CompanyRawMapper implements RowMapper<Company>{
        public Company mapRow(final ResultSet resultSet, final int i) throws SQLException{
            Company company = new Company();
            User responsibleUser = new User();
            User creator = new User();

            company.setId(resultSet.getInt(FIELD_ID));
            company.setName(resultSet.getString(FIELD_NAME));
            company.setPhone(resultSet.getString("phone"));
            company.setEmail(resultSet.getString("email"));
            company.setAddress(resultSet.getString("address"));
            company.setResponsibleUser(responsibleUser);
            responsibleUser.setId(resultSet.getInt("responsible_user_id"));
            company.setWeb(resultSet.getString("web"));
            company.setDelete(false);
            company.setCreator(creator);
            creator.setId(resultSet.getInt("created_by_id"));
            company.setDateCreate(resultSet.getTimestamp("date_create"));
            return company;
        }
    }
}