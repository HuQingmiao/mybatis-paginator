package com.github.walker.mybatis.paginator;


import com.github.walker.mybatis.paginator.dialect.Dialect;
import com.github.walker.mybatis.paginator.support.SQLHelp;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;


/**
 * 为myBatis提供基于数据库方言的分页插件
 * <p/>
 * 本类是对com.github.miemiedev.mybatis.paginator.OffsetLimitInterceptor的重构(原作者是：badqiu、miemiedev)。
 * <p/>
 * 本次修改的内容:
 * 1. 使得在分页和非分页场景下，查询返回的结果都是PageList类型，而PageList继承ArrayList；
 * 2. 修改了分页参数的传递形式；
 * 3. 减化了部分代码，去掉了Paginator类。
 *
 * @author HuQingmiao 2015-08-26
 */
@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class OffsetLimitInterceptor implements Interceptor {

    //private static Logger log = LoggerFactory.getLogger(OffsetLimitInterceptor.class);

    private ExecutorService pool = Executors.newFixedThreadPool(2);

    private static int MAPPED_STATEMENT_INDEX = 0;
    private static int PARAMETER_INDEX = 1;
    private static int ROWBOUNDS_INDEX = 2;

    private String dialectClass;

    @Override
    public void setProperties(Properties properties) {
        String dialectClass = (String) properties.get("dialectClass");
        this.dialectClass = dialectClass;
    }

    public Object intercept(final Invocation invocation) throws Throwable {
        final Executor executor = (Executor) invocation.getTarget();
        final Object[] queryArgs = invocation.getArgs();
        final MappedStatement ms = (MappedStatement) queryArgs[MAPPED_STATEMENT_INDEX];
        final Object parameter = queryArgs[PARAMETER_INDEX];
        final RowBounds rowBounds = (RowBounds) queryArgs[ROWBOUNDS_INDEX];

        //DAO接口中没有传PageBounds参量
        if (!(rowBounds instanceof PageBounds)) {
            return invocation.proceed();
        }

        //DAO接口传有PageBounds参量
        PageBounds pageBounds = (PageBounds) rowBounds;
        if (pageBounds.getOffset() == PageBounds.NO_ROW_OFFSET
                && pageBounds.getLimit() == PageBounds.NO_ROW_LIMIT
                && pageBounds.getOrders().isEmpty()) {
            return new PageList((ArrayList) invocation.proceed());
        }

        final Dialect dialect;
        try {
            Class clazz = Class.forName(dialectClass);
            Constructor constructor = clazz.getConstructor(MappedStatement.class, Object.class, PageBounds.class);
            dialect = (Dialect) constructor.newInstance(new Object[]{ms, parameter, pageBounds});
        } catch (Exception e) {
            throw new ClassNotFoundException("Cannot create dialect instance: " + dialectClass, e);
        }

        final BoundSql boundSql = ms.getBoundSql(parameter);
        queryArgs[MAPPED_STATEMENT_INDEX] = copyFromNewSql(ms, boundSql, dialect.getPageSQL(), dialect.getParameterMappings(), dialect.getParameterObject());
        queryArgs[PARAMETER_INDEX] = dialect.getParameterObject();
        queryArgs[ROWBOUNDS_INDEX] = new RowBounds();

        //采用异步方式，执行分页查询
        Callable<List> queryThread = new Callable<List>() {
            public List call() throws Exception {
                return (List) invocation.proceed();
            }
        };
        Future<List> queryFuture = call(queryThread, true);

        //如果不需要count总的结果集，则直接返回分页查询结果
        if (!pageBounds.isIfCount()) {
            return new PageList(queryFuture.get());
        }

        //对总的结果集进行count
        Callable<Integer> countThread = new Callable<Integer>() {
            public Integer call() throws Exception {
                Cache cache = ms.getCache();
                Integer count = null;
                if (cache != null && ms.isUseCache() && ms.getConfiguration().isCacheEnabled()) {
                    CacheKey cacheKey = executor.createCacheKey(ms, parameter, new RowBounds(), copyFromBoundSql(ms, boundSql, dialect.getCountSQL(), boundSql.getParameterMappings(), boundSql.getParameterObject()));
                    count = (Integer) cache.getObject(cacheKey);
                    if (count == null) {
                        count = SQLHelp.getCount(ms, parameter, boundSql, dialect);
                        cache.putObject(cacheKey, count);
                    }
                } else {
                    count = SQLHelp.getCount(ms, parameter, boundSql, dialect);
                }
                return count;
            }
        };
        Future<Integer> countFutrue = call(countThread, true);
        PageList pageList = new PageList(queryFuture.get(), countFutrue.get());
        pool.shutdown();

        return pageList;
    }

    private <T> Future<T> call(Callable callable, boolean async) {
        if (async) {
            return pool.submit(callable);
        } else {
            FutureTask<T> future = new FutureTask(callable);
            future.run();
            return future;
        }
    }

    private MappedStatement copyFromNewSql(MappedStatement ms, BoundSql boundSql,
                                           String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = copyFromBoundSql(ms, boundSql, sql, parameterMappings, parameter);
        return copyFromMappedStatement(ms, new BoundSqlSqlSource(newBoundSql));
    }

    private BoundSql copyFromBoundSql(MappedStatement ms, BoundSql boundSql,
                                      String sql, List<ParameterMapping> parameterMappings, Object parameter) {
        BoundSql newBoundSql = new BoundSql(ms.getConfiguration(), sql, parameterMappings, parameter);
        for (ParameterMapping mapping : boundSql.getParameterMappings()) {
            String prop = mapping.getProperty();
            if (boundSql.hasAdditionalParameter(prop)) {
                newBoundSql.setAdditionalParameter(prop, boundSql.getAdditionalParameter(prop));
            }
        }
        return newBoundSql;
    }

    //see: MapperBuilderAssistant
    private MappedStatement copyFromMappedStatement(MappedStatement ms, SqlSource newSqlSource) {
        Builder builder = new Builder(ms.getConfiguration(), ms.getId(), newSqlSource, ms.getSqlCommandType());

        builder.resource(ms.getResource());
        builder.fetchSize(ms.getFetchSize());
        builder.statementType(ms.getStatementType());
        builder.keyGenerator(ms.getKeyGenerator());
        if (ms.getKeyProperties() != null && ms.getKeyProperties().length != 0) {
            StringBuffer keyProperties = new StringBuffer();
            for (String keyProperty : ms.getKeyProperties()) {
                keyProperties.append(keyProperty).append(",");
            }
            keyProperties.delete(keyProperties.length() - 1, keyProperties.length());
            builder.keyProperty(keyProperties.toString());
        }

        //setStatementTimeout()
        builder.timeout(ms.getTimeout());

        //setStatementResultMap()
        builder.parameterMap(ms.getParameterMap());

        //setStatementResultMap()
        builder.resultMaps(ms.getResultMaps());
        builder.resultSetType(ms.getResultSetType());

        //setStatementCache()
        builder.cache(ms.getCache());
        builder.flushCacheRequired(ms.isFlushCacheRequired());
        builder.useCache(ms.isUseCache());

        return builder.build();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }


    public static class BoundSqlSqlSource implements SqlSource {
        BoundSql boundSql;

        public BoundSqlSqlSource(BoundSql boundSql) {
            this.boundSql = boundSql;
        }

        public BoundSql getBoundSql(Object parameterObject) {
            return boundSql;
        }
    }

}
