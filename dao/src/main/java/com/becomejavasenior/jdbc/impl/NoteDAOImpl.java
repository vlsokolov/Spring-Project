package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.NoteDAO;
import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.List;
//import java.util.logging.Level;
//import java.util.logging.Logger;

@Repository
public class NoteDAOImpl extends AbstractDAO<Note> implements NoteDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO note (created_by_id, note, date_create, deleted," +
            " deal_id, company_id, contact_id) VALUES (?, ?, ?, FALSE, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE note SET created_by_id = ?, note = ?, date_create = ?, deleted = ?," +
            " deal_id = ?, company_id = ?, contact_id = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, note, created_by_id, date_create, deal_id, company_id, contact_id\n" +
            "FROM note WHERE NOT deleted";

    @Override
    public int insert(Note note) {

        if (note.getId() != 0) {
            throw new DatabaseException("note id must be obtained from DB");
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setInt(1, note.getCreator().getId());
            statement.setString(2, note.getNote());
            statement.setTimestamp(3, new java.sql.Timestamp(note.getDateCreate() == null ? System.currentTimeMillis() : note.getDateCreate().getTime()));
            statement.setObject(4, note.getDeal() == null ? null : note.getDeal().getId(), Types.INTEGER);
            statement.setObject(5, note.getCompany() == null ? null : note.getCompany().getId(), Types.INTEGER);
            statement.setObject(6, note.getContact() == null ? null : note.getContact().getId(), Types.INTEGER);
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        note.setId(id);
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "note"/*, logger*/);
    }

    @Override
    public void update(Note note) {

        if (note.getId() == 0) {
            throw new DatabaseException("note must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, note.getCreator().getId());
            statement.setString(2, note.getNote());
            statement.setTimestamp(3, new java.sql.Timestamp(note.getDateCreate().getTime()));
            statement.setBoolean(4, note.isDelete());
            statement.setObject(5, note.getDeal() == null ? null : note.getDeal().getId(), Types.INTEGER);
            statement.setObject(6, note.getCompany() == null ? null : note.getCompany().getId(), Types.INTEGER);
            statement.setObject(7, note.getContact() == null ? null : note.getContact().getId(), Types.INTEGER);
            statement.setInt(8, note.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    @Override
    public List<Note> getAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, NoteRowMapper);
    }

    @Override
    public Note getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", NoteRowMapper, id);
    }

    private static final RowMapper<Note> NoteRowMapper = (resultSet, i) -> {
        Note note = new Note();
        User creator = new User();

        note.setId(resultSet.getInt(FIELD_ID));
        creator.setId(resultSet.getInt("created_by_id"));
        note.setCreator(creator);
        note.setNote(resultSet.getString("note"));
        note.setDateCreate(resultSet.getTimestamp("date_create"));
        note.setDelete(false);
        if (resultSet.getObject("deal_id") != null) {
            Deal deal = new Deal();
            deal.setId(resultSet.getInt("deal_id"));
            note.setDeal(deal);
        }
        if (resultSet.getObject("company_id") != null) {
            Company company = new Company();
            company.setId(resultSet.getInt("company_id"));
            note.setCompany(company);
        }
        if (resultSet.getObject("contact_id") != null) {
            Contact contact = new Contact();
            contact.setId(resultSet.getInt("contact_id"));
            note.setContact(contact);
        }
        return note;
    };
}
