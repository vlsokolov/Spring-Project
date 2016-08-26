package com.becomejavasenior.jdbc;

import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class ConnectionPoolTest {

    @Autowired
    private BasicDataSource dataSource;

    @Test
    public void openConnectionTest() throws DatabaseException, SQLException {
        Connection connection = ConnectionPool.getConnection();
        Assert.assertNotNull("connection does not exist", connection);
        Assert.assertFalse("connection must not be closed", connection.isClosed());
        Assert.assertFalse("connection must not be read only", connection.isReadOnly());
        connection.close();
        Assert.assertTrue("finally connection must be closed", connection.isClosed());
    }
}
