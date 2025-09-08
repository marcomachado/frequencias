package com.petsaudedigital.backend.config;

import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.spi.SQLExceptionConverter;

import java.sql.SQLException;

public class SQLiteConstraintConverter implements SQLExceptionConverter {
    @Override
    public JDBCException convert(SQLException sqlException, String message, String sql) {
        String msg = sqlException.getMessage();
        if (msg != null && msg.contains("SQLITE_CONSTRAINT")) {
            return new ConstraintViolationException(message, sqlException, sql);
        }
        return null; // fall back to default conversion
    }
}
