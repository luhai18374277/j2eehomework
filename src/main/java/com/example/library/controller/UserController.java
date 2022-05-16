package com.example.library.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.library.common.R;
import com.example.library.entity.User;
import com.example.library.mapper.UserMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Api(description = "普通用户相关接口")
public class UserController {
    @Resource
    private UserMapper userMapper;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/register")
    @ApiOperation(notes = "(不需要邮箱了！！！！)需要用户名、密码，用户名不能重复", value = "注册")
    public R register(@RequestBody User user) {
        if(user.getName() == null)
            return R.fail("缺少用户名参数");
        if(user.getPwd()==null)
            return R.fail("缺少密码参数");
        user.setPwd(DigestUtils.md5DigestAsHex(user.getPwd().getBytes()));
        String username = user.getName();

        QueryWrapper<User> wrapper1 = new QueryWrapper<>();
        wrapper1.eq("name", username);
        User user1 = userMapper.selectOne(wrapper1);
        if (user1 != null) {
            System.out.println("failname\n" + user1.toString());
            return R.fail("用户名已注册！");
        } else {

//            user.setIdentity(1);//普通用户
//            user.setState(0); //未封禁
//            user.setIsAdmin(false);

            userMapper.insert(user);

            return R.success(user);
        }
    }

    @PostMapping("/login")
    @ApiOperation(notes = "使用用户密码", value = "登录")
    public R login(@RequestBody User user, HttpSession session) {

        String name = user.getName();
        String password = user.getPwd();
        Map<String, Object> map = new HashMap<>();
        try {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("name", name);
            User user1 = userMapper.selectOne(wrapper);

            if (user1 != null) {
//                if (user1.getState() == 2) {
//                    long seconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(user1.getBanDate().getTime()),
//                            Instant.ofEpochMilli(new Date().getTime()));  //计算时间
//                    System.out.println(seconds);
//                    if (seconds < 0) {
//                        return R.fail("用户已被封禁至"+user1.getBanDate());
//                    }
//                    else {
//                        user1.setState(0);
//                        user1.setBanDate(null);
//                    }
//                }

                if (DigestUtils.md5DigestAsHex(user.getPwd().getBytes()).equals(user1.getPwd())) {
//                    user1.setLoginDate(new Date());
//                    userMapper.update(user1, wrapper);  //更新登录时间
                    session.setAttribute("loginUser", user1);
                    session.setMaxInactiveInterval(60 * 60);  //

                    return R.success(user1);
                } else {
                    return R.fail("密码错误");
                }
            } else {
                return R.fail("不存在该用户名");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }


}