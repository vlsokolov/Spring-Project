package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.Rights;
import com.becomejavasenior.entity.SubjectType;
import com.becomejavasenior.entity.User;
import com.becomejavasenior.jdbc.entity.RightsDAO;
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
public class RightsDAOImpl extends AbstractDAO<Rights> implements RightsDAO {

    private static final String INSERT_SQL = "INSERT INTO rights (user_id, subject_type, subject_type_create," +
            " subject_type_read, subject_type_delete, subject_type_change, subject_type_export, deleted)\n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, FALSE)";
    private static final String UPDATE_SQL = "UPDATE rights SET user_id = ?, subject_type = ?," +
            " subject_type_create = ?, subject_type_read = ?, subject_type_delete = ?, subject_type_change = ?," +
            " subject_type_export = ?, deleted = ?\nWHERE id = ?;";
    private static final String SELECT_SQL = "SELECT id, user_id, subject_type, subject_type_create," +
            " subject_type_read, subject_type_delete, subject_type_change, subject_type_export\n" +
            "FROM rights WHERE NOT deleted";

    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_SUBJECT_TYPE = "subject_type";
    private static final String FIELD_SUBJECT_TYPE_CREATE = "subject_type_create";
    private static final String FIELD_SUBJECT_TYPE_READ = "subject_type_read";
    private static final String FIELD_SUBJECT_TYPE_DELETE = "subject_type_delete";
    private static final String FIELD_SUBJECT_TYPE_CHANGE = "subject_type_change";
    private static final String FIELD_SUBJECT_TYPE_EXPORT = "subject_type_export";

    private static final String TABLE_NAME = "rights";

    private final String className = getClass().getSimpleName().concat(": ");

    @Override
    public int insert(Rights rights) {

        if (rights.getId() != 0) {
            throw new DatabaseException(className + ERROR_ID_MUST_BE_FROM_DBMS + TABLE_NAME + ERROR_GIVEN_ID + rights.getId());
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setObject(1, rights.getUser() == null ? null : rights.getUser().getId(), Types.INTEGER);
            statement.setObject(2, rights.getSubjectType() == null ? null : rights.getSubjectType().getId(), Types.INTEGER);
            statement.setBoolean(3, rights.isCreate());
            statement.setBoolean(4, rights.isRead());
            statement.setBoolean(5, rights.isDelete());
            statement.setBoolean(6, rights.isChange());
            statement.setBoolean(7, rights.isExport());
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        rights.setId(id);
        return id;
    }

    @Override
    public void update(Rights rights) {

        if (rights.getId() == 0) {
            throw new DatabaseException("rights must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, rights.getUser().getId());
            statement.setInt(2, rights.getSubjectType().getId());
            statement.setBoolean(3, rights.isCreate());
            statement.setBoolean(4, rights.isRead());
            statement.setBoolean(5, rights.isDelete());
            statement.setBoolean(6, rights.isChange());
            statement.setBoolean(7, rights.isExport());
            statement.setBoolean(8, rights.isDeleted());
            statement.setInt(9, rights.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public void delete(int id) {
        delete(id, TABLE_NAME);
    }

    @Override
    public List<Rights> getAll() {
        return jdbcTemplate.query(SELECT_SQL, RightsRowMapper);
    }

    @Override
    public Rights getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_SQL + " AND id = ?", RightsRowMapper, id);
    }

    private static final RowMapper<Rights> RightsRowMapper = (resultSet, i) -> {
        Rights rights = new Rights();
        rights.setId(resultSet.getInt(FIELD_ID));
        User user = new User();
        user.setId(resultSet.getInt(FIELD_USER_ID));
        rights.setUser(user);
        rights.setSubjectType(SubjectType.getById(resultSet.getInt(FIELD_SUBJECT_TYPE)));
        rights.setCreate(resultSet.getBoolean(FIELD_SUBJECT_TYPE_CREATE));
        rights.setRead(resultSet.getBoolean(FIELD_SUBJECT_TYPE_READ));
        rights.setDelete(resultSet.getBoolean(FIELD_SUBJECT_TYPE_DELETE));
        rights.setChange(resultSet.getBoolean(FIELD_SUBJECT_TYPE_CHANGE));
        rights.setExport(resultSet.getBoolean(FIELD_SUBJECT_TYPE_EXPORT));
        rights.setDeleted(false);
        return rights;
    };

    @Override
    public List<Rights> getRightsByUserId(int userId) {
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, userId);
        };
        return jdbcTemplate.query(SELECT_SQL + " AND user_id = ?", RightsRowMapper, preparedStatementSetter);
    }
}
