package com.example;

import com.example.DAO.ProjectDAO;
import com.example.DAO.SQLiteGroupDAO;
import com.example.DAO.GroupDAO;
import com.example.DAO.PostgresGroupDAO;
import com.example.DAO.PostgresProjectDAO;
import com.example.DAO.PostgresTagDAO;
import com.example.DAO.PostgresTaskDAO;
import com.example.DAO.SQLiteProjectDAO;
import com.example.DAO.SQLiteTagDAO;
import com.example.DAO.SQLiteTaskDAO;
import com.example.DAO.TagDAO;
import com.example.DAO.TaskDAO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {
    private static HikariDataSource dataSource;
    private static ProjectDAO projectDAO;
    private static GroupDAO groupDAO;
    private static TagDAO tagDAO;
    private static TaskDAO taskDAO;

    public static void switchDatabase(boolean usePostgreSQL) {
        HikariConfig config = new HikariConfig();
        if (usePostgreSQL) {
            config.setJdbcUrl("jdbc:postgresql://localhost:5432/ProjMan");
            config.setUsername("postgres");
            config.setPassword("123");
            projectDAO = new PostgresProjectDAO();
            groupDAO = new PostgresGroupDAO();
            tagDAO = new PostgresTagDAO();
            taskDAO = new PostgresTaskDAO();
        } else {
            config.setJdbcUrl("jdbc:sqlite:ProjMan.db");
            projectDAO = new SQLiteProjectDAO();
            groupDAO = new SQLiteGroupDAO();
            tagDAO = new SQLiteTagDAO();
            taskDAO = new SQLiteTaskDAO();
        }
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);

        if (dataSource != null) {
            dataSource.close();
        }
        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized. Call switchDatabase() first.");
        }
        return dataSource.getConnection();
    }

    public static ProjectDAO getProjectDAO() {
        if (projectDAO == null) {
            throw new IllegalStateException("ProjectDAO is not initialized. Call switchDatabase() first.");
        }
        return projectDAO;
    }

    public static GroupDAO getGroupDAO() {
        if (groupDAO == null) {
            throw new IllegalStateException("GroupDAO is not initialized. Call switchDatabase() first.");
        }
        return groupDAO;
    }

    public static TagDAO getTagDAO() {
        if (tagDAO == null) {
            throw new IllegalStateException("TagDAO is not initialized. Call switchDatabase() first.");
        }
        return tagDAO;
    }

    public static TaskDAO getTaskDAO() {
        if (taskDAO == null) {
            throw new IllegalStateException("TaskDAO is not initialized. Call switchDatabase() first.");
        }
        return taskDAO;
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
