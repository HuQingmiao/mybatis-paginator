package com.github.walker.mybatis.paginator;

import com.github.walker.mybatis.paginator.utils.MappingUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * @author miemiedev
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 8138022018100161833L;
    private Direction direction;
    private String property;
    private String orderExpr;

    private Order() {
    }

    private Order(String property, Direction direction, String orderExpr) {
        this.direction = direction;
        this.property = property;
        this.orderExpr = orderExpr;
    }

    public Direction getDirection() {
        return direction;
    }

    public String getProperty() {
        return property;
    }

    public String getOrderExpr() {
        return orderExpr;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setOrderExpr(String orderExpr) {
        this.orderExpr = orderExpr;
    }

    private static String INJECTION_REGEX = "[A-Za-z0-9\\_\\-\\+\\.]+";

    public static boolean isSQLInjection(String str) {
        return !Pattern.matches(INJECTION_REGEX, str);
    }

    @Override
    public String toString() {
        if (isSQLInjection(property)) {
            throw new IllegalArgumentException("SQLInjection property: " + property);
        }
        if (orderExpr != null && orderExpr.indexOf("?") != -1) {
            String[] exprs = orderExpr.split("\\?");
            if (exprs.length == 2) {
                return String.format(orderExpr.replaceAll("\\?", "%s"), property) + (direction == null ? "" : " " + direction.name());
            }
            return String.format(orderExpr.replaceAll("\\?", "%s"), property, direction == null ? "" : " " + direction.name());
        }

        //如果排序属性中含有"_"，或仅含小写字母、数字，或仅含大写字母、数字， 则被认为是是列名，不需要再转换
        if (property.contains("_") || property.matches("[a-z0-9]+") || property.matches("[A-Z0-9]+")) {
            return property + (direction == null ? "" : " " + direction.name());
        } else {
            return MappingUtil.getColumnName(property) + (direction == null ? "" : " " + direction.name());
        }
    }


    public static LinkedList<Order> formString(String orderSegment) {
        return formString(orderSegment, null);
    }

    /**
     * @param orderSegment ex: "id.asc,code.desc" or "code.desc"
     */
    public static LinkedList<Order> formString(String orderSegment, String orderExpr) {
        if (orderSegment == null || orderSegment.trim().equals("")) {
            return new LinkedList();
        }

        LinkedList<Order> results = new LinkedList();
        String[] orderSegments = orderSegment.trim().split(",");
        for (int i = 0; i < orderSegments.length; i++) {
            String sortSegment = orderSegments[i];
            Order order = _formString(sortSegment, orderExpr);
            if (order != null) {
                results.add(order);
            }
        }
        return results;
    }


    private static Order _formString(String orderSegment, String orderExpr) {

        if (orderSegment == null || orderSegment.trim().equals("") ||
                orderSegment.startsWith("null.") || orderSegment.startsWith(".")) {
            return null;
        }

        String[] array = orderSegment.trim().split("\\.");
        if (array.length != 1 && array.length != 2) {
            throw new IllegalArgumentException("orderSegment pattern must be {property}.{direction}, input is: " + orderSegment);
        }

        return create(array[0], array.length == 2 ? array[1] : "asc", orderExpr);
    }

    public static Order create(String property, String direction) {
        return create(property, direction, null);
    }

    /**
     * @param property
     * @param direction
     * @param orderExpr placeholder is "?", in oracle like: "nlssort( ? ,'NLS_SORT=SCHINESE_PINYIN_M')".
     *                  Warning: you must prevent orderExpr SQL injection.
     * @return
     */
    public static Order create(String property, String direction, String orderExpr) {
        return new Order(property, Order.Direction.fromString(direction), orderExpr);
    }


    public static enum Direction {
        ASC, DESC;

        public static Direction fromString(String value) {
            try {
                return Direction.valueOf(value.toUpperCase(Locale.US));
            } catch (Exception e) {
                return ASC;
            }
        }
    }

    public static void main(String[] args) {
        String a = "aasb";
        System.out.println(a.contains("_") || a.matches("[a-z0-9]+") || a.matches("[A-Z0-9]+"));

        a = "AASB";
        System.out.println(a.contains("_") || a.matches("[a-z0-9]+") || a.matches("[A-Z0-9]+"));

        a = "aasb12";
        System.out.println(a.contains("_") || a.matches("[a-z0-9]+") || a.matches("[A-Z0-9]+"));

        a = "AASB12";
        System.out.println(a.contains("_") || a.matches("[a-z0-9]+") || a.matches("[A-Z0-9]+"));

        a = "aasb_12";
        System.out.println(a.contains("_") || a.matches("[a-z0-9]+") || a.matches("[A-Z0-9]+"));


        System.out.println(MappingUtil.getColumnName(a));
    }
}
