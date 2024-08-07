package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model, HttpServletRequest httpServletRequest, HttpSession session) {

//        /**
//         * 手动获取Cookie
//         */
//        Cookie[] cookies = httpServletRequest.getCookies();
//        if (null != cookies && cookies.length > 0) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equalsIgnoreCase("GULIMALL")) {
//                    String loginUserKey = cookie.getValue();
//                    String json = stringRedisTemplate.opsForValue().get(loginUserKey);
//                    MemberRespVo loginUser = JSON.parseObject(json, new TypeReference<MemberRespVo>(){});
//                    session.setAttribute("loginUser", loginUser);
//                }
//            }
//        }


//        /**
//         * 手动获取session
//         */
//        Cookie[] cookies = httpServletRequest.getCookies();
//        if (null != cookies && cookies.length > 0) {
//            for (Cookie cookie : cookies) {
//                if (cookie.getName().equals(AuthServerConstant.REDIS_SESSION_ID_KEY)) {
//                    String rsessionId = cookie.getValue();
//                    String rsessionJson = stringRedisTemplate.opsForValue().get(rsessionId);
//                    HashMap<String, String> rsession;
//                    if (StringUtils.isEmpty(rsessionJson)) {
//                        rsession = new HashMap<>();
//                    } else {
//                        rsession = JSON.parseObject(rsessionJson, HashMap.class);
//                    }
//                    String s = rsession.get(AuthServerConstant.LOGIN_USER);
//                    if (!StringUtils.isEmpty(s)) {
//                        MemberRespVo loginUser = JSON.parseObject(s, new TypeReference<MemberRespVo>() {
//                        });
//                        session.setAttribute(AuthServerConstant.LOGIN_USER, loginUser);
//                    }
//                }
//            }
//        }
//        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
//        model.addAttribute(AuthServerConstant.LOGIN_USER, attribute);
//        System.out.println("thymeleaf可以直接获取session中的属性进行使用：" + attribute);


        List<CategoryEntity> categorys = categoryService.listLevel1Categorys();
        model.addAttribute("categorys", categorys);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //2、加锁，不会有死锁问题，而且还会自动续期
//        lock.lock(); //阻塞式等待，知道获取到锁。Redisson有一个看门狗，可以给锁自动续期，默认加的锁都是30s时间。
        //1）、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //2）、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。

//        lock.lock(10,TimeUnit.SECONDS); //10秒自动解锁,无需unlock。自动解锁时间一定要大于业务的执行时间。
        //问题：lock.lock(10,TimeUnit.SECONDS); 在锁时间到了以后，即使业务没有执行完成，也不会自动续期。
        //1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //2、如果我们未指定锁的超时时间，就使用30 * 1000【LockWatchdogTimeout看门狗的默认时间】;
        //    只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】,每隔10s都会自动再次续期，续成30s
        //    internalLockLeaseTime【看门狗时间】 / 3        =      10s后续期

        //最佳实战
        lock.lock(30, TimeUnit.SECONDS); //省掉了整个续期操作。业务时间给大一点然后手动解锁
        try {
//            业务代码
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
//            Thread.sleep(30000); //模拟超长任务
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3、解锁  假设解锁代码没有运行，redisson会不会出现死锁   答案：不会出现死锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }


    //保证一定能读到最新数据,修改期间，写锁是一个排他锁（互斥锁、独享锁）。读锁是一个共享锁,大家都能用,加了跟没加一样
    //写锁没释放读就必须等待
    // 读 + 读： 相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
    // 写 + 读： 等待写锁释放
    // 写 + 写： 阻塞方式
    // 读 + 写： 有读锁。写也需要等待。
    // 只要有写的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String writeValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        //1、改数据加写锁，读数据加读锁
        rLock.lock();
        try {
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放" + Thread.currentThread().getId());
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
//        ReentrantReadWriteLock writeLock = new ReentrantReadWriteLock();
        String s = "";
        //加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功" + Thread.currentThread().getId());
            s = stringRedisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放" + Thread.currentThread().getId());
        }

        return s;
    }

    /**
     * 车库停车，
     * 3车位[事先在redis中放key:park,value:3]
     * 信号量也可以用作分布式限流；
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire();//获取一个信号，获取一个值,占一个车位
        boolean b = park.tryAcquire();
        if (b) { //信号量也可以用作分布式限流
            //执行业务
        } else {
            return "error";
        }

        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个车位

//        Semaphore semaphore = new Semaphore(5);
//        semaphore.release();
//        semaphore.acquire();

        return "ok";
    }


    /**
     * 放假，锁门
     * 1班没人了，2班没人了......
     * 5个班全部走完，我们可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); //等待闭锁都完成

        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数减一；

//        CountDownLatch

        return id + "班的人都走了...";
    }
}
