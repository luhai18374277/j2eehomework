package com.example.library.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.library.common.R;
import com.example.library.emenu.SessionKey;
import com.example.library.entity.Book;
import com.example.library.entity.Record;
import com.example.library.entity.User;
import com.example.library.mapper.BookMapper;
import com.example.library.mapper.RecordMapper;
import com.example.library.mapper.UserMapper;
import com.example.library.util.BookPrice;
import com.example.library.util.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequestMapping("/book")
@RestController
public class BookController {
    @Resource
    private BookMapper bookMapper;
    @Resource
    private RecordMapper recordMapper;
    @Resource
    private UserMapper userMapper;
    /**
     * 借书功能主要实现于由用户已经在网站完成预约，完成到店拿书功能
     * @param jsonObject 传递参数为 （symbolNum:书号,id:预约用户的id,password:用户密码）
     * @return
     */
    @PostMapping("Borrowbooks")
    @ApiOperation("借书功能")
    public R Borrowbooks(@RequestBody JSONObject jsonObject, HttpSession session){
        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null ){
            return R.fail("请使用管理员账号登录");
        }
        if (!jsonObject.containsKey("symbolNum")){
            return R.fail("请输入书号");
        }
          if (!jsonObject.containsKey("id")){
              return R.fail("用户编号");

          }
          if (!jsonObject.containsKey("password")){
              return R.fail("请输入密码");
          }
          if (jsonObject.getString("password").equals("")){
              return R.fail("请输入密码");

          }

        /**
         * 验证用户信息
         */
        User user = userMapper.selectById(jsonObject.getInteger("id"));
        if (user == null){
            return R.fail("用户不存在");
        }else if(!DigestUtils.md5DigestAsHex(jsonObject.getString("password").getBytes()).equals(user.getPassword())) {
            return R.fail("密码错误");
        }
        /**
         * 验证用户是否已经预约该书
         */
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",jsonObject.getInteger("id"));
        queryWrapper.eq("symbol_num",jsonObject.getString("symbolNum"));
        Record record = recordMapper.selectOne(queryWrapper);
        if (record == null){
            return R.fail("未查询到预约信息");
        }else if (record.getIsReturn() == 1){
            return R.fail("您已经借出该书");
        }else  if (record.getIsReturn() == 2){
            return R.fail("您已归还该书");
        }else  if (record.getIsReturn() == 4){
            return R.fail("您已取消预约该书");
        }
        record.setBorrowTime(new Date());
        record.setIsReturn(1);
        //还书时间为一个月后
//        long returnZone = new Date().getTime() + (262656 * 1000);
//        Date returnTime = new Date(returnZone);

        record.setReturnTime(DateUtil.getMonthDate(new Date(),1));
        return recordMapper.updateById(record) == 0?R.fail("借书失败"):R.success("借书成功");
    }
    /**
     * 借书功能主要实现于由用户已经在网站完成预约，完成到店拿书功能
     * @param param 传递参数为 （symbolNum:书号,id:预约用户的id,password:用户密码）
     * @return
     */
    @ApiOperation("登记借阅信息")
    @PostMapping("registerBorrowbooks")
    public R registerBorrowbooks(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null ){
            return R.fail("请使用管理员账号登录");
        }
        if (!param.containsKey("symbolNum")){
            return R.fail("请输入书号");
        }
        if (!param.containsKey("id")){
            return R.fail("用户编号");

        }
        if (!param.containsKey("password")){
            return R.fail("请输入密码");
        }
        if (param.getString("password").equals("")){
            return R.fail("请输入密码");
        }
        /**
         * 验证用户信息
         */
        User user = userMapper.selectById(param.getInteger("id"));
        if (user == null){
            return R.fail("用户不存在");
        }else if(! DigestUtils.md5DigestAsHex(param.getString("password").getBytes()).equals(user.getPassword())) {
            return R.fail("密码错误");
        }
        Book book = bookMapper.selectById(param.getString("symbolNum"));
        if (book.getRemainingQuantity() == 0){
            return R.fail("存货不足");
        }

        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        queryWrapper.eq("symbol_num",param.getString("symbolNum"));
        List<Integer> list= new ArrayList<>();
        list.add(1);
        list.add(3);
        queryWrapper.in("is_return",list);

        if (recordMapper.selectCount(queryWrapper) != 0){
            return R.fail("您已经预约过或还有同类书籍未归还");
        }

        //减少剩余货量
        book.setRemainingQuantity(book.getRemainingQuantity() - 1);
        bookMapper.updateById(book);
        Record record = new Record();

        record.setId(param.getInteger("id"));
        record.setSymbolNum(param.getString("symbolNum"));
        record.setBorrowTime(new Date());
        record.setIsReturn(1);
        //还书时间为一个月后
