package com.mycompany.backend.correo;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@ApplicationScoped
public class DatabaseConnection {

    @Resource(lookup = "jdbc/CorreoDB")
    private DataSource dataSource;

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El DataSource 'jdbc/CorreoDB' no fue inyectado correctamente por Payara.");
        }
        return dataSource.getConnection();
    }
}