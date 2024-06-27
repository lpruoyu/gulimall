package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.constant.RedisConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        /// 组装父子结构：

        // 查出一级分类
        List<CategoryEntity> level1 = entities.stream().filter(entity -> entity.getParentCid() == 0).map(entity -> {
                    entity.setChildren(getChildren(entity, entities));
                    return entity;
                })
                // 0 是排序的默认值
                .sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());
        return level1;
    }

    @Override
    public void removeMenusByIds(List<Long> ids) {
        //TODO 检查当前删除的菜单，是否被别的地方引用

//        这个方法不推荐直接使用，推荐使用逻辑删除
        baseMapper.deleteBatchIds(ids);
    }

    // 返回：[2, 25, 225]
    // 也就是根据子分类，查出所有父分类id
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        ArrayList<Long> list = new ArrayList<>();
        findCatelogPath(list, catelogId);
        return list.toArray(new Long[list.size()]);
    }


    private void findCatelogPath(ArrayList<Long> list, Long catelogId) {
        CategoryEntity entity = this.getById(catelogId);
        if (entity != null) {
            list.add(0, catelogId);
            Long parentCid = entity.getParentCid();
            if (parentCid != 0) {
//                list.add(0, parentCid);
                findCatelogPath(list, parentCid);
            }
        }
    }

    // 递归查找所有子分类
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> collect = all.stream()
//                .filter(entity -> entity.getParentCid() == root.getCatId()) // 这样写是有bug的
                .filter(entity -> entity.getParentCid().equals(root.getCatId())) // 应该使用equals比较
                .map(entity -> {
                    entity.setChildren(getChildren(entity, all));
                    return entity;
                }).sorted((menu1, menu2) -> (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort())).collect(Collectors.toList());
        return collect;
    }

    /**
     * 级联更新所有关联的数据
     * @CacheEvict:失效模式
     * 1、同时进行多种缓存操作  @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区（这个分区只是Spring逻辑划分的，Redis物理存储并没有）。
     * 这样做的好处就是当我们修改了这个类型的数据，我们就直接可以将分区里跟这个类型有关的所有数据都删除掉
     * 分区名默认就是缓存的前缀（不在application.properties中指定前缀）
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'listLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
//  或者
    @CacheEvict(value = "category",allEntries = true) //缓存失效模式 //allEntries:删除所有category的缓存
//    @CachePut //缓存双写模式 ： 方法没有返回值不支持双写模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);

        //TODO 当category表中的某些字段被其他表使用，并且该字段发生了更新，那么也需要修改那些表的信息
        if (!StringUtils.isEmpty(category.getName())) { // 分类名发生了变化
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }
    }

    /**
     * 1、每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(推荐按照业务类型分)】
     * 2、 @Cacheable({"category"})
     * 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     * 如果缓存中没有，会调用方法，最后将方法的结果放入缓存
     * 3、默认行为
     * 1）、如果缓存中有，方法不用调用。
     * 2）、key默认自动生成 category::SimpleKey [] ==> 缓存的名字::SimpleKey [] (自主生成的key值)
     * 3）、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存到redis
     * 4）、默认ttl时间 -1；代表永不过期
     *
     * 自定义：
     * 1）、指定生成的缓存使用的key：key属性指定接受一个SpEL表达式
     * SpEL的详细语法: https://docs.spring.io/spring/docs/5.1.12.RELEASE/spring-framework-reference/integration.html#cache-spel-context
     * 2）、指定缓存的数据的存活时间： 配置文件中修改ttl: spring.cache.redis.time-to-live=10000
     * 3）、将数据保存为json格式:自定义RedisCacheConfiguration即可
     *
     *
     * 4、Spring-Cache的不足；
     * 1）、读模式：Spring-Cache全部考虑到了
         * 缓存穿透：查询一个缓存和数据库都为null的数据。解决：缓存空数据；cache-null-values=true
         * 缓存击穿：大量并发进来同时查询一个正好过期的数据。
     *          解决：加锁；？默认是无加锁的;
     *               sync = true（加本地锁【本地锁：有多少服务就有多少锁就有多少数据库操作被放行，这样问题不大，不会有超多请求查询数据库】，可以解决击穿）
         * 缓存雪崩：大量的key同时过期。
     *                  解决：加随机时间。加上过期时间就行（存储的时间不一样，那么过期的时间自然也就不一样）：time-to-live=3600000
     * 2）、写模式：Spring-Cache并没有考虑，我们自己可以考虑：
             * 1）、读写加锁。（适用于读多写少的系统）
             * 2）、引入Canal，感知到 MySQL 的更新去更新数据库
             * 3）、加上过期时间，一段时间后缓存与数据库最终一致即可（看自己对与系统数据实时性观察的容忍性了）
             * 4）、读多写多，直接去数据库查询就行，别用缓存
     * 3）、总结：
             * 常规数据（读多写少，即时性，一致性要求不高的数据）；完全可以使用Spring-Cache；
     *                                                       读模式：Spring-Cache全部解决了问题；写模式（只要缓存的数据有过期时间就足够了）
             * 特殊数据（比如实时性要求高/经常写/......）：特殊设计
     *
     */

    /**
     * 查询1级分类
     */
//    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
//    @Cacheable(value = {"category"}, key = "'listLevel1Categorys'", sync = true)
//    @Cacheable(value = {"category"})
    @Override
    public List<CategoryEntity> listLevel1Categorys() {
        System.out.println("listLevel1Categorys查询了数据库.....");

        List<CategoryEntity> categoryEntities = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
//        return null; // 测试缓存空结果
    }

    /*
        public static void main(String[] args) {
        new ArrayList<Integer>().stream()
                .sorted((menu1, menu2) -> {
                            System.out.println(menu1);
                            System.out.println(menu2);
                            return (menu1  == null ? 0 : menu1) - (menu2 == null ? 0 : menu2 );
                        }
                        )
                .collect(Collectors.toList());
        System.out.println("不会报错");
    }
     */

    @Override
