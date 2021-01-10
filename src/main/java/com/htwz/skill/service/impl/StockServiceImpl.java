package com.htwz.skill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htwz.skill.mapper.StockMapper;
import com.htwz.skill.model.Stock;
import com.htwz.skill.service.StockService;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 16:09
 * @Version V1.0.0
 **/
@Service
public class StockServiceImpl extends ServiceImpl<StockMapper, Stock> implements StockService {
}
