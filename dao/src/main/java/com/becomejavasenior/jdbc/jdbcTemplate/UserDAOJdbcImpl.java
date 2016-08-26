package com.becomejavasenior.jdbc.jdbcTemplate;

import com.becomejavasenior.entity.Language;
import com.becomejavasenior.entity.User;
import com.becomejavasenior.jdbc.entity.UserDAO;
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

public class UserDAOJdbcImpl extends GenericDAOJdbc<User> implements UserDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO \"user\" (name, email, password, is_admin, phone, " +
            "mobile_phone, note, deleted, url, image, language_id) VALUES (?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE \"user\" SET name = ?, email = ?, password = ?, is_admin = ?, phone = ?," +
            " mobile_phone = ?, note = ?, deleted = ?, image = ?, url = ?, language_id = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, name, email, password, is_admin, phone, mobile_phone," +
            " note, image, url, language_id FROM \"user\" WHERE NOT deleted";

    private DataSource dataSource;

    public UserDAOJdbcImpl(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public int insert(User user) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (user.getId() != 0) {
            throw new DatabaseException("user id must be obtained from DB");
        }
        int id;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        int insertCount = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setBoolean(4, user.isAdmin());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getMobilePhone());
            statement.setString(7, user.getNote());
            statement.setString(8, user.getUrl());
            statement.setBytes(9, user.getImage());
            statement.setInt(10, user.getLanguage().getId());
            return statement;
        }, keyHolder);

            if (insertCount == 1){
                id = keyHolder.getKey().intValue();
                user.setId(id);
            } else {
                throw new DatabaseException("Can't get user id from database.");
            }
            //logger.log(Level.INFO, "INSERT NEW USER " + user.toString());
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "\"user\"");
    }

    @Override
    public void update(User user) {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        if (user.getId() == 0) {
            throw new DatabaseException("user must be created before update");
        }
        jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(UPDATE_SQL);

                    statement.setString(1, user.getName());
                    statement.setString(2, user.getEmail());
                    statement.setString(3, user.getPassword());
                    statement.setBoolean(4, user.isAdmin());
                    statement.setString(5, user.getPhone());
                    statement.setString(6, user.getMobilePhone());
                    statement.setString(7, user.getNote());
                    statement.setBoolean(8, user.isDelete());
                    statement.setBytes(9, user.getImage());
                    statement.setString(10, user.getUrl());
                    statement.setInt(11, user.getLanguage().getId());
                    statement.setInt(12, user.getId());
                    return statement;
                });
            //logger.log(Level.INFO, "UPDATE USER " + user.toString());
    }

    @Override
    public List<User> getAll() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(SELECT_ALL_SQL, new UserRawMapper());
    }

    @Override
    public User getById(int id) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", User.class, new UserRawMapper());
    }

    private static class UserRawMapper implements RowMapper<User>{
        public User mapRow(final ResultSet resultSet, final int i) throws SQLException{
            User user = new User();
            user.setId(resultSet.getInt(FIELD_ID));
            user.setName(resultSet.getString(FIELD_NAME));
            user.setEmail(resultSet.getString("email"));
            user.setPassword(resultSet.getString("password"));
            user.setAdmin(resultSet.getBoolean("is_admin"));
            user.setPhone(resultSet.getString("phone"));
            user.setMobilePhone(resultSet.getString("mobile_phone"));
            user.setNote(resultSet.getString("note"));
            user.setDelete(false);
            user.setImage(resultSet.getBytes("image"));
            user.setUrl(resultSet.getString("url"));
            if (resultSet.getObject("language_id") != null) {
                Language language = new Language();
                language.setId(resultSet.getInt("language_id"));
                user.setLanguage(language);
            }
            return user;
        }
    }
}