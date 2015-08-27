# mybatis-paginator，为myBatis提供的基于mysql/oracle数据库的分页插件。


使用本分页插件，在mybatis.xml添加如下配置即可:
<![CDATA[
    <plugins>
	<plugin interceptor="walker.mybatis.paginator.OffsetLimitInterceptor">
    </plugins>
]]>

代码示例：

    public void findBooks() {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("title", "%UNIX%");
        paramMap.put("minCost", new Float(21));
        paramMap.put("maxCost", new Float(101));

        // new PageBounds(); //非分页方式，采用默认构造函数
        // new PageBounds(int limit); //取前面的limit条记录

	//取从offset开始的limit条记录，offset从1开始
        // new PageBounds(int offset, int limit); 

	//按cost升序、book_id倒序排列后再分页
        // new PageBounds(int page, int limit, Order.formString("cost.asc, book_id.desc"));

	//如果想排序的话,以逗号分隔多项排序,若查询语句中有ORDER BY, 则仍然会以此为准。
        String sortString = "cost.asc, book_id.desc";
	
	//取第4条开始的3条记录
        PageBounds pageBounds = new PageBounds(4, 3, Order.formString(sortString));
        List<Book> bookList = bookDao.find(paramMap, pageBounds);

        PageList<Book> pageList = (PageList<Book>) bookList;// 获得结果集条总数
        log.info("本页记录数: " + bookList.size());
        log.info("总的记录数: " + pageList.size());
        for (Book book : bookList) {
            log.info(book.getBookId() + " " + book.getTitle() + " " + book.getCost());
        }
    }
