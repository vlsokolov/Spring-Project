package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.Language;
import com.becomejavasenior.jdbc.entity.LanguageDAO;
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
public class LanguageDAOImpl extends AbstractDAO<Language> implements LanguageDAO {

    private static final String INSERT_SQL = "INSERT INTO language (id, name, code, deleted) " +
            "VALUES ((SELECT CASE WHEN max(id) ISNULL THEN 1 ELSE max(id) + 1 END FROM language), ?, ?, FALSE)";
    private static final String UPDATE_SQL = "UPDATE language SET name = ?, code = ?, deleted = ? WHERE id = ?";
    private static final String SELECT_SQL = "SELECT id, name, code FROM language WHERE (deleted ISNULL OR NOT deleted)";

    private static final String FIELD_CODE = "code";

    private static final String TABLE_NAME = "language";
    private final String className = getClass().getSimpleName().concat(": ");

    @Override
    public final int insert(Language language) {

        if (language.getId() != 0) {
            throw new DatabaseException(className + ERROR_ID_MUST_BE_FROM_DBMS + TABLE_NAME + ERROR_GIVEN_ID + language.getId());
        }

        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setString(1, language.getName());
            statement.setString(2, language.getLanguageCode());
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        return id;
    }

    @Override
    public void update(Language language) {

        if (language.getId() == 0) {
            throw new DatabaseException("language must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setString(1, language.getName());
            statement.setString(2, language.getLanguageCode());
            statement.setBoolean(3, language.isDelete());
            statement.setInt(4, language.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public void delete(int id) {
        delete(id, TABLE_NAME);
    }

    @Override
    public List<Language> getAll() {
        return jdbcTemplate.query(SELECT_SQL, LanguageRowMapper);
    }

    @Override
    public Language getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_SQL + " AND id = ?", LanguageRowMapper, id);
    }

    private static final RowMapper<Language> LanguageRowMapper = (resultSet, i) -> {
        Language language = new Language();
        language.setId(resultSet.getInt(FIELD_ID));
        language.setName(resultSet.getString(FIELD_NAME));
        language.setLanguageCode(resultSet.getString(FIELD_CODE));
        language.setDelete(false);
        return language;
    };
}
