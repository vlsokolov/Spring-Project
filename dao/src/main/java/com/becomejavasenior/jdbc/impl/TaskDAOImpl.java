package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.TaskDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import com.becomejavasenior.jdbc.factory.PostgresDAOFactory;
import org.apache.commons.dbcp2.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;

@Repository
public class TaskDAOImpl extends AbstractDAO<Task> implements TaskDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO task (responsible_user_id, task_type_id, created_by_id," +
            " company_id, contact_id, deal_id, period, name, deleted, date_create, status_id, date_task, time_task)\n" +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE task SET responsible_user_id = ?, task_type_id = ?, created_by_id = ?," +
            " company_id = ?, contact_id = ?, deal_id = ?, period = ?, name = ?, deleted = ?, date_create = ?, status_id = ?," +
            " date_task = ?, time_task = ?\nWHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT task.id, period, responsible_user_id, created_by_id, task.name," +
            "  task_type.name as task_type_name, task_status.name as task_status_name," +
            "  date_create, company_id, contact_id, deal_id, date_task, time_task\n" +
            "FROM task\n" +
            "  LEFT JOIN task_type ON task.task_type_id = task_type.id\n" +
            "  LEFT JOIN task_status ON task.status_id = task_status.id\n" +
            "WHERE task.deleted = FALSE";

    private static final String TASK_STATUS_INSERT_SQL = "INSERT INTO task_status (id, name, deleted)\n" +
            "VALUES ((SELECT CASE WHEN max(id) ISNULL THEN 1 ELSE max(id) + 1 END FROM task_status), ?, FALSE)";
    private static final String TASK_STATUS_SELECT_SQL = "SELECT id, name FROM task_status WHERE NOT deleted";
    private static final String TASK_TYPE_INSERT_SQL = "INSERT INTO task_type (id, name, deleted)\n" +
            "VALUES ((SELECT CASE WHEN max(id) ISNULL THEN 1 ELSE max(id) + 1 END FROM task_type), ?, FALSE)";
    private static final String TASK_TYPE_SELECT_SQL = "SELECT id, name FROM task_type WHERE NOT deleted";

    private static final String FIELD_PERIOD = "period";
    private static final String FIELD_RESPONSIBLE_USER_ID = "responsible_user_id";
    private static final String FIELD_CREATED_BY_ID = "created_by_id";
    private static final String FIELD_DATE_CREATE = "date_create";
    private static final String FIELD_TASK_TYPE_NAME = "task_type_name";
    private static final String FIELD_TASK_STATUS_NAME = "task_status_name";
    private static final String FIELD_COMPANY_ID = "company_id";
    private static final String FIELD_CONTACT_ID = "contact_id";
    private static final String FIELD_DEAL_ID = "deal_id";
    private static final String FIELD_DATE_TASK = "date_task";
    private static final String FIELD_TIME_TASK = "time_task";

    @Autowired
    private DataSource datasource;

    @Override
    public int insert(Task task) {

        if (task.getId() != 0) {
            throw new DatabaseException("task id must be obtained from DB");
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setObject(1, task.getResponsibleUser() == null ? null : task.getResponsibleUser().getId(), Types.INTEGER);
            statement.setInt(2, insertOrSelectTaskType(task.getTaskType()));
            statement.setInt(3, task.getCreator().getId());
            statement.setObject(4, task.getCompany() == null ? null : task.getCompany().getId(), Types.INTEGER);
            statement.setObject(5, task.getContact() == null ? null : task.getContact().getId(), Types.INTEGER);
            statement.setObject(6, task.getDeal() == null ? null : task.getDeal().getId(), Types.INTEGER);
            statement.setInt(7, task.getPeriod().getId());
            statement.setString(8, task.getName());
            statement.setTimestamp(9, new java.sql.Timestamp(task.getDateCreate() == null ? System.currentTimeMillis() : task.getDateCreate().getTime()));
            statement.setInt(10, insertOrSelectTaskStatus(task.getStatus()));
            statement.setObject(11, task.getDateTask() == null ? null : new java.sql.Date(task.getDateTask().getTime()), Types.DATE);
            statement.setString(12, task.getTimeTask());
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "task"/*, logger*/);
    }

    @Override
    public void update(Task task) {

        if (task.getId() == 0) {
            throw new DatabaseException("task must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, task.getResponsibleUser().getId());
            statement.setInt(2, insertOrSelectTaskType(task.getTaskType()));
            statement.setInt(3, task.getCreator().getId());
            statement.setObject(4, task.getCompany() == null ? null : task.getCompany().getId(), Types.INTEGER);
            statement.setObject(5, task.getContact() == null ? null : task.getContact().getId(), Types.INTEGER);
            statement.setObject(6, task.getDeal() == null ? null : task.getDeal().getId(), Types.INTEGER);
            statement.setInt(7, task.getPeriod().getId());
            statement.setString(8, task.getName());
            statement.setBoolean(9, task.isDelete());
            statement.setTimestamp(10, new java.sql.Timestamp(task.getDateCreate().getTime()));
            statement.setInt(11, insertOrSelectTaskStatus(task.getStatus()));
            statement.setDate(12, new java.sql.Date(task.getDateTask().getTime()));
            statement.setString(13, task.getTimeTask());
            statement.setInt(14, task.getId());
        };

        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public List<Task> getAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, TaskRowMapper);
    }

    @Override
    public Task getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND task.id = ?", TaskRowMapper, id);
    }

    private static final RowMapper<Task> TaskRowMapper = (resultSet, i) -> {
        Task task = new Task();
        User responsibleUser = new User();
        User creator = new User();

        task.setId(resultSet.getInt(FIELD_ID));
        responsibleUser.setId(resultSet.getInt(FIELD_RESPONSIBLE_USER_ID));
        task.setResponsibleUser(responsibleUser);
        task.setTaskType(resultSet.getString(FIELD_TASK_TYPE_NAME));
        creator.setId(resultSet.getInt(FIELD_CREATED_BY_ID));
        task.setCreator(creator);
        if (resultSet.getObject(FIELD_COMPANY_ID) != null) {
            Company company = new Company();
            company.setId(resultSet.getInt(FIELD_COMPANY_ID));
            task.setCompany(company);
        }
        if (resultSet.getObject(FIELD_CONTACT_ID) != null) {
            Contact contact = new Contact();
            contact.setId(resultSet.getInt(FIELD_CONTACT_ID));
            task.setContact(contact);
        }
        if (resultSet.getObject(FIELD_DEAL_ID) != null) {
            Deal deal = new Deal();
            deal.setId(resultSet.getInt(FIELD_DEAL_ID));
            task.setDeal(deal);
        }
        task.setPeriod(TypeOfPeriod.getById(resultSet.getInt(FIELD_PERIOD)));
        task.setName(resultSet.getString(FIELD_NAME));
        task.setDelete(false);
        task.setDateCreate(resultSet.getTimestamp(FIELD_DATE_CREATE));
        task.setStatus(resultSet.getString(FIELD_TASK_STATUS_NAME));
        task.setDateTask(resultSet.getDate(FIELD_DATE_TASK));
        task.setTimeTask(resultSet.getString(FIELD_TIME_TASK));
        return task;
    };

    // task_status table maintenance
    private int insertOrSelectTaskStatus(String taskStatus) {

        if (taskStatus == null || taskStatus.isEmpty()) {
            throw new DatabaseException("task status can't be empty");
        }

        int id;
        ResultSet resultSet = null;
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = connection.prepareStatement(TASK_STATUS_SELECT_SQL + " AND name = ?")) {

            statement.setString(1, taskStatus);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt(FIELD_ID);
            } else {
                id = insertTaskStatus(taskStatus);
            }

        } catch (SQLException e) {
            throw new DatabaseException("can't get task status id", e);
        } finally {
            if (resultSet != null) {
                Utils.closeQuietly(resultSet);
            }
        }
        return id;
    }

    private int insertTaskStatus(String taskStatus) {
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(TASK_STATUS_INSERT_SQL, new String[]{"id"});
            statement.setString(1, taskStatus);
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        return id;
    }

    @Override
    public List<String> getAllTaskStatus() {
        return jdbcTemplate.query(TASK_STATUS_SELECT_SQL + " ORDER BY name", resultSet -> {
            List<String> statusList = new ArrayList<>();
            while (resultSet.next()) {
                statusList.add(resultSet.getString(FIELD_NAME));
            }
            return statusList;
        });
    }

    // task_type table maintenance
    private int insertOrSelectTaskType(String taskType) {

        if (taskType == null || taskType.isEmpty()) {
            throw new DatabaseException("task type can't be empty");
        }

        int id;
        ResultSet resultSet = null;
        try (Connection connection = datasource.getConnection();
             PreparedStatement statement = connection.prepareStatement(TASK_TYPE_SELECT_SQL + " AND name = ?")) {

            statement.setString(1, taskType);
            resultSet = statement.executeQuery();

            if (resultSet.next()) {
                id = resultSet.getInt(FIELD_ID);
            } else {
                id = insertTaskType(taskType);
            }

        } catch (SQLException e) {
            throw new DatabaseException("can't get task type id", e);
        } finally {
            if (resultSet != null) {
                Utils.closeQuietly(resultSet);
            }
        }
        return id;
    }

    private int insertTaskType(String taskType) {

        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(TASK_TYPE_INSERT_SQL, new String[]{"id"});
            statement.setString(1, taskType);
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        return id;
    }

    @Override
    public List<String> getAllTaskType() {

        return jdbcTemplate.query(TASK_TYPE_SELECT_SQL + " ORDER BY name", resultSet -> {
            List<String> typeList = new ArrayList<>();
            while (resultSet.next()) {
                typeList.add(resultSet.getString(FIELD_NAME));
            }
            return typeList;
        });
    }

    @Override
    public Map<Integer, String> getTaskTypeList() {
        return jdbcTemplate.query(TASK_TYPE_SELECT_SQL, resultSet -> {
        Map<Integer, String> taskTypes = new HashMap<>();
            while (resultSet.next()) {
                taskTypes.put(resultSet.getInt("id"), resultSet.getString("name"));
            }
        return taskTypes;
        });
    }
}