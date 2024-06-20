package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {
    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class  Accout {
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
    @Data
    class User{
        private String userName;
        private String gender;
        private Integer age;

    }

//    @Test
//    public void testDefaultUriBuilderFactory(){
//        DefaultUriBuilderFactory builderFactory = new DefaultUriBuilderFactory();
//
//        //http://search.gulimall.com/list.html
//        UriBuilder builder = builderFactory.builder()
//                .scheme("http")
//                .host("search.gulimall.com")
//                .path("/list.html")
//                .queryParam("keyword", "手机")
//                ;
//
//        String s = builder.build().toString();
//        System.out.println(s);
//    }


    /**
     * （1）、方便检索
     * {
     *      skuId:1
     *      spuId:11
     *      skuTitle:华为xx
     *      price:998
     *      saleCount:99
     *      attrs:[
     *          {尺寸：5寸},
     *          {CPU：高通945},
     *          {分辨率：全高清}
     *      ]
     *  }
     * 冗余：同一个spu的规格参数是一样的，直接存sku会有冗余信息，不过问题不大：
     *  100万*20=1000000*2KB=2000MB=2G 20
     *
     * （2）、
     *    sku索引{
     *     skuId:1
     *     spuId:11
     *     xxxxx
     *    }
     *
     *    attr索引{
     *        spuId:11,
     *        attrs:[
     *              {尺寸：5寸},
     *              {CPU：高通945},
     *              {分辨率：全高清}
     *      ]
     *    }
     *
     *   虽然没有冗余数据但是：
     *
     *   搜索 小米；
     *        粮食类/手机类/电器类都有小米
     *   10000个带小米两字的商品，涉及到了4000个spu
     *
     *   此时我们会分步，找4000个spu对应的所有可能属性；
     *
     *   esClient：不说别的，光spuId:[4000个spuid（id都是long数据）] 4000*8=32000byte=32kb 【一个请求32kb数据】
     *
     *   32kb*10000个人=32000mb;=32GB
     */

    /**
     * 空间与时间不可兼得
     */


    @Test
    public void searchData() throws IOException {
        //1、创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        //SearchSourceBuilder sourceBuilde 封装的条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //1.1）、构造检索条件
//        sourceBuilder.query();
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation()
        sourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        //1.2）、按照年龄的值分布进行聚合
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        //1.3）、计算平均薪资
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAvg);

        System.out.println("检索条件"+sourceBuilder.toString());
        searchRequest.source(sourceBuilder);

        //2、执行检索；
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //3、分析结果 searchResponse
        System.out.println(searchResponse.toString());
//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        //3.1）、获取所有查到的数据
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            /**
             * "_index": "bank",
             * 			"_type": "account",
             * 			"_id": "345",
             * 			"_score": 5.4032025,
             * 			"_source":
             */
//            hit.getIndex();hit.getType();hit.getId();
            String string = hit.getSourceAsString();
            Accout accout = JSON.parseObject(string, Accout.class);
            System.out.println("accout："+accout);
        }

        //3.2）、获取这次检索到的分析信息；
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println("当前聚合："+aggregation.getName());
////            aggregation.get
//
//        }
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄："+keyAsString+"==>"+bucket.getDocCount());
        }

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("平均薪资："+balanceAvg1.getValue());

//        Aggregation balanceAvg2 = aggregations.get("balanceAvg");
    }

    /**
     * 测试存储数据到es
     * 更新也可以
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");//数据的id，如果不设置会自动生成id
//        indexRequest.source("userName","zhangsan","age",18,"gender","男");
        User user = new User();
        user.setUserName("zhangsan");
        user.setAge(18);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);//要保存的内容

        //执行操作
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //提取有用的响应数据
        System.out.println(index);
    }


    @Test
    public void contextLoads() {

        System.out.println(client);
    }

}