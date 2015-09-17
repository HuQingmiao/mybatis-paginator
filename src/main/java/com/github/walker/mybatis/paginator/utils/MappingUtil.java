package com.github.walker.mybatis.paginator.utils;


/**
 * mapping util class.
 *
 * @author HuQingmiao
 */
public class MappingUtil {

    /**
     * Translates table name into the class name of relative entity.
     * <p/>
     * For example: Input the parameter 'COMPANY_NAME', this method will return
     * the result 'companyName'.
     *
     * @param tableName the table name in database
     * @return the class name of BaseEntity
     */
    public static String getEntityName(String tableName) {

        StringBuffer buff = new StringBuffer(tableName.toLowerCase());

        // the first character of class name is upper case
        buff.replace(0, 1, String.valueOf(Character.toUpperCase(tableName.charAt(0))));

        // delete character '_', and convert the next character to uppercase
        for (int i = 1, length = buff.length(); i < length; ) {

            char lastCh = buff.charAt(i - 1);// the last character
            char ch = buff.charAt(i); // the current character

            // if this character is a letter, and the last character is '_'
            if (Character.isLetter(ch) && lastCh == '_') {
                buff.replace(i - 1, i, String.valueOf(Character.toUpperCase(ch)));

                buff.deleteCharAt(i);
                length--;
            } else {
                i++;
            }
        }

        return buff.toString();
    }

    /**
     * Translates column name into the field name in relative entity class.
     * <p/>
     * For example: Input the parameter 'COMPANY_NAME', this method will return
     * the result 'companyName'.
     *
     * @param columnName the column name in database
     * @return the field name of BaseEntity Class
     */
    public static String getFieldName(String columnName) {

        StringBuffer buff = new StringBuffer(columnName.toLowerCase());

        // delete character '_', and convert the next character to uppercase
        for (int i = 1, length = buff.length(); i < length; ) {

            char lastCh = buff.charAt(i - 1);// the last character
            char ch = buff.charAt(i); // the current character

            // if this character is a letter, and the last character is '_'
            if (Character.isLetter(ch) && lastCh == '_') {
                buff.replace(i - 1, i, String.valueOf(Character.toUpperCase(ch)));

                buff.deleteCharAt(i);
                length--;
            } else {
                i++;
            }
        }

        return buff.toString();
    }


    /**
     * Parses text from the given string to produce a column name that dababase
     * understands.
     * <p/>
     * For example: Input the parameter 'companyName', this method will return
     * the result 'COMPANY_NAME'.
     *
     * @param fieldName the String object, the field name in entity class.
     * @return the column name that the dababase understands.
     */
    public static String getColumnName(String fieldName) {

        StringBuffer buff = new StringBuffer(fieldName);

        for (int i = 1, length = buff.length(); i < length; i++) {
            // insert character '_' before uppercase or digit
            if (Character.isUpperCase(buff.charAt(i))) {
                buff.insert(i++, '_');
                length++;
            }
        }

        return buff.toString().toLowerCase();
    }
}