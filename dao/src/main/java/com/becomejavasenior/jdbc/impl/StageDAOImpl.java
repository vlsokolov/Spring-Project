package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.Stage;
import com.becomejavasenior.jdbc.entity.StageDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.apache.commons.dbcp2.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

@Repository
public class StageDAOImpl extends AbstractDAO<Stage> implements StageDAO {

    private static final String INSERT_SQL = "INSERT INTO stage_deals (id, name, deleted) " +
            "VALUES ((SELECT CASE WHEN max(id) ISNULL THEN 1 ELSE max(id) + 1 END FROM stage_deals), ?, FALSE)";
    private static final String UPDATE_SQL = "UPDATE stage_deals SET name = ?, deleted = ? WHERE id = ?";
    private static final String SELECT_SQL = "SELECT id, name FROM stage_deals WHERE (deleted ISNULL OR NOT deleted)";

    private static final String TABLE_NAME = "stage_deals";

    private final String className = getClass().getSimpleName().concat(": ");

    @Autowired
    private DataSource dataSource;

    @Override
    public int insert(Stage stage) {

        if (stage.getId() != 0) {
            throw new DatabaseException(className + ERROR_ID_MUST_BE_FROM_DBMS + TABLE_NAME + ERROR_GIVEN_ID + stage.getId());
        }

        int id = selectStageByName(stage.getName());
        if (id > 0) {
            stage.setId(id);
            return id;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            insertStatement.setString(1, stage.getName());

            if (1 == insertStatement.executeUpdate() && insertStatement.getGeneratedKeys().next()) {
                id = insertStatement.getGeneratedKeys().getInt(FIELD_ID);
                stage.setId(id);
            } else {
                throw new DatabaseException(className + "Can't get stage id from database");
            }
        } catch (Exception e) {
            throw new DatabaseException(className + ERROR_PREPARING_INSERT + TABLE_NAME, e);
        }
        return id;
    }

    @Override
    public void update(Stage stage) {

        if (stage.getId() == 0) {
            throw new DatabaseException("stage must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setString(1, stage.getName());
            statement.setBoolean(2, stage.isDelete());
            statement.setInt(3, stage.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public void delete(int id) {
        delete(id, TABLE_NAME);
    }

    @Override
    public List<Stage> getAll() {
        return jdbcTemplate.query(SELECT_SQL, StageRowMapper);
    }

    @Override
    public Stage getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_SQL + " AND id = ?", StageRowMapper, id);
    }

    private static final RowMapper<Stage> StageRowMapper = (resultSet, i) -> {
        Stage stage = new Stage();
        stage.setId(resultSet.getInt(FIELD_ID));
        stage.setName(resultSet.getString(FIELD_NAME));
        stage.setDelete(false);
        return stage;
    };


    private int selectStageByName(String stageName) {

        if (stageName == null || stageName.isEmpty()) {
            throw new DatabaseException("deal stage name can't be empty");
        }

        int id = 0;
        ResultSet resultSet = null;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SQL + " AND name = ?")) {

            statement.setString(1, stageName);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt(FIELD_ID);
            }
        } catch (SQLException e) {
            throw new DatabaseException("can't check if stage already exist", e);
        } finally {
            if (resultSet != null) {
                Utils.closeQuietly(resultSet);
            }
        }
        return id;
    }
}
