package walker.mybatis.paginator.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import walker.mybatis.paginator.dialect.Dialect;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author miemiedev
 */
public class SQLHelp {
    private static final Logger LOGGER = LogManager.getLogger(SQLHelp.class);


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
        LOGGER.debug("Total count SQL [{}] ", countSql);
        LOGGER.debug("Total count Parameters: {} ", parameterObject);

        try (Connection conn = mappedStatement.getConfiguration().getEnvironment().getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(countSql);) {

            DefaultParameterHandler handler = new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
            handler.setParameters(stmt);

            ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }
            LOGGER.debug("Total count: {}", count);
            return count;
        }
    }

}