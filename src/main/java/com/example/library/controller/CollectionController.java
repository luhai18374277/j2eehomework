package com.example.library.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.library.common.R;
import com.example.library.emenu.SessionKey;
import com.example.library.entity.Collection;
import com.example.library.entity.User;
import com.example.library.mapper.CollectionMapper;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RequestMapping("/collection")
@RestController
@CrossOrigin(origins = "*")
public class CollectionController {
    @Resource
    private CollectionMapper collectionMapper;
    @PostMapping("addCollection/{id}")
    @ApiOperation("增加收藏信息")
    public R addCollection(HttpSession session, @PathVariable("id")String id){
        Object attribute = session.getAttribute(SessionKey.USER_SESSION_key.getCode());

        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        User user = JSON.parseObject(JSONObject.toJSONString(attribute),User.class);

        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",user.getId());
        queryWrapper.eq("symbol_num",id);
        if (collectionMapper.selectCount(queryWrapper) != 0){
            return R.fail("您已经收藏过了");
        }
        Collection collection = new Collection();
        collection.setUid(user.getId());
        collection.setSymbolNum(id);
        return collectionMapper.insert(collection) == 0? R.fail("收藏失败") : R.success("收藏成功");
    }

    @PostMapping("delCollection/{id}")
    public R delCollection(HttpSession session, @PathVariable("id")String id){
        Object attribute = session.getAttribute(SessionKey.USER_SESSION_key.getCode());

        if (session.getAttribute(SessionKey.USER_SESSION_key.getCode()) == null){
            return R.fail("请先登录");
        }
        User user = JSON.parseObject(JSONObject.toJSONString(attribute),User.class);

        QueryWrapper<Collection> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("uid",user.getId());
        queryWrapper.eq("symbol_num",id);
        Collection collection = collectionMapper.selectOne(queryWrapper);
        if (collectionMapper.selectCount(queryWrapper) == 0){
            return R.fail("未查询到收藏记录");
        }
        return collectionMapper.deleteById(collection.getId()) == 0? R.fail("取消收藏失败") : R.success("取消收藏成功");
    }

}
