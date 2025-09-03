package walker.mybatis.paginator.dialect;

import walker.mybatis.paginator.PageBounds;
import org.apache.ibatis.mapping.MappedStatement;

import java.util.StringTokenizer;

/**
 * Oracle分页Dialect
 * <p/>
 * 重写Oracle的分页语句
 *
 * @author HuQingmiao
 */
public class OracleDialect extends Dialect {

    // 以下关键字是EasyDB专用的, 在客户程序传入的SQL中不得使用.
    private static final String WALKER_PAGER_ROW = "WALKER_PAGER_ROW";

    private static final String WALKER_A = "WALKER_A";

    private static final String WALKER_B = "WALKER_B";

    private static final String WALKER_COUNT = "WALKER_COUNT";

    public OracleDialect(MappedStatement mappedStatement, Object parameterObject, PageBounds pageBounds) {
        super(mappedStatement, parameterObject, pageBounds);
    }

    @Override
    protected String getLimitString(String sql, int offset, int limit) {
        final String select = "SELECT ";
        final String from = " FROM ";

        sql = sql.trim();
        int selectPosi = this.indexIgloreCase(sql, select);

        int fromPosi = this.indexIgloreCase(sql, from);

        // 取得要查询的列, 即originSql中"SELECT" 到 "FROM" 之间的字符串
        String columns = sql.substring(selectPosi + select.length(), fromPosi);

        // 去掉左右括号及其中间的内容, 如SELECT A.NAME,
        // (TO_DATE('2003-02-04','YYYY-MM-DD')-SYSDATE) EXCEED_DAY_CN
        columns = trimBracket(columns);

        // 存放转换后的查询列
        StringBuffer colStr = new StringBuffer();

        StringTokenizer st = new StringTokenizer(columns);

        // 取得各个查询列
        while (st.hasMoreElements()) {
            String col = st.nextToken(",").trim();

            // 如果该列带有表名或表的别名, 则过滤之
            int dotPosi = col.indexOf('.');
            if (dotPosi > 0) {
                col = col.substring(dotPosi + 1);
            }

            // 如果该列定义了别名, 则取别名作为列名
            int asPosi = col.lastIndexOf(' ');
            if (asPosi > 0) {
                col = col.substring(asPosi + 1).trim();
            }

            colStr.append(WALKER_A).append('.').append(col).append(',');
        }

        colStr.deleteCharAt(colStr.length() - 1);

        // 构造分页SQL
        StringBuffer pagerSql = new StringBuffer("SELECT ").append(WALKER_B).append(".*");
        pagerSql.append(" FROM ( SELECT ");
        pagerSql.append(colStr.toString());
        pagerSql.append(", ROWNUM ");
        pagerSql.append(WALKER_PAGER_ROW);
        pagerSql.append(" FROM (").append(sql).append(") ");
        pagerSql.append(WALKER_A);
        pagerSql.append(" WHERE ROWNUM < ").append(offset + 1 + limit);
        pagerSql.append(" ) ").append(WALKER_B);
        pagerSql.append(" WHERE ");
        pagerSql.append(WALKER_B).append('.').append(WALKER_PAGER_ROW);
        pagerSql.append(" > ").append(offset);

        colStr.delete(0, colStr.length());

        return pagerSql.toString();
    }

    /**
     * 去掉左右括号中间的字符
     *
     * @param colstr 左右括号必须成对出现的字符串
     */
    private String trimBracket(String colstr) {

        String str = colstr.trim();
        int i = str.indexOf("(");
        if (i < 0) {
            return str;
        }

        StringBuffer buff = new StringBuffer(str);
        buff.deleteCharAt(i);// 删除出现的第一个'(', 此时i指向'('后面的那个字符

        int leftCnt = 1;// 出现的左括号数
        int rightCnt = 0;// 出现的右括号数

        while (i < buff.length() && rightCnt < leftCnt) {
            char ch = buff.charAt(i);
            buff.deleteCharAt(i);

            if (ch == '(') {
                leftCnt++;
            }
            if (ch == ')') {
                rightCnt++;
            }
        }
        String s = buff.toString();
        buff.delete(0, s.length());

        return trimBracket(s);
    }
}
