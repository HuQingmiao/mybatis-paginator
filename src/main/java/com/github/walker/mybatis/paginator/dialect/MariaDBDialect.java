package com.github.walker.mybatis.paginator.dialect;

import com.github.walker.mybatis.paginator.PageBounds;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * MariaDB分页Dialect
 * <p/>
 * 重写mysql的分页语句.
 *
 * @author HuQingmiao
 */
public class MariaDBDialect extends Dialect {

    public MariaDBDialect(MappedStatement mappedStatement, Object parameterObject, PageBounds pageBounds) {
        super(mappedStatement, parameterObject, pageBounds);
    }

    protected String getLimitString(String sql, int offset, int limit) {
        StringBuffer buffer = new StringBuffer(sql.length() + 20).append(sql);
        if (offset > 0) {
            buffer.append(" LIMIT ").append(offset).append(",").append(limit);
        } else {
            buffer.append(" LIMIT ").append(limit);
        }
        return buffer.toString();
    }

}
