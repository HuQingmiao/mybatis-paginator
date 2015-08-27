package walker.mybatis.paginator;

import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * 用于包装和传递分页参数的类。
 * <p/>
 * 本类是对com.github.miemiedev.mybatis.paginator.domain.PageBounds的重新设计(原作者是：badqiu、miemiedev)。
 * <p/>
 * 原来的PageBounds接受的参数是页码、每页记录数，查询时必须先指定第页记录数。 显然，这样不够灵活，不能查询指定
 * 某条记录开始的某段记录，如查询第2~第5条。为此，我对PageBounds类及OffsetLimitInterceptor进行了重构。
 *
 * @author HuQingmiao
 */
public class PageBounds extends RowBounds implements Serializable {
    private static final long serialVersionUID = -6414350656252331011L;

    public final static int NO_ROW_OFFSET = 1;
    public final static int NO_ROW_LIMIT = Integer.MAX_VALUE;

    private int offset = NO_ROW_OFFSET;

    private int limit = NO_ROW_LIMIT;

    private LinkedList<Order> orders = new LinkedList<Order>();

    protected boolean ifCount = false;//是否对总的结果集进行count

    /**
     * 无参构造方法，用于不分页的场景
     */
    public PageBounds() {
    }

    public PageBounds(LinkedList<Order> orders) {
        this.orders = orders;
    }

    public PageBounds(int limit) {
        this.limit = limit;
    }

    public PageBounds(int limit, LinkedList<Order> orders) {
        this.limit = limit;
        this.orders = orders;
    }

    public PageBounds(int offset, int limit) {
        this(offset, limit, true);
    }

    public PageBounds(int offset, int limit, LinkedList<Order> orders) {
        this(offset, limit, orders, true);
    }

    public PageBounds(int offset, int limit, boolean ifCount) {
        this.offset = offset;
        this.limit = limit;
        this.ifCount = ifCount;
    }

    /**
     * @param offset  起始行号，取值范围从1开始
     * @param limit   记录条数
     * @param orders  排序
     * @param ifCount 是否对总的结果集进行count
     */
    public PageBounds(int offset, int limit, LinkedList<Order> orders, boolean ifCount) {
        this.offset = offset;
        this.limit = limit;
        this.orders = orders;
        this.ifCount = ifCount;
    }

    public PageBounds(RowBounds rowBounds) {
        this.offset = rowBounds.getLimit();
        this.limit = rowBounds.getLimit();
        this.ifCount = true;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public LinkedList<Order> getOrders() {
        return this.orders;
    }

    public void setOrders(LinkedList<Order> orders) {
        this.orders = orders;
    }

    public boolean isIfCount() {
        return ifCount;
    }

    @Override
    public int getLimit() {
        return this.limit;
    }
}