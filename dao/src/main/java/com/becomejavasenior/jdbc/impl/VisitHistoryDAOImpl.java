package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.VisitHistoryDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;


import java.sql.*;
import java.util.List;

@Repository
public class VisitHistoryDAOImpl extends AbstractDAO<VisitHistory> implements VisitHistoryDAO {

    private static final String INSERT_SQL = "INSERT INTO visit_history (user_id, date_create, ip_address," +
            " browser, deleted)\nVALUES (?, ?, ?, ?, false)";
    private static final String UPDATE_SQL = "UPDATE visit_history SET user_id = ?, date_create = ?, ip_address = ?," +
            " browser = ?, deleted = ?\nWHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, user_id, date_create, ip_address, browser\n" +
            "FROM visit_history WHERE NOT deleted";

    private static final String TABLE_NAME = "visit_history";

    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_DATE_CREATE = "date_create";
    private static final String FIELD_IP_ADDRESS = "ip_address";
    private static final String FIELD_BROWSER = "browser";

    private final String className = getClass().getSimpleName().concat(": ");

    @Override
    public int insert(VisitHistory visitHistory) {

        if (visitHistory.getId() != 0) {
            throw new DatabaseException(className + ERROR_ID_MUST_BE_FROM_DBMS + TABLE_NAME + ERROR_GIVEN_ID + visitHistory.getId());
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setInt(1, visitHistory.getUser().getId());
            statement.setTimestamp(2, new java.sql.Timestamp(visitHistory.getDateCreate() == null ? System.currentTimeMillis() : visitHistory.getDateCreate().getTime()));
            statement.setString(3, visitHistory.getIpAddress());
            statement.setString(4, visitHistory.getBrowser());
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        visitHistory.setId(id);
        return id;
    }

    @Override
    public void update(VisitHistory visitHistory) {

        if (visitHistory.getId() == 0) {
            throw new DatabaseException("VisitHistory must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, visitHistory.getUser().getId());
            statement.setTimestamp(2, new java.sql.Timestamp(visitHistory.getDateCreate().getTime()));
            statement.setString(3, visitHistory.getIpAddress());
            statement.setString(4, visitHistory.getBrowser());
            statement.setBoolean(5, visitHistory.isDelete());
            statement.setInt(6, visitHistory.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public void delete(int id) {
        delete(id, TABLE_NAME);
    }

    @Override
    public List<VisitHistory> getAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, VisitHistoryRowMapper);
    }

    @Override
    public VisitHistory getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", VisitHistoryRowMapper, id);
    }

    private static final RowMapper<VisitHistory> VisitHistoryRowMapper = (resultSet, i) -> {
        VisitHistory visitHistory = new VisitHistory();
        visitHistory.setId(resultSet.getInt(FIELD_ID));
        User user = new User();
        user.setId(resultSet.getInt(FIELD_USER_ID));
        visitHistory.setUser(user);
        visitHistory.setDateCreate(resultSet.getTimestamp(FIELD_DATE_CREATE));
        visitHistory.setIpAddress(resultSet.getString(FIELD_IP_ADDRESS));
        visitHistory.setBrowser(resultSet.getString(FIELD_BROWSER));
        visitHistory.setDelete(false);
        return visitHistory;
    };
}