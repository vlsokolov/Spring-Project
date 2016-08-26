package com.becomejavasenior.jdbc.jdbcTemplate;

import com.becomejavasenior.jdbc.entity.GenericDAO;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

abstract class GenericDAOJdbc<T> extends JdbcDaoSupport implements GenericDAO<T> {

    static final String ERROR_PARSE_RESULT_SET = "error while parsing result set for ";
    static final String ERROR_PREPARING_INSERT = "error while preparing INSERT statement for ";
    static final String ERROR_PREPARING_UPDATE = "error while preparing UPDATE statement for ";
    static final String ERROR_ID_MUST_BE_FROM_DBMS = "id must be obtained from DB, cannot create record in ";
    static final String ERROR_GIVEN_ID = ", given id value is: ";
    static final String ERROR_SELECT_ALL = "error while reading all records";
    static final String ERROR_SELECT_1 = "error while reading record by key";

    static final String FIELD_ID = "id";
    static final String FIELD_NAME = "name";

    public void initDataSource(DataSource dataSource) {
        setDataSource(dataSource);
    }

    @Override
    public abstract int insert(T o);

    @Override
    public abstract void update(T t);

    @Override
    public abstract List<T> getAll();

    @Override
    public abstract T getById(int id);

    public void delete(int id, String tableName) {

        final String DELETE_SQL = "UPDATE " + tableName + " SET deleted = TRUE WHERE id = ?";

        getJdbcTemplate().update(DELETE_SQL, id);
    }
}