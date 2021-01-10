package com.htwz.skill.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htwz.skill.mapper.StockOrderMapper;
import com.htwz.skill.model.OrderRecord;
import com.htwz.skill.model.Stock;
import com.htwz.skill.model.StockOrder;
import com.htwz.skill.model.User;
import com.htwz.skill.service.OrderService;
import com.htwz.skill.service.StockService;
import com.htwz.skill.service.UserService;
import com.htwz.skill.thread.OrderCreateThread;
import com.htwz.skill.util.ThreadPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 15:54
 * @Version V1.0.0
 **/
@Service
@Transactional
public class OrderServiceImpl extends ServiceImpl<StockOrderMapper, StockOrder> implements OrderService {

    @Autowired
    private StockService stockService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderCreateThread orderCreateThread;

    @Override
    public Integer kill(Integer id) {
        //根据商品id校验库存
        Stock stock = stockService.getById(id);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("商品已经卖完~");
        }
        Stock updateStock = new Stock();
        updateStock.setId(stock.getId());
        //扣除库存
        updateStock.setSale(stock.getSale() + 1);
        //updateStock.setVersion(stock.getVersion());
        //更新库存
        stockService.updateById(updateStock);
        //创建订单
        StockOrder stockOrder = new StockOrder();
        stockOrder.setName(stock.getName());
        stockOrder.setSid(stock.getId());
        save(stockOrder);
        return stockOrder.getId();
    }

    @Override
    public Integer killV2(Integer id) {
        //校验Redis中的秒杀商品是否超时
        Boolean hasKey = stringRedisTemplate.hasKey("kill:" + id);
        if (!hasKey) {
            throw new RuntimeException("当前商品的抢购活动已经结束了~");
        }
        Stock stock = this.checkStock(id);
        this.updateSale(stock);
        Integer orderId = this.createOrder(stock);
        return orderId;
    }

    @Override
    public Integer killV4(Integer id, Integer userId, String md5) {
        //判断是否到商品秒杀时间
        Long skillTime = getSkillTime();
        if (System.currentTimeMillis() < skillTime) {
            throw new RuntimeException("商品秒杀还没开始~");
        }
        //校验Redis中的秒杀商品是否超时
        Boolean hasKey = stringRedisTemplate.hasKey("kill:" + id);
        if (!hasKey) {
            throw new RuntimeException("当前商品的抢购活动已经结束了~");
        }
        //校验md5是否合法
        String oldMd5 = stringRedisTemplate.opsForValue().get(getHashKey(userId, id));
        if (md5 == null || !md5.equals(oldMd5)) {
            throw new RuntimeException("用户非法的请求~");
        }

        Stock stock = this.checkStock(id);
        this.updateSale(stock);
        Integer orderId = this.createOrder(stock);
        return orderId;
    }

