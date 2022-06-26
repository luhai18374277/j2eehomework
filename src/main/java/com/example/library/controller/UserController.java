package com.example.library.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.R;
import com.example.library.emenu.SessionKey;
import com.example.library.entity.*;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.CollectionMapper;
import com.example.library.mapper.RecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.service.MailService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
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
@Component
//@Api(description = "普通用户相关接口")
public class UserController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private BookMapper bookMapper;
    @Autowired
    private MailService mailService;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private CollectionMapper collectionMapper;
    @PostMapping("/hello")
    public R hello(HttpSession session) {
        try{
            User user=new User();
            user.setName("root");
            user.setPassword("123456");
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("name", user.getName());
            User user1 = userMapper.selectOne(wrapper);

            if (user1 != null) {
                if (DigestUtils.md5DigestAsHex(user.getPassword().getBytes()).equals(user1.getPassword())) {
//                    user1.setLoginDate(new Date());
//                    userMapper.update(user1, wrapper);  //更新登录时间
                    session.setAttribute(SessionKey.USER_SESSION_key.getCode(), user1);
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
    @GetMapping("/info")
    public R userinfo(HttpSession session) {
        try {
            if (session.getAttribute("loginUser") == null) {
                return R.fail("此操作需要先登录");
            }
            User user = (User) session.getAttribute("loginUser");
            return R.success(user);
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }

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
                if (DigestUtils.md5DigestAsHex(user.getPassword().getBytes()).equals(user1.getPassword())) {
//                    user1.setLoginDate(new Date());
//                    userMapper.update(user1, wrapper);  //更新登录时间
                    session.setAttribute(SessionKey.USER_SESSION_key.getCode(), user1);
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

    @PostMapping("/modify")
    public R editinfo(@RequestBody User user, HttpSession session) {

        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        user.setId(JSON.parseObject(JSONObject.toJSONString(session.getAttribute(SessionKey.USER_SESSION_key.getCode())),User.class).getId());
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

        return R.success(user);
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


    /**
     * 获取用户列表功能
     * @param session
     * @param param(pageNo:1,pageSize:5)
     * @return
     */
    @PostMapping("getUserPage")
    @ApiOperation("获取用户列表")
    public R getUserPage(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
            return R.fail("请使用管理员账号登录");
        }
        //页码，长度
        int pageNo = 1,pageSize = 4;
        if (param.containsKey("current")){
            pageNo = param.getInteger("current");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        PageInfo<User> pageInfo = new PageInfo<>(userMapper.selectList(queryWrapper));
         return R.success(pageInfo);
    }

//
    @GetMapping("/books")
    @ApiOperation("获取当前个人借书信息")
    public R getUserBooks(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("未登录");
        }
        User user=(User) session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        //页码，长度
        int pageNo = 1,pageSize = 4;
        if (param.containsKey("current")){
            pageNo = param.getInteger("current");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        queryWrapper.eq("is_return",1);

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        PageInfo<Record> pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));
        return R.success(pageInfo);
    }

    @GetMapping("/history")
    @ApiOperation("获取个人借书历史")
    public R getUserhistory(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("未登录");
        }
        User user=(User) session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        //页码，长度
        int pageNo = 1,pageSize = 4;
        if (param.containsKey("current")){
            pageNo = param.getInteger("current");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        System.out.println(user.getId());
        queryWrapper.eq("is_return",2);

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        PageInfo<Record> pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));
        return R.success(pageInfo);
    }

    @GetMapping("/reserve")
    @ApiOperation("获取个人预约信息")
    public R getUserreserve(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("未登录");
        }
        User user=(User) session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        //页码，长度
        int pageNo = 1,pageSize = 4;
        if (param.containsKey("current")){
            pageNo = param.getInteger("current");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        queryWrapper.eq("is_return",3);

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        PageInfo<Record> pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));
        return R.success(pageInfo);
    }


    @GetMapping("/getSubscribe")
    @ApiOperation("获取个人收藏信息")
    public R getUserSubscribe(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("未登录");
        }
        User user=(User) session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        //页码，长度
        int pageNo = 1,pageSize = 4;
        if (param.containsKey("current")){
            pageNo = param.getInteger("current");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",user.getId());

//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        PageInfo<Collection> pageInfo = new PageInfo<>(collectionMapper.selectList(queryWrapper));
        return R.success(pageInfo);
    }

    @GetMapping("/searchbooks")
    @ApiOperation("获取图书信息")
    public R getBooks(HttpSession session,@RequestBody JSONObject param){
        try {

            if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
                return R.fail("未登录");
            }
            User user=(User) session.getAttribute(SessionKey.USER_SESSION_key.getCode());
            //页码，长度
            int pageNo = 1,pageSize = 4;
            if (param.containsKey("current")){
                pageNo = param.getInteger("current");
            }
            if (param.containsKey("pageSize")){
                pageSize = param.getInteger("pageSize");
            }
            PageHelper.startPage(pageNo,pageSize);
            QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
            if(param.containsKey("select") && param.containsKey("search")){
                String select=param.getString("select");
                String search=param.getString("search");
                if(select.equals("图书id")){
                    queryWrapper.eq("symbol_num",search);
                }else if(select.equals("图书名称")){
                    queryWrapper.eq("book_name",search);
                }else if(select.equals("作者")){
                    queryWrapper.eq("author",search);
                }else if(select.equals("出版社")){
                    queryWrapper.eq("publisher",search);
                }else if(select.equals("类型")){
                    queryWrapper.eq("tag",search);
                }else {
                    return R.fail("select参数错误");
                }
            }else {
                return R.fail("缺少参数select或search");
            }
            //        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            PageInfo<Book> pageInfo = new PageInfo<>(bookMapper.selectList(queryWrapper));

            return R.success(pageInfo);
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }
}
