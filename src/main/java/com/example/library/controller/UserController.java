package com.example.library.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.example.library.common.R;
import com.example.library.entity.User;
import com.example.library.mapper.UserMapper;
import com.example.library.service.MailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.example.library.common.VerCodeGenerateUtil.generateVerCode;

@RestController
@RequestMapping("/user")
//@Api(description = "普通用户相关接口")
public class UserController {
    @Resource
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @PostMapping("/register")
//    @ApiOperation(notes = "(不需要邮箱了！！！！)需要用户名、密码，用户名不能重复", value = "注册")
    public R register(@RequestBody User user) {
        if(user.getName() == null)
            return R.fail("缺少用户名参数");
        if(user.getPassword()==null)
            return R.fail("缺少密码参数");
        user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
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
            user.setCreateTime(new Date());
            userMapper.insert(user);

            return R.success(user);
        }
    }

    @PostMapping("/login")
    @ApiOperation(notes = "使用用户密码", value = "登录")
    public R login(@RequestBody User user, HttpSession session) {

        String name = user.getName();
        String password = user.getPassword();
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

                if (DigestUtils.md5DigestAsHex(user.getPassword().getBytes()).equals(user1.getPassword())) {
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

    @PostMapping("/logout")
    public R logout(HttpSession session) {
        try {
            if (session.getAttribute("loginUser") == null) {
                return R.fail("未登录");
            }
            session.removeAttribute("loginUser");
            if (session.getAttribute("loginUser") == null) {
                System.out.println("用户信息已清除，已成功退出登录");
            }
            return R.success("登出成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("登出错误");
        }
    }

    @PostMapping("/editinfo")
    public R editinfo(@RequestBody User user, HttpSession session) {
        if (user.getId() == null) {
            return R.fail("id不存在");
        }
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        if (user.getName()!=null){
            if (!userMapper.selectById(user.getId()).getName().equals(user.getName())){
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name",user.getName());
                if (userMapper.selectCount(queryWrapper) != 0){
                    return R.fail("用户名存在");
                }
            }
        }
        if (userMapper.updateById(user) == 0) {
            return R.fail("用户不存在");
        }
        return R.success("修改成功");
    }

    @PostMapping("/testvercode")
    public R testvercode(@RequestBody JSONObject json, HttpSession session) {
        try {
            if (json.get("email")==null){
                return R.fail("邮箱不能为空");
            }
            String email = (String) json.get("email");
            String verCode = mailService.sendEmailVerCode(email, generateVerCode());
            return R.success("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("邮件发送失败");
        }
    }


    @PostMapping("/vercode")
    public R vercode(@RequestBody JSONObject json, HttpSession session) {
        try {
            if (session.getAttribute("loginUser") == null) {
                return R.fail("此操作需要先登录");
            }
            User user = (User) session.getAttribute("loginUser");

            if(user.getCodeDate()!=null){
                long seconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(user.getCodeDate().getTime()),
                        Instant.ofEpochMilli(new Date().getTime()));  //计算时间
                System.out.println(seconds);
                if (seconds < 60) {
                    return R.fail("一分钟内只能获取一次验证码");
                }
            }
            if(user.getEmail()==null) return R.fail("您没有邮箱或邮箱错误");
            String verCode = mailService.sendEmailVerCode(user.getEmail(), generateVerCode());

            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("name", user.getName());
            user.setCodeDate(new Date());  //设置验证码获得时间
            user.setVercode(verCode);
            userMapper.update(user, wrapper);
            session.setAttribute("loginUser", user); //更新session

            System.out.println("用户" + user.getId() + "的验证码为" + user.getVercode());
            return R.success("邮件发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("邮件发送失败");
        }
    }

    @PostMapping("/sendvercodebyemail")
    public R sendvercodebyemail(@RequestBody JSONObject json, HttpSession session) {
        try {
            if (json.get("email")==null){
                return R.fail("邮箱不能为空");
            }
            String email = (String) json.get("email");
            String verCode = mailService.sendEmailVerCode(email, generateVerCode());
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("email", email);
            User user = userMapper.selectOne(wrapper);
            user.setVercode(verCode);
            user.setCodeDate(new Date());
            userMapper.update(user, wrapper);
            session.setAttribute("resetpwdUser", user); //更新session
            System.out.println("用户" + user.getId() + "的验证码为" + user.getVercode());

            return R.success("邮件发送成功");
            //跳转到resetpwd
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("邮件发送失败");
        }
    }

    @PostMapping("/resetpwd")
    public R resetpwd(@RequestBody JSONObject json, HttpSession session) {
        try {
            if (session.getAttribute("resetpwdUser") == null) {
                return R.fail("没有要重置验证码的用户");
            }
            User user = (User) session.getAttribute("resetpwdUser");

            if(json.get("vercode")==null) return R.fail("请输入验证码");
            String vercode=(String) json.get("vercode");
            if(json.get("password")==null) return R.fail("请输入新密码");
            String password=(String) json.get("password");

            if(user.getCodeDate()!=null){
                long seconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(user.getCodeDate().getTime()),
                        Instant.ofEpochMilli(new Date().getTime()));  //计算时间
//                System.out.println(seconds);
                if (seconds >600) {
                    return R.fail("超时");
                }
                if(!vercode.equals(user.getVercode())){
                    return R.fail("验证码错误");
                }
                user.setPassword(DigestUtils.md5DigestAsHex(password.getBytes()));
                QueryWrapper<User> wrapper = new QueryWrapper<>();
                wrapper.eq("email", user.getEmail());
                userMapper.update(user, wrapper);
                session.removeAttribute("resetpwdUser");
                session.setAttribute("loginUser", user); //更新session

                return R.success("密码修改成功");
            }else {
                return R.fail("没有获取验证码时间，这不正常");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("reset错误");
        }
    }

}