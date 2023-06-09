package com.fpmislata.grup4pawparadise.database;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.*;
import java.util.List;
import javax.sql.DataSource;

public class JDBCUtil {

    private static DataSource datasource;
    private static final String HOST = System.getenv("DBHOST");
    private static final String NAME = System.getenv("DBNAME");
    private static final String USER = System.getenv("DBUSER");
    private static final String PASSWORD = System.getenv("DBPASSWD");

    public static DataSource getDataSource() {
        if (datasource == null) {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://" + HOST + ":3306/" + NAME);
            dataSource.setUsername(USER);
            dataSource.setPassword(PASSWORD);
            datasource = dataSource;
        }
        return datasource;
    }

    public static Connection open() {
        Connection connection;
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + HOST + ":3306/" + NAME,
                    USER,
                    PASSWORD);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException();
        }
    }

    public static void close(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSet select(Connection connection, String sql, List<Object> values) {
        try {
            PreparedStatement preparedStatement = setParameters(connection, sql, values);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean insert(Connection connection, String sql, List<Object> values) {
        try {
            PreparedStatement preparedStatement = setParameters(connection, sql, values);
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int update(Connection connection, String sql, List<Object> values) {
        try {
            PreparedStatement preparedStatement = setParameters(connection, sql, values);
            int numRows = preparedStatement.executeUpdate();
            return numRows;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static PreparedStatement setParameters(Connection connection, String sql, List<Object> values) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            if (values != null) {
                for (int i = 0; i < values.size(); i++) {
                    Object value = values.get(i);
                    preparedStatement.setObject(i + 1, value);
                }
            }
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean delete(Connection connection, String sql, List<Object> values) {
        try {
            PreparedStatement preparedStatement = setParameters(connection, sql, values);
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}