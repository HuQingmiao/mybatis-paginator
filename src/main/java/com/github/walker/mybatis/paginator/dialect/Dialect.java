package com.github.walker.mybatis.paginator.dialect;

import com.github.walker.mybatis.paginator.Order;
import com.github.walker.mybatis.paginator.PageBounds;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.RowBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页Dialect
 * <p/>
 * 本类是对com.github.miemiedev.mybatis.paginator.dialect的改进(原作者是：badqiu、miemiedev)。
 * <p/>
 * 修改了count语句低效的问题； 修改了oracle分页取不出数据的BUG; 修改了mysql分页低效的问题。
 *
 * @author HuQingmiao
 */
public class Dialect {

    protected MappedStatement mappedStatement;
    protected PageBounds pageBounds;
    protected Object parameterObject;
    protected BoundSql boundSql;
    protected List<ParameterMapping> parameterMappings;
    protected Map<String, Object> pageParameters = new HashMap<String, Object>();

    private String pageSQL;
    private String countSQL;

    protected final static String WALKER_COUNT = "WALKER_COUNT";


    public Dialect(MappedStatement mappedStatement, Object parameterObject, PageBounds pageBounds) {
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        this.pageBounds = pageBounds;

        init();
    }

    protected void init() {
        boundSql = mappedStatement.getBoundSql(parameterObject);
        parameterMappings = new ArrayList(boundSql.getParameterMappings());
        if (parameterObject instanceof Map) {
            pageParameters.putAll((Map) parameterObject);
        } else {
            for (ParameterMapping parameterMapping : parameterMappings) {
                pageParameters.put(parameterMapping.getProperty(), parameterObject);
            }
        }

        StringBuffer bufferSql = new StringBuffer(boundSql.getSql().trim());
        if (bufferSql.lastIndexOf(";") == bufferSql.length() - 1) {
            bufferSql.deleteCharAt(bufferSql.length() - 1);
        }
        String sql = bufferSql.toString();

        //替换制表符
        pageSQL = sql.replaceAll("\t", " ").trim();

        //合并空格符
        pageSQL = pageSQL.replaceAll("\\s{1,}", " ");

        if (pageBounds.getOrders() != null && !pageBounds.getOrders().isEmpty()) {
            pageSQL = getSortString(sql, pageBounds.getOrders());
        }
        if (pageBounds.getOffset() != RowBounds.NO_ROW_OFFSET
                || pageBounds.getLimit() != RowBounds.NO_ROW_LIMIT) {
            pageSQL = getLimitString(pageSQL, pageBounds.getOffset(), pageBounds.getLimit());
        }

        if (pageBounds.isIfCount()) {
            countSQL = getCountString(sql);
        }
    }


    public List<ParameterMapping> getParameterMappings() {
        return parameterMappings;
    }

    public Object getParameterObject() {
        return pageParameters;
    }


    public String getPageSQL() {
        return pageSQL;
    }

    protected void setPageParameter(String name, Object value, Class type) {
        ParameterMapping parameterMapping = new ParameterMapping.Builder(mappedStatement.getConfiguration(), name, type).build();
        parameterMappings.add(parameterMapping);
        pageParameters.put(name, value);
    }


    public String getCountSQL() {
        return countSQL;
    }


    /**
     * 将sql变成分页sql语句
     */
    protected String getLimitString(String sql, int offset, int limit) {
        throw new UnsupportedOperationException("paged queries not supported");
    }

    /**
     * 将sql转换为总记录数SQL
     *
     * @param sql SQL语句
     * @return 总记录数的sql
     */
    protected String getCountString(String sql) {

        //若含有DISTINCT
        if (sql.toUpperCase().startsWith("SELECT DISTINCT ")) {
            return "SELECT COUNT(1) FROM (" + sql + ") WALKER_COUNT";
        }

        // 为提升SQL性能，在count时去掉order by 子句。 -Updated by HuQingmiao 2015-08-25
        int orderPosi = this.indexIgloreCase(sql, " ORDER BY ", 0, sql.length());
        if (orderPosi > 0) {
            sql = sql.substring(0, orderPosi);
        }

        //取 从'FROM'开始的部分SQL构造COUNT语句
        final String from = " FROM ";
        int fromPosi = this.indexIgloreCase(sql, from, 0, sql.length());

        StringBuffer countSql = new StringBuffer("SELECT COUNT(1) ");
        countSql.append(sql.substring(fromPosi));
        return countSql.toString();
    }

    /**
     * 将sql转换为带排序的SQL
     *
     * @param sql SQL语句
     * @return 总记录数的sql
     */
    protected String getSortString(String sql, List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return sql;
        }
        StringBuffer buffer = new StringBuffer(sql);
        buffer.append(" ORDER BY ");

        for (Order order : orders) {
            if (order != null) {
                buffer.append(order.toString())
                        .append(", ");
            }
        }
        buffer.delete(buffer.length() - 2, buffer.length());
        return buffer.toString();
    }


    /**
     * 忽略大小写, 检索baseStr中从位置startPos至endPos的子串, 看是否存在子串indexedStr. 返回第一次匹配的位置.
     *
     * @param baseStr
     * @param indexedStr
     * @param startPos
     * @param endPos
     * @return
     * @author HuQingmiao
     */
    protected int indexIgloreCase(String baseStr, String indexedStr, int startPos, int endPos) {
        String str = baseStr.toUpperCase();
        return str.indexOf(indexedStr.toUpperCase());
    }

    public static void main(String[] args) {
        String sql = "\t \tSELECT AC_ORG as FRT_ACC_ORG, AC_TYP as FRT_AC_TYP, CAP_TYP as FRT_CAP_TYP, CCY as FRT_CCY, " +
                " SUM( CASE WHEN UPD_DT > #{sumDt} THEN LAST_AC_BAL ELSE CUR_AC_BAL END ) AS FRT_AC_BAL\n" +
                " FROM ACTTACBL " +
                " GROUP BY AC_ORG, AC_TYP, CAP_TYP, CCY   ";

        String sql2 = "SELECT book_id,title,cost,publish_time,blob_content,text_content,update_time\n" +
                "        FROM book\n" +
                "         WHERE  title like ?\n" +
                "            \n" +
                "            \n" +
                "               AND cost >= ?\n" +
                "            \n" +
                "            \n" +
                "                  AND cost < ?";


        System.out.println(sql);

        //System.out.println(indexIgloreCase(sql, " FROM ", 0, sql.length()));

        //System.out.println(indexIgloreCase(sql2, " FROM ", 0, sql.length()));

        String st = "asfas    b   sd ";

        String test = st.trim().replaceAll("\\s{1,}", " ");
        System.out.println(test + "<<");
    }
}
