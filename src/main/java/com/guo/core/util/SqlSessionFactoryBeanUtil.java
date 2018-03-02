package com.guo.core.util;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedIOException;

import java.io.IOException;

/**
 * Created by guo on 3/2/2018.
 * SqlSessionFactoryBean工厂bean
 */
public class SqlSessionFactoryBeanUtil extends SqlSessionFactoryBean {

    private static Logger logger = LoggerFactory.getLogger(SqlSessionFactoryBeanUtil.class);

    @Override
    protected SqlSessionFactory buildSqlSessionFactory() throws IOException {
        try {
            return super.buildSqlSessionFactory();
        }catch (NestedIOException e) {
            logger.error("ex:{}",e.getMessage());
            throw new NestedIOException("Faild to parse mapping resource: " + e);
        }finally {
            ErrorContext.instance().reset();
        }
    }
}
