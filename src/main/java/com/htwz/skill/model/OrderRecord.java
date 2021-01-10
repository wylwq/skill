package com.htwz.skill.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/10 15:10
 * @Version V1.0.0
 **/
@Data
public class OrderRecord implements Serializable {

    private Integer id;

    private Integer userId;
}
