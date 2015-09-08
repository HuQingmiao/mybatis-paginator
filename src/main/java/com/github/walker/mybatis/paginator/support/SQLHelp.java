package com.github.walker.mybatis.paginator.support;

import com.github.walker.mybatis.paginator.dialect.Dialect;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author miemiedev
 */
public class SQLHelp {
    private static Logger log = LoggerFactory.getLogger(SQLHelp.class);

    /**
     * 查询总纪录数
     *
     * @param mappedStatement mapped
     * @param parameterObject 参数
     * @param boundSql        boundSql
     * @param dialect         database dialect
     * @return 总记录数
     * @throws java.sql.SQLException sql查询错误
     */
    public static int getCount(
            final MappedStatement mappedStatement, final Object parameterObject,
            final BoundSql boundSql, Dialect dialect) throws SQLException {
        final String countSql = dialect.getCountSQL();
        log.debug("Total count SQL [{}] ", countSql);
        log.debug("Total count Parameters: {} ", parameterObject);

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
            stmt = conn.prepareStatement(countSql);
            DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
            handler.setParameters(stmt);

            rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            log.debug("Total count: {}", count);
            return count;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } finally {
                try {
                    if (stmt != null) {
                        stmt.close();
                    }
                } finally {
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                    }
                }
            }
        }
    }

}