//    @Override
//    public Integer killV5(Integer id, Integer userId, String md5) {
//        //判断该商品是否到秒杀时间(根据业务需要进行修改)
//        Long skillTime = getSkillTime();
//        if (System.currentTimeMillis() < skillTime) {
//            throw new RuntimeException("商品秒杀还没开始~");
//        }
//        //校验md5是否合法（隐藏秒杀接口）
//        String oldMd5 = stringRedisTemplate.opsForValue().get(getHashKey(userId, id));
//        if (md5 == null || !md5.equals(oldMd5)) {
//            throw new RuntimeException("用户非法的请求~");
//        }
//        //从redis获取秒杀商品
//        Stock goods = (Stock) stringRedisTemplate.boundHashOps("goods:" + id).get(id);
//        //判断商品是否存在，或库存是否<=0
//        if (null == goods || goods.getCount() <= 0) {
//            //如果商品不存在，或库存<=0，返回失败，提示商品已售空或秒杀活动已经结束
//            throw new RuntimeException("商品已售空或秒杀活动已经结束~");
//        }
//        //生成秒杀订单，将订单保存到redis缓存
//        StockOrder stockOrder = new StockOrder();
//        stockOrder.setSid(goods.getId());
//        stockOrder.setName(goods.getName());
//        stringRedisTemplate.boundHashOps("order:" + id).put(userId, stockOrder);
//        //秒杀商品库存-1，
//        //判断库存量是否<=0
//        goods.setSale(goods.getSale() + 1);
//        if (goods.getCount().equals(goods.getSale())) {
//            //如果是将秒杀商品更新到数据库，删除秒杀商品缓存
//            Stock updateStock = new Stock();
//            updateStock.setId(goods.getId());
//            updateStock.setSale(goods.getSale());
//            updateStock.setVersion(goods.getVersion());
//            stockService.updateById(updateStock);
//            stringRedisTemplate.boundHashOps("goods:" + id).delete(id);
//        } else {
//            //否，将秒杀商品更新到缓存，返回成功
//            stringRedisTemplate.boundHashOps("goods:" + id).put(id, goods);
//        }
//        return 200;
//    }

    @Override
    public Integer killV5(Integer id, Integer userId, String md5) {
        //判断该商品是否到秒杀时间(根据业务需要进行修改)
        Long skillTime = getSkillTime();
        if (System.currentTimeMillis() < skillTime) {
            throw new RuntimeException("商品秒杀还没开始~");
        }
        //校验md5是否合法（隐藏秒杀接口）
        String oldMd5 = stringRedisTemplate.opsForValue().get(getHashKey(userId, id));
        if (md5 == null || !md5.equals(oldMd5)) {
            throw new RuntimeException("用户非法的请求~");
        }
        //从用户的set集合中判断用户是否已下单
        Boolean member = stringRedisTemplate.boundSetOps("U_" + id + "_" + userId).isMember(userId);
        if (member) {
            System.out.println("对不起，您正在排队等待支付，请尽快支付~");
            return 0;
        }
        //如果正在排队或者已支付，提示用户你正在排队或者有订单未支付
        //从队列中获取秒杀商品id(使用redis队列解决超卖问题)
        String goodsId = stringRedisTemplate.boundListOps("goods:" + id).rightPop();
        //判断商品是否存在，或库存是否<=0
        if (goodsId == null || "".equals(goodsId)) {
            //如果商品不存在，或库存<=0，返回失败，提示商品已售空或秒杀活动已经结束
            throw new RuntimeException("商品已售空或秒杀活动已经结束~");
        }
        //将用户放入用户集合
        stringRedisTemplate.boundSetOps("U_user").add("" + userId);
        //创建OrderRecord对象记录用户下单信息，用户id，商品id，放到OrderRecord队列中
        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setId(id);
        orderRecord.setUserId(userId);
        String o = JSON.toJSONString(orderRecord);
        stringRedisTemplate.boundListOps(OrderRecord.class.getSimpleName()).leftPush(o);
        ThreadPoolExecutor pool = ThreadPoolUtil.getPool();
        pool.execute(orderCreateThread);
        return 200;
    }

    private Long getSkillTime() {
        //根据业务系统修改
        return System.currentTimeMillis() + 10 * 60 * 1000;
    }

    @Override
    public String getMd5(Integer id, Integer userId) {
        //校验用户的合法性
        User user = userService.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在~");
        }
        //校验商品的合法性
        Stock stock = stockService.getById(id);
        if (stock == null) {
            throw new RuntimeException("商品信息有误~");
        }
        //生成hashkey
        String hashKey = getHashKey(userId, id);
        //生成md5
        String md5Hex = DigestUtil.md5Hex(userId + id + "ABC");
        stringRedisTemplate.opsForValue().set(hashKey, md5Hex, 3600, TimeUnit.SECONDS);
        System.out.println("redis写入：" + hashKey +"," + md5Hex);
        return md5Hex;
    }

    private String getHashKey(Integer userId, Integer id) {
        return "KEY_" + userId + "_" + id;
    }

    /**
     * 校验库存
     *
     * @param id
     * @return
     */
    private Stock checkStock(Integer id) {
        Stock stock = stockService.getById(id);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("商品已经卖完~");
        }
        return stock;
    }

    /**
     * 扣除库存
     *
     * @param stock
     */
    private void updateSale(Stock stock) {
        Stock updateStock = new Stock();
        updateStock.setId(stock.getId());
        //扣除库存
        updateStock.setSale(stock.getSale() + 1);
        updateStock.setVersion(stock.getVersion());
        //更新库存
        if (!stockService.updateById(updateStock)) {
            throw new RuntimeException("抢购失败，请重试~");
        }
    }

    /**
     * 创建订单
     *
     * @param stock
     * @return
     */
    private Integer createOrder(Stock stock) {
        //创建订单
        StockOrder stockOrder = new StockOrder();
        stockOrder.setName(stock.getName());
        stockOrder.setSid(stock.getId());
        save(stockOrder);
        return stockOrder.getId();
    }

    @Scheduled(cron = "0/30 * * * * ?")
    public void importToRedis() {
        //从数据库中查询合法的商品，状态是秒杀的，并且库存>0的商品
        Collection<Stock> stocks = stockService.listByIds(Collections.singletonList(1));
        Iterator<Stock> iterator = stocks.iterator();
        while (iterator.hasNext()) {
            Stock next = iterator.next();
            stringRedisTemplate.boundHashOps("goods:" + next.getId()).put(next.getId(), next);
            //为每一个商品商品创建一个队列
            createGoodsQueue(next.getId(), next.getCount(), next.getSale());
        }
    }

    private void createGoodsQueue(Integer id, Integer count, Integer sale) {
        int subCount = count - sale;
        if (subCount > 0) {
            for (int i = 0; i < subCount; i++) {
                stringRedisTemplate.boundListOps("Q_" + id).leftPush("" + id);
            }
        }
    }
}
