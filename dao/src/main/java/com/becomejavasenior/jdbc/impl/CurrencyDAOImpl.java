package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.Currency;
import com.becomejavasenior.jdbc.entity.CurrencyDAO;
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
public class CurrencyDAOImpl extends AbstractDAO<Currency> implements CurrencyDAO {

    private static final String INSERT_SQL = "INSERT INTO currency (id, name, active, deleted) " +
            "VALUES ((SELECT CASE WHEN max(id) ISNULL THEN 1 ELSE max(id) + 1 END FROM currency), ?, ?, FALSE)";
    private static final String UPDATE_SQL = "UPDATE currency SET name = ?, active = ?, deleted = ? WHERE id = ?";
    private static final String SELECT_SQL = "SELECT id, name, active FROM currency WHERE (deleted ISNULL OR NOT deleted)";

    private static final String FIELD_ACTIVE = "active";

    private static final String TABLE_NAME = "currency";
    private final String className = getClass().getSimpleName().concat(": ");

    @Override
    public int insert(Currency currency) {

        if (currency.getId() != 0) {
            throw new DatabaseException(className + ERROR_ID_MUST_BE_FROM_DBMS + TABLE_NAME + ERROR_GIVEN_ID + currency.getId());
        }

        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setString(1, currency.getName());
            statement.setBoolean(2, currency.isActive());
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        currency.setId(id);
        return id;
    }

    @Override
    public void update(Currency currency) {

        if (currency.getId() == 0) {
            throw new DatabaseException("currency must be created before update");
        }

        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setString(1, currency.getName());
            statement.setBoolean(2, currency.isActive());
            statement.setBoolean(3, currency.isDelete());
            statement.setInt(4, currency.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public void delete(int id) {
        delete(id, TABLE_NAME);
    }

    @Override
    public List<Currency> getAll() {
       return jdbcTemplate.query(SELECT_SQL, CurrencyRowMapper);
    }

    @Override
    public Currency getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_SQL + " AND id = ?", CurrencyRowMapper, id);
    }

    private static final RowMapper<Currency> CurrencyRowMapper = (resultSet, i) -> {
                Currency currency = new Currency();
                currency.setId(resultSet.getInt(FIELD_ID));
                currency.setName(resultSet.getString(FIELD_NAME));
                currency.setActive(resultSet.getBoolean(FIELD_ACTIVE));
                currency.setDelete(false);
                return currency;
    };
}