//        long returnZone = new Date().getTime() + (262656 * 1000);
//        Date returnTime = new Date(returnZone);
        record.setReturnTime(DateUtil.getMonthDate(new Date(),1));
        return recordMapper.insert(record) == 0?R.fail("借书失败"):R.success("借书成功");
    }
    /**
     * 获取用户列表功能
     * @param session
     * @param param(pageNo:1,pageSize:5)
     * @return
     */
    @ApiOperation("获取书籍信息")
    @PostMapping("getBooksPage")
    public R getBooksPage(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
            return R.fail("请使用管理员账号登录");
        }
        //页码，长度
        int pageNo = 1,pageSize = 5;
        if (param.containsKey("pageNo")){
            pageNo = param.getInteger("pageNo");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        PageInfo<Book> pageInfo = new PageInfo<>(bookMapper.selectList(queryWrapper));
        return R.success(pageInfo);
    }

    /**
     *  传递参数为 （symbolNum:书号,id:预约用户的id）
     * @param session
     * @param param
     * @return
     */
    @PostMapping("returnBook")
    @ApiOperation("工作人员还书")
    public R returnBook(HttpSession session,@RequestBody JSONObject param){
        if (session.getAttribute(SessionKey.MANANGER_SESSION_key.getCode()) == null){
            return R.fail("请使用管理员账号登录");
        }
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("symbol_num",param.getString("symbolNum"));
        queryWrapper.eq("id",param.getInteger("id"));
        queryWrapper.eq("is_return",1);
        Record record = recordMapper.selectOne(queryWrapper);
        if (record == null){
            return R.fail("未查询到借书记录");
        }
        //获取当前时间
        long currentTime = new Date().getTime();
        long returnTime = record.getReturnTime().getTime();
        //获取书本价格,当前是按照该价格为30天借用价格
        Double price = bookMapper.selectById(record.getSymbolNum()).getPrice();
        if (currentTime > returnTime){
            //当前时间大于归还时间，需要计算罚息
            price = BookPrice.getPrice(price,currentTime - returnTime);
        }
        User user = userMapper.selectById(record.getId());
        if (user.getDeposit() < price){
            return R.fail("余额不足，缺少金额为"+( price - user.getDeposit() )+"元");
        }
        user.setDeposit(user.getDeposit() - price);
        userMapper.updateById(user);
        record.setIsReturn(2);
        recordMapper.updateById(record);
        Book book = bookMapper.selectById(param.getString("symbolNum"));
        book.setRemainingQuantity(book.getRemainingQuantity() + 1);
        bookMapper.updateById(book);
        return R.fail("还书成功");
    }

    @ApiOperation("预约图书")
    @PostMapping("makeAppointmentBook/{id}")
    public R makeAppointmentBook(HttpSession session , @PathVariable("id") String id){
        Object attribute = session.getAttribute(SessionKey.USER_SESSION_key.getCode());

        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        Book book = bookMapper.selectById(id);
        if (book.getRemainingQuantity() == 0){
            return R.fail("剩余数量不足");
        }
        book.setRemainingQuantity(book.getRemainingQuantity()   -1 );
        bookMapper.updateById(book);
        User user = JSON.parseObject(JSONObject.toJSONString(attribute),User.class);
        //验证用户是否已经预约过
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",user.getId());
        queryWrapper.eq("symbol_num",id);
        List<Integer> list= new ArrayList<>();
        list.add(1);
        list.add(3);
        queryWrapper.in("is_return",list);
        if (recordMapper.selectCount(queryWrapper) != 0){
            return R.fail("您已经预约过该图书");
        }
        //进行预约
        Record record = new Record();
        record.setIsReturn(3);
        record.setSymbolNum(id);
        record.setId(user.getId());
        record.setBorrowTime(new Date());
        return recordMapper.insert(record)==0?R.fail("预约失败"):R.success("预约成功");
    }


    @ApiOperation("续借图书")
    @PostMapping("continueBook/{id}")
    public R continueBook(HttpSession session,@PathVariable("id")String id){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        Object attribute = session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        Integer userId = JSON.parseObject(JSONObject.toJSONString(attribute),User.class).getId();
        User user = userMapper.selectById(userId);
        if (user.getRenew() == 0){
            return R.fail("续借次数不足");
        }

        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",userId);
        queryWrapper.eq("symbol_num",id);
        queryWrapper.eq("is_return",1);
        Record record = recordMapper.selectOne(queryWrapper);
        if (record == null){
            return R.fail("未查询到借书记录");
        }
        user.setRenew(user.getRenew() - 1);
        userMapper.updateById(user);
//        long returnZone = record.getReturnTime().getTime() + (262656 * 1000);
        Date returnTime =  DateUtil.getMonthDate(record.getReturnTime(),1);
        record.setReturnTime(returnTime);
        return recordMapper.updateById(record) ==0 ? R.fail("续借失败"): R.success("续借成功");
    }
    /**
     *
     * @param param（入参 book_name：书名，symbol_num：书号,tag:书类,author:作者,hotRecommend:热门推荐（借阅记录,传任何值都可以，传就代表按照借阅记录排序，传空代表不按照借阅记录排序））
     * @return
     */
    @PostMapping("searchBook")
    @ApiOperation("搜索书")
    public R searchBook(@RequestBody JSONObject param){
        //页码，长度
        int pageNo = 1,pageSize = 5;
        if (param.containsKey("pageNo")){
            pageNo = param.getInteger("pageNo");
        }
        if (param.containsKey("pageSize")){
            pageSize = param.getInteger("pageSize");
        }
        PageHelper.startPage(pageNo,pageSize);
        PageInfo<JSONObject> pageInfo = new PageInfo<>(bookMapper.searchList(param));
        return R.success(pageInfo);
    }

    @PostMapping("cancelReservation/{id}")
    @ApiOperation("取消预约")
    public R cancelReservation(HttpSession session,@PathVariable("id") String id){
        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        Object attribute = session.getAttribute(SessionKey.USER_SESSION_key.getCode());
        Integer userId = JSON.parseObject(JSONObject.toJSONString(attribute),User.class).getId();
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id",userId);
        queryWrapper.eq("symbol_num",id);
        queryWrapper.eq("is_return",3);
        Record record = recordMapper.selectOne(queryWrapper);
        if ( record== null){
            return R.fail("未查询到预约记录");
        }
        record.setIsReturn(4);
        return recordMapper.updateById(record) ==0? R.fail("取消预约失败") : R.success("取消成功");
    }


}
