package com.github.walker.mybatis.paginator.dialect;

import com.github.walker.mybatis.paginator.PageBounds;
import org.apache.ibatis.mapping.MappedStatement;

/**
 * Vertica分页逻辑
 * <p/>
 * Created by Huqingmiao on 2015/4/14.
 */
public class VerticaDialect extends Dialect {

    public VerticaDialect(MappedStatement mappedStatement, Object parameterObject, PageBounds pageBounds) {
        super(mappedStatement, parameterObject, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, int offset, int limit) {
        StringBuffer buffer = new StringBuffer(sql.length() + 20).append(sql);
        if (offset > 0) {
            buffer.append(" OFFSET ").append(offset).append(" LIMIT ").append(limit);
        } else {
            buffer.append(" LIMIT ").append(limit);
        }
        return buffer.toString();
    }
}
