package com.htwz.skill.thread;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.htwz.skill.model.OrderRecord;
import com.htwz.skill.model.Stock;
import com.htwz.skill.model.StockOrder;
import com.htwz.skill.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/10 15:08
 * @Version V1.0.0
 **/
@Service
public class OrderCreateThread implements Runnable {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private StockService stockService;

    @Override
    public void run() {
        String o = stringRedisTemplate.boundListOps(OrderRecord.class.getSimpleName()).rightPop();

        if (!StrUtil.isBlank(o)) {
            OrderRecord orderRecord = JSONObject.parseObject(o, OrderRecord.class);
            //从redis获取秒杀商品
            Stock goods = (Stock) stringRedisTemplate.boundHashOps("goods:" + orderRecord.getId()).get(orderRecord.getId());
            //生成秒杀订单，将订单保存到redis缓存
            StockOrder stockOrder = new StockOrder();
            stockOrder.setSid(goods.getId());
            stockOrder.setName(goods.getName());
            stringRedisTemplate.boundHashOps("order:" + goods.getId()).put(orderRecord.getUserId(), stockOrder);
            synchronized (OrderCreateThread.class) {
                goods = (Stock) stringRedisTemplate.boundHashOps("goods:" + orderRecord.getId()).get(orderRecord.getId());
                //秒杀商品库存-1，
                //判断库存量是否<=0
                goods.setSale(goods.getSale() + 1);
                if (goods.getCount().equals(goods.getSale())) {
                    //如果是将秒杀商品更新到数据库，删除秒杀商品缓存
                    Stock updateStock = new Stock();
                    updateStock.setId(goods.getId());
                    updateStock.setSale(goods.getSale());
                    updateStock.setVersion(goods.getVersion());
                    stockService.updateById(updateStock);
                    stringRedisTemplate.boundHashOps("goods:" + goods.getId()).delete(goods.getId());
                } else {
                    //否，将秒杀商品更新到缓存，返回成功
                    stringRedisTemplate.boundHashOps("goods:" + goods.getId()).put(goods.getId(), goods);
                }
            }
        }

    }
}
