package com.becomejavasenior.jdbc.impl;

import com.becomejavasenior.entity.*;
import com.becomejavasenior.jdbc.entity.FileDAO;
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
public class FileDAOImpl extends AbstractDAO<File> implements FileDAO {

    //private final static Logger logger = Logger.getLogger(CompanyDAOImpl.class.getName());

    private static final String INSERT_SQL = "INSERT INTO attached_file (created_by_id, date_create, filename, filesize, deleted," +
            " url_file, file, contact_id, company_id, deal_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_SQL = "UPDATE attached_file SET created_by_id = ?, date_create = ?, filename = ?, filesize = ?, deleted = ?," +
            " url_file = ?, file = ?, contact_id = ?, company_id = ?, deal_id = ? WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, created_by_id, date_create, filename, filesize," +
            " url_file, file, contact_id, company_id, deal_id FROM attached_file WHERE NOT deleted";

    @Override
    public int insert(File file) {

        if (file.getId() != 0) {
            throw new DatabaseException("file id must be obtained from DB");
        }
        PreparedStatementCreator preparedStatementCreator = connection -> {
            PreparedStatement statement = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            statement.setInt(1, file.getCreator().getId());
            statement.setTimestamp(2, new java.sql.Timestamp(file.getDateCreate() == null ? System.currentTimeMillis() : file.getDateCreate().getTime()));
            statement.setString(3, file.getFileName());
            statement.setInt(4, file.getFileSize());
            statement.setBoolean(5, file.isDelete());
            statement.setString(6, file.getUrlFile());
            statement.setBytes(7, file.getFile());
            statement.setObject(8, file.getContact() == null ? null : file.getContact().getId(), Types.INTEGER);
            statement.setObject(9, file.getCompany() == null ? null : file.getCompany().getId(), Types.INTEGER);
            statement.setObject(10, file.getDeal() == null ? null : file.getDeal().getId(), Types.INTEGER);
            return statement;
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(preparedStatementCreator, keyHolder);
        int id = (int) keyHolder.getKey().longValue();
        return id;
    }

    @Override
    public void delete(int id) {
        delete(id, "attached_file"/*, logger*/);
    }

    @Override
    public void update(File file) {

        if (file.getId() == 0) {
            throw new DatabaseException("file must be created before update");
        }
        PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setInt(1, file.getCreator().getId());
            statement.setTimestamp(2, new java.sql.Timestamp(file.getDateCreate().getTime()));
            statement.setString(3, file.getFileName());
            statement.setInt(4, file.getFileSize());
            statement.setBoolean(5, file.isDelete());
            statement.setString(6, file.getUrlFile());
            statement.setBytes(7, file.getFile());
            statement.setObject(8, file.getContact() == null ? null : file.getContact().getId(), Types.INTEGER);
            statement.setObject(9, file.getCompany() == null ? null : file.getCompany().getId(), Types.INTEGER);
            statement.setObject(10, file.getDeal() == null ? null : file.getDeal().getId(), Types.INTEGER);
            statement.setInt(11, file.getId());
        };
        jdbcTemplate.update(UPDATE_SQL, preparedStatementSetter);
    }

    private final static RowMapper<File> FileRowMapper = (resultSet, i) -> {
        File file = new File();
        User creator = new User();

        file.setId(resultSet.getInt(FIELD_ID));
        creator.setId(resultSet.getInt("created_by_id"));
        file.setCreator(creator);
        file.setDateCreate(resultSet.getTimestamp("date_create"));
        file.setFileName(resultSet.getString("filename"));
        file.setFileSize(resultSet.getInt("filesize"));
        file.setDelete(false);
        file.setUrlFile(resultSet.getString("url_file"));
        file.setFile(resultSet.getBytes("file"));
        if (resultSet.getObject("contact_id") != null) {
            Contact contact = new Contact();
            contact.setId(resultSet.getInt("contact_id"));
            file.setContact(contact);
        }
        if (resultSet.getObject("company_id") != null) {
            Company company = new Company();
            company.setId(resultSet.getInt("company_id"));
            file.setCompany(company);
        }
        if (resultSet.getObject("deal_id") != null) {
            Deal deal = new Deal();
            deal.setId(resultSet.getInt("deal_id"));
            file.setDeal(deal);
        }
        return file;
    };
    @Override
    public List<File> getAll() {
        return jdbcTemplate.query(SELECT_ALL_SQL, FileRowMapper);
    }

    @Override
    public File getById(int id) {
        return jdbcTemplate.queryForObject(SELECT_ALL_SQL + " AND id = ?", FileRowMapper, id);
    }
}
