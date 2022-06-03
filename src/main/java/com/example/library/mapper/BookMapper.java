package com.example.library.mapper;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper  //表示这是一个mybatis的mapper类，也就是dao
@Component
public interface BookMapper extends BaseMapper<Book> {

    //搜索图书
    List<JSONObject> searchList(JSONObject jsonObject);
}
