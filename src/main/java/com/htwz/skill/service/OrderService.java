package com.htwz.skill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.htwz.skill.model.StockOrder;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 15:54
 * @Version V1.0.0
 **/
public interface OrderService extends IService<StockOrder> {


    Integer kill(Integer id);

    Integer killV2(Integer id);

    Integer killV4(Integer id, Integer userId, String md5);

    Integer killV5(Integer id, Integer userId, String md5);

    String getMd5(Integer id, Integer userId);

}
