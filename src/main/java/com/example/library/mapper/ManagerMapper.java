package com.example.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.library.entity.Manager;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper  //表示这是一个mybatis的mapper类，也就是dao
@Component
public interface ManagerMapper extends BaseMapper<Manager> {
}
