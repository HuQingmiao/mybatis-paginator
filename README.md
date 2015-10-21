
## mybatis-paginator-20150827
mybatis-paginator，为采用myBatis的项目提供支持mysql/oracle等数据库的分页插件。目前，mybatis的分页插件不少，但多数分页插件接受的参数是页码、第页的记录条数，这就使得并不能查询从任意起始止到任意结束行的记录。为此，我参考网友miemiedev的同名项目代码，重新设计了本款分页插件，使得可以查询任意起止行范围内的记录，并对count()等SQL进行了优化。
 
 
##使用本分页插件，步骤如下：
1. 使用本分页插件，先下载mybatis-paginator.jar(链接: http://pan.baidu.com/s/1kTF7xf5 密码: p154)，然后将这个jar包引入到你的工程。
2. 在你的工程的mybatis.xml中，添加如下配置:
```
    <plugins>
        <plugin interceptor="com.github.walker.mybatis.paginator.OffsetLimitInterceptor">
            <property name="dialectClass" value="com.github.walker.mybatis.paginator.dialect.MySQLDialect"/>
        </plugin>
    </plugins>
```
3. 在你的工程的业务层代码中，通过如下方式调用分页接口：
```
    public void findBooks() {
        HashMap<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("title", "%UNIX%");
        paramMap.put("minCost", new Float(21));
        paramMap.put("maxCost", new Float(101));

        // new PageBounds(); //非分页方式，采用默认构造函数
        // new PageBounds(int limit); //取前面的limit条记录

	//取从offset开始的limit条记录，offset从0开始
        // new PageBounds(int offset, int limit); 

	//按cost升序、book_id倒序排列后再分页
        // new PageBounds(int page, int limit, Order.formString("cost.asc, book_id.desc"));

	//如果想排序的话,以逗号分隔多项排序,若查询语句中有ORDER BY, 则仍然会以此为准。
        String sortString = "cost.asc, book_id.desc";
	
	//取第4条后面的3条记录
        PageBounds pageBounds = new PageBounds(4, 3, Order.formString(sortString));
        List<Book> bookList = bookDao.find(paramMap, pageBounds);

        PageList<Book> pageList = (PageList<Book>) bookList;// 获得结果集条总数
        log.info("本页记录数: " + bookList.size());
        log.info("总的记录数: " + pageList.size());
        for (Book book : bookList) {
            log.info(book.getBookId() + " " + book.getTitle() + " " + book.getCost());
        }
    }
```


##参与开发本分页插件，请知道：
####编译环境
* Windows or Linux
* Java 7
* Maven 3.0.5+ (for building)

####开发工具
* IntelliJ IDEA, Eclipse

