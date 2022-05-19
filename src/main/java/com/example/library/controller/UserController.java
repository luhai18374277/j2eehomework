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

//    @PostMapping("/testvercode")
//    @ApiOperation(notes = "需登录，参数为(作者id)authorId和(邮箱)mail，成功则跳转到/scholar/vercode进行验证码确认", value = "认证学者")
//    public R beScholar(@RequestBody JSONObject json, HttpSession session) {
//        try {
//            if (session.getAttribute("loginUser") == null) {
//                return R.fail("此操作需要先登录");
//            }
//            User user = (User) session.getAttribute("loginUser");
//            if (user.getIdentity() == 2) {
//                return R.fail("你已经是学者了");
//            }
//            if (json.get("authorId") == null) {
//                return R.fail("作者id不能为空");
//            }
//            if (json.get("mail")==null){
//                return R.fail("邮箱不能为空");
//            }
//
//            String mail = (String) json.get("mail");
//            QueryWrapper<User> wrapper0 = new QueryWrapper<>();
//            wrapper0.eq("mail", mail);
//            User user1 = userMapper.selectOne(wrapper0);
//            if (user1 != null) {
//                return R.fail("邮箱已被使用！如有必要请进行申诉");
//            }
//
//
//            JSONArray papers = paperService.getPaperByAuthorId((String) json.get("authorId"));
//            if(papers==null){
//                return R.fail("错误的authorId？(根据authorId未找到任何paper)");
//            }  //
//
//            if(scholarMapper.selectByAuthorId((String) json.get("authorId"))!=null){
//                return R.fail("该authorId已被认领，如果是本人认领请勿再进行认领操作，如果是冒领请进行申诉");
//            }
//            if(user.getCodeDate()!=null){
//                long seconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(user.getCodeDate().getTime()),
//                        Instant.ofEpochMilli(new Date().getTime()));  //计算时间
//                System.out.println(seconds);
//                if (seconds < 60) {
//                    return R.fail("一分钟内只能获取一次验证码");
//                }
//            }
//
//            String verCode = mailService.sendEmailVerCode(mail, generateVerCode());
//
//            QueryWrapper<User> wrapper = new QueryWrapper<>();
//            wrapper.eq("name", user.getName());
//            user.setCodeDate(new Date());  //设置验证码获得时间
//            user.setVercode(verCode);
//            userMapper.update(user, wrapper);
//            session.setAttribute("loginUser", user); //更新session
//            session.setAttribute("authorId", json.get("authorId"));
//            session.setAttribute("mail", mail);
//            System.out.println("用户" + user.getId() + "的验证码为" + user.getVercode());
//            return R.success("邮件发送成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return R.fail("邮件发送失败");
//        }
//    }

//    @PostMapping("/vercode")
//    @ApiOperation(notes = "需登录，参数为(作者id)authorId和(邮箱)mail，成功则跳转到/scholar/vercode进行验证码确认", value = "认证学者")
//    public R beScholar(@RequestBody JSONObject json, HttpSession session) {
//        try {
//            if (session.getAttribute("loginUser") == null) {
//                return R.fail("此操作需要先登录");
//            }
//            User user = (User) session.getAttribute("loginUser");
//            if (user.getIdentity() == 2) {
//                return R.fail("你已经是学者了");
//            }
//            if (json.get("authorId") == null) {
//                return R.fail("作者id不能为空");
//            }
//            if (json.get("mail")==null){
//                return R.fail("邮箱不能为空");
//            }
//
//            String mail = (String) json.get("mail");
//            QueryWrapper<User> wrapper0 = new QueryWrapper<>();
//            wrapper0.eq("mail", mail);
//            User user1 = userMapper.selectOne(wrapper0);
//            if (user1 != null) {
//                return R.fail("邮箱已被使用！如有必要请进行申诉");
//            }
//
//
//            JSONArray papers = paperService.getPaperByAuthorId((String) json.get("authorId"));
//            if(papers==null){
//                return R.fail("错误的authorId？(根据authorId未找到任何paper)");
//            }  //
//
//            if(scholarMapper.selectByAuthorId((String) json.get("authorId"))!=null){
//                return R.fail("该authorId已被认领，如果是本人认领请勿再进行认领操作，如果是冒领请进行申诉");
//            }
//            if(user.getCodeDate()!=null){
//                long seconds = ChronoUnit.SECONDS.between(Instant.ofEpochMilli(user.getCodeDate().getTime()),
//                        Instant.ofEpochMilli(new Date().getTime()));  //计算时间
//                System.out.println(seconds);
//                if (seconds < 60) {
//                    return R.fail("一分钟内只能获取一次验证码");
//                }
//            }
//
//            String verCode = mailService.sendEmailVerCode(mail, generateVerCode());
//
//            QueryWrapper<User> wrapper = new QueryWrapper<>();
//            wrapper.eq("name", user.getName());
//            user.setCodeDate(new Date());  //设置验证码获得时间
//            user.setVercode(verCode);
//            userMapper.update(user, wrapper);
//            session.setAttribute("loginUser", user); //更新session
//            session.setAttribute("authorId", json.get("authorId"));
//            session.setAttribute("mail", mail);
//            System.out.println("用户" + user.getId() + "的验证码为" + user.getVercode());
//            return R.success("邮件发送成功");
//        } catch (Exception e) {
//            e.printStackTrace();
//            return R.fail("邮件发送失败");
//        }
//    }

}