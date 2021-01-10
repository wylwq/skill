package com.htwz.skill.controller;

import com.htwz.skill.annotation.Ratelimiter;
import com.htwz.skill.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 15:51
 * @Version V1.0.0
 **/
@RestController
@RequestMapping("/skill/")
public class SkillController {

    @Autowired
    private OrderService orderService;

    /**
     * 使用悲观锁解决超卖问题，注意同步关键字需要加在控制层
     *
     * @param id
     * @return
     */
//    @GetMapping("kill")
//    public String killV1(Integer id) {
//        try{
//            System.out.println("秒杀商品的id= " + id);
//            synchronized (this) {
//                //根据秒杀商品的id 去调用秒杀业务
//                Integer orderId = orderService.kill(id);
//                return "秒杀成功：订单id：" + orderId;
//            }
//        } catch (Exception e) {
//            return e.getMessage();
//        }
//
//    }

//    /**
//     * 使用乐观锁解决超卖问题
//     *
//     * @param id
//     * @return
//     */
//    @GetMapping("kill")
//    public String killV2(Integer id) {
//        try{
//            Integer orderId = orderService.killV2(id);
//            return "秒杀成功：订单id：" + orderId;
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return e.getMessage();
//        }
//
//    }

//    /**
//     * 使用乐观锁解决超卖问题和令牌桶算法解决高并发限流问题（可到每个用户的请求频率限制）
//     *
//     * @param id
//     * @return
//     */
//    @GetMapping("kill")
//    @Ratelimiter(limit = 20, timeout = 1000)
//    public String killV3(Integer id) {
//        try{
//            Integer orderId = orderService.killV2(id);
//            return "秒杀成功：订单id：" + orderId;
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return e.getMessage();
//        }
//
//    }

//    /**
//     * 使用乐观锁解决超卖问题和令牌桶算法解决高并发限流问题（可到每个用户的请求频率限制）
//     * 添加秒杀接口隐藏逻辑
//     *
//     * @param id
//     * @return
//     */
//    @GetMapping("kill")
//    @Ratelimiter(limit = 20, timeout = 1000)
//    public String killV4(Integer id, Integer userId, String md5) {
//        try{
//            Integer orderId = orderService.killV4(id, userId, md5);
//            return "秒杀成功：订单id：" + orderId;
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return e.getMessage();
//        }
//
//    }

    /**
     * 订单异步处理（目前是采用线程，也可以是用消息队列处理（推荐）），限制每个用户秒杀商品的数量，比如每个用户只能秒杀一件商品
     *
     * @param id
     * @return
     */
    @GetMapping("kill")
    @Ratelimiter(limit = 20, timeout = 1000)
    public String killV4(Integer id, Integer userId, String md5) {
        try{
            Integer orderId = orderService.killV5(id, userId, md5);
            return "秒杀成功：订单id：" + orderId;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return e.getMessage();
        }

    }

    /**
     * 生成md5,用途是隐藏秒杀接口，前端调用秒杀接口的时候需要创建这个md5，并且判断这个md5合法性校验
     * 这个接口存在一个问题：没有进行并发处理，解决方案一：提前一定时间将数据库的所有用户根据一定的规则生成好md5
     * 存放在redis中，当前端调用这个接口的时候直接从redis中取值就可以了，这种做法还是有问题，我们什么时候生成md5？
     * 并且用户数很多的时候还是会存在一定问题？
     * 解决方案二（推荐）:前端与后端约定好生成一个唯一token的规则，调用秒杀接口的时候，前端生成这个token传给后端
     * 然后后端根据这个规则去校验是否合法，方案二可以有效解决并发问题
     *
     * @param id
     * @param userId
     * @return
     */
    @GetMapping("md5")
    public String getMd5(Integer id, Integer userId) {
        String md5;
        try{
            md5 = orderService.getMd5(id, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "获取md5失败：" + e.getMessage();
        }
        return "获取的MD5信息为: " + md5;
    }


}
