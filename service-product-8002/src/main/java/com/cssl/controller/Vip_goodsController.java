package com.cssl.controller;


import com.cssl.entity.PageInfo;
import com.cssl.entity.VipGoods;
import com.cssl.service.Vip_goodsService;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author lx
 * @since 2019-09-10
 */
@Controller
@RequestMapping("/vip_goods")
public class Vip_goodsController {

    @Autowired
    private Vip_goodsService vip_goodsService;

    //会员商品
    @RequestMapping("vipGoodsFindAll")
    @ResponseBody
    public PageInfo<Map<String, Object>> vipGoodsFindAll(@RequestParam Map<String,Object> param, @RequestParam("page")int page, @RequestParam("limit")int limit){
        PageInfo<Map<String, Object>>pages=new PageInfo<>();
        Page<Map<String, Object>> maps = vip_goodsService.vipGoodsFindAll(param,page,limit);
        List<Map<String, Object>> result = maps.getResult();
        //封装查询数据
        pages.setList(result);
        //封装总记录数
        pages.setTotalCount((int)maps.getTotal());
        return pages;
    }
}