//    @Cacheable(value = "category", key = "#root.methodName")
    @Cacheable(value = "category", key = "#root.methodName", sync = true)
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询了数据库.....");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //封装数据
        Map<String, List<Catelog2Vo>> catgoryData = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            //2、封装上面面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return catgoryData;
    }

//    =============================================================================================================

    //TODO 产生堆外内存溢出：OutOfDirectMemoryError【堆外内存溢出】
    //1）、springboot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信。
    //2）、lettuce的bug导致netty堆外内存溢出 -Xmx300m；netty如果没有指定堆外内存，默认使用-Xmx300m
    //   可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存。
    //1）、升级lettuce客户端。   2）、切换使用jedis
    //  redisTemplate：
    //  lettuce、jedis操作redis的底层客户端。Spring再次封装redisTemplate；
//    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson1() {
        //给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型；【序列化与反序列化】JSON跨语言，跨平台兼容。
        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         * 3、加锁：解决缓存击穿
         */
        //1、加入缓存逻辑,缓存中存的数据是json字符串。
        String catalogJSON = getCatalogJSON();
        if (StringUtils.isEmpty(catalogJSON)) {
            //2、缓存中没有,查询数据库
            //保证数据库查询完成以后，将数据放在redis中，这是一个原子操作。在同一把锁内完成
            System.out.println("缓存不命中....将要查询数据库...");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisson();
//            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
//            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithLocalLock();
//            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDB();
            return catalogJsonFromDb;
        }

        System.out.println("缓存命中....直接返回....");
        //转为我们指定的对象。
        Map<String, List<Catelog2Vo>> result = getJSONData(catalogJSON);
        return result;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
//        //1、如果缓存中有就用缓存的
//        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
//        if(cache.get("catalogJson") == null) {
//            //调用业务  xxxxx
//            //返回数据又放入缓存
//            cache.put("catalogJson",parent_cid);
//        }
//        return catalogJson;

        //只要是同一把锁，就能锁住需要这个锁的所有线程
        //1、synchronized (this)：SpringBoot所有的组件在容器中都是单例的。
        //TODO 本地锁：synchronized，JUC（Lock），在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            return getCatalogJsonFromDB();
        }
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {
        //1、占分布式锁。去redis占坑
        String uuid = UUID.randomUUID().toString();
//        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid);
//        加锁和设置过期时间保证原子性
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);

        if (lock) {
            System.out.println("获取分布式锁成功...");
            //加锁成功... 执行业务
            //2、设置过期时间，必须和加锁是同步的，原子的
//            redisTemplate.expire("lock",30,TimeUnit.SECONDS);

            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getCatalogJsonFromDB();
            } finally {
//           TODO 写博客     lua脚本解锁    原子操作
//                Redis分布式锁，核心：
//                                  - 加锁和过期时间保证原子性
//                                  - 解锁保证原子性
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                Long delResult = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return dataFromDb;

//            Map<String, List<Catelog2Vo>> dataFromDb = getCatalogJsonFromDB();
//            //获取值对比+对比成功删除==原子操作  lua脚本解锁
//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)){
//                //删除我自己的锁
//                redisTemplate.delete("lock");//删除锁
//            }
//            return dataFromDb;
        } else {
            //加锁失败...重试。synchronized ()
            //休眠200ms重试
            System.out.println("获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {
            }
            return getCatalogJsonFromDbWithRedisLock();//自旋的方式
        }
    }

    /**
     * 通过加锁解决了缓存击穿，确保了读取缓存没问题之后，又来了新的问题：
     * 那就是：缓存里面的数据如何和数据库保持一致
     * 也就是缓存数据一致性
         * 1）、双写模式
         * 2）、失效模式
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisson() {
        //锁的名字: 锁的粒度，越细越快。[锁的一个名字就是一个锁,不同名字就是不同的锁]
        //锁的粒度：具体缓存的是某个数据，11-号商品；  product-11-lock   product-12-lock

//        加锁和设置过期时间保证原子性
//      Redisson的所有操作都是发送Lua脚本,所以都保证了原子性
        RLock lock = redisson.getLock(RedisConstant.CATEGORY_KEY);
//        lock.lock();
        lock.lock(10, TimeUnit.SECONDS);
        System.out.println("获取分布式锁成功...");
        //加锁成功... 执行业务
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {
            dataFromDb = getCatalogJsonFromDB();
        } finally {
            //删除锁
            lock.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDB() {
        String catalogJSON = getCatalogJSON();
        if (!StringUtils.isEmpty(catalogJSON)) {
            //缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = getJSONData(catalogJSON);
            return result;
        }

        System.out.println("查询了数据库.....");
        List<CategoryEntity> selectList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);
        //2、封装数据
        Map<String, List<Catelog2Vo>> catgoryData = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            //2、封装上面面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);
                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        //3、查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(catgoryData);
        redisTemplate.opsForValue().set(RedisConstant.CATEGORY_KEY, s, 1, TimeUnit.DAYS);
//        redisTemplate.opsForValue().set(RedisConstant.CATEGORY_KEY, s);

        return catgoryData;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> all, Long parent_cid) {
        List<CategoryEntity> collect = all.stream().filter(item -> Objects.equals(item.getParentCid(), parent_cid)).collect(Collectors.toList());
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private String getCatalogJSON() {
        return redisTemplate.opsForValue().get(RedisConstant.CATEGORY_KEY);
    }

    private Map<String, List<Catelog2Vo>> getJSONData(String catalogJSON) {
        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

}