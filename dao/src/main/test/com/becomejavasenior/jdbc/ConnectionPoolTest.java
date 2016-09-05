package com.becomejavasenior.jdbc;

import com.becomejavasenior.jdbc.exceptions.DatabaseException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:dao-context.xml")
public class ConnectionPoolTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void openConnectionTest() throws DatabaseException, SQLException {
        Connection connection = dataSource.getConnection();
        Assert.assertNotNull("connection does not exist", connection);
        Assert.assertFalse("connection must not be closed", connection.isClosed());
        Assert.assertFalse("connection must not be read only", connection.isReadOnly());
        connection.close();
        Assert.assertTrue("finally connection must be closed", connection.isClosed());
    }
}
