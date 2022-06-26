package com.example.library.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.library.common.R;
import com.example.library.emenu.SessionKey;
import com.example.library.entity.Book;
import com.example.library.entity.Manager;
import com.example.library.entity.Record;
import com.example.library.entity.User;
import com.example.library.mapper.*;
import com.example.library.service.MailService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    @Resource
    private UserMapper userMapper;
    @Resource
    private BookMapper bookMapper;
    @Resource
    private ManagerMapper managerMapper;
    @Autowired
    private MailService mailService;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private CollectionMapper collectionMapper;

    @PostMapping("/login")
    @ApiOperation(notes = "使用用户密码", value = "登录")
    public R login(@RequestBody Manager user, HttpSession session) {
    //e10adc3949ba59abbe56e057f20f883e
        String name = user.getName();
        String password = user.getPassword();
        Map<String, Object> map = new HashMap<>();
        try {
            QueryWrapper<Manager> wrapper = new QueryWrapper<>();
            wrapper.eq("name", name);
            Manager user1 = managerMapper.selectOne(wrapper);

            if (user1 != null) {
                if (DigestUtils.md5DigestAsHex(user.getPassword().getBytes()).equals(user1.getPassword())) {
//                    user1.setLoginDate(new Date());
//                    userMapper.update(user1, wrapper);  //更新登录时间
                    session.setAttribute(SessionKey.MANANGER_SESSION_key.getCode(), user1);
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
            if (session.getAttribute("loginAdmin") == null) {
                return R.fail("未登录");
            }
            session.removeAttribute("loginAdmin");
            if (session.getAttribute("loginAdmin") == null) {
                System.out.println("用户信息已清除，已成功退出登录");
            }
            return R.success("登出成功");
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail("登出错误");
        }
    }

    @GetMapping("/info")
    public R admininfo(HttpSession session) {
        try {
            if (session.getAttribute("loginAdmin") == null) {
                return R.fail("此操作需要先登录");
            }
            Manager user = (Manager) session.getAttribute("loginAdmin");
            return R.success(user);
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }

    @PostMapping("/modify")
    public R editinfo(@RequestBody Manager user, HttpSession session) {

        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        user.setId(JSON.parseObject(JSONObject.toJSONString(session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode())),User.class).getId());
        if (user.getPassword() != null) {
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }
        if (user.getName()!=null){
            if (!managerMapper.selectById(user.getId()).getName().equals(user.getName())){
                QueryWrapper<Manager> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("name",user.getName());
                if (managerMapper.selectCount(queryWrapper) != 0){
                    return R.fail("用户名存在");
                }
            }
        }
        if (managerMapper.updateById(user) == 0) {
            return R.fail("用户不存在");
        }

        return R.success(user);
    }

    @GetMapping("/history")
    @ApiOperation("获取借阅记录")
    public R getHistory(HttpSession session,@RequestBody JSONObject param){
        try {

            if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
                return R.fail("未登录");
            }
            Manager user=(Manager) session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode());
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
            PageInfo<Record> pageInfo;
            if(param.containsKey("select") && param.containsKey("search")){
                String select=param.getString("select");
                String search=param.getString("search");
                List<Integer> intlist= new ArrayList<>();
                intlist.add(1);
                intlist.add(2);
                queryWrapper.in("is_return",intlist);
                if(select.equals("图书id")){
                    queryWrapper.eq("symbol_num",search);
                    pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));

                }else if(select.equals("图书名称")){
                    QueryWrapper<Book> BookqueryWrapper = new QueryWrapper<>();

                    BookqueryWrapper.eq("book_name",search);

                    List<Book> booklist=bookMapper.selectList(BookqueryWrapper);
                    List<String> list= new ArrayList<>();
                    for(Book book:booklist){
                        list.add(book.getSymbolNum());
                        System.out.println("-----"+book.getSymbolNum()+"-----");
                    }
                    queryWrapper.in("symbol_num",list);

                    pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));

                }else if(select.equals("借阅者id")){
                    queryWrapper.eq("id",search);
                    pageInfo = new PageInfo<>(recordMapper.selectList(queryWrapper));

                }else {
                    return R.fail("select参数错误");
                }
            }else {
                return R.fail("缺少参数select或search");
            }
    //        QueryWrapper<User> queryWrapper = new QueryWrapper<>();


            return R.success(pageInfo);
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }

    @GetMapping("/books")
    @ApiOperation("获取图书信息")
    public R getBooks(HttpSession session,@RequestBody JSONObject param){
        try {

            if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
                return R.fail("未登录");
            }
            Manager user=(Manager) session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode());
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

    @ApiOperation("增加书籍")
    @PostMapping("/add")
    public R addBook(HttpSession session,@RequestBody Book book){
        try{
            if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
                return R.fail("请使用管理员账号登录");
            }

            bookMapper.insert(book);
            return R.success(book);
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }

    @ApiOperation("删除书籍")
    @PostMapping("/deleteBook")
    public R deleteBook(HttpSession session,@RequestBody Book book){
        try{
            if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
                return R.fail("请使用管理员账号登录");
            }
            QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("symbol_num",book.getSymbolNum());
            bookMapper.delete(queryWrapper);
            return R.success();
        }catch (Exception e) {
            e.printStackTrace();
            return R.fail("未知错误");
        }
    }
}
