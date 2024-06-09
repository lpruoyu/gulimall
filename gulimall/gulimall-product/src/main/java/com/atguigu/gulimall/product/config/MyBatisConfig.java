package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement //开启事务
@MapperScan("com.atguigu.gulimall.product.dao")
public class MyBatisConfig {

    /*
    分页查询：
        # 假设每页15条（pageSize = 15）
        # 假设查询第n页 （n >= 1）
        #   SELECT * FROM student LIMIT (n - 1) * pageSize, pageSize;
            SELECT * FROM student LIMIT 0, 15; # 查询第一页
            SELECT * FROM student LIMIT 15, 15; # 查询第二页

    总数量：101条
    每一页显示20条
    公式：总页数 = (总数量  +  每页的数量   -   1) / 每页的数量
                = ( 101   +    20        -   1) / 20
     */

    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        // 设置请求的页面大于最大页后操作， true调回到首页，false 继续请求  默认false
         paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，默认 500 条，-1 不受限制
        paginationInterceptor.setLimit(1000);
        return paginationInterceptor;
    }

}
