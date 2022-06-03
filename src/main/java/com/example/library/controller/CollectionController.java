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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RequestMapping("/collection")
@RestController
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
        queryWrapper.eq("bid",id);
        if (collectionMapper.selectCount(queryWrapper) != 0){
            return R.fail("您已经收藏过了");
        }
        Collection collection = new Collection();
        collection.setUid(user.getId());
        collection.setBid(id);
        return collectionMapper.insert(collection) == 0? R.fail("收藏失败") : R.success("收藏成功");
    }

}
