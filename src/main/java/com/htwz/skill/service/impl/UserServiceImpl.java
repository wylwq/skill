package com.htwz.skill.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.htwz.skill.mapper.UserMapper;
import com.htwz.skill.model.User;
import com.htwz.skill.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author wangy
 * @Date 2021/1/9 21:34
 * @Version V1.0.0
 **/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
