package com.cssl.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cssl.entity.*;
import com.cssl.mapper.Order_detailMapper;
import com.cssl.service.EvaluateService;
import com.cssl.service.GoodsService;
import com.cssl.service.Order_detailService;
import com.cssl.service.OrdersService;
import com.github.pagehelper.Page;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.rmi.server.UID;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author lx
 * @since 2019-09-10
 */
@Controller
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private Order_detailService orderDetailService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private EvaluateService evaluateService;

    //-----------------------------前台模块----------------------------
    //用户下单信息
    @RequestMapping("orderInfo")
    @ResponseBody
    public Map<String, Object> orderInfo(@RequestParam("uid") Integer uid, @RequestParam Map<String, Object> map) {
        System.out.println("uid**************** = " + uid);
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> goodsList = new ArrayList<>();
        String[] gid = map.get("goodsId").toString().split(",");
        System.out.println("gid = " + gid);
        String[] num = map.get("num").toString().split(",");
        for (int i = 0; i < gid.length; i++) {
            Goods goods = goodsService.getOne(new QueryWrapper<Goods>().eq("goods_id",gid[i]));
            Map<String,Object> param = new HashMap<>();
            param.put("gid",goods.getId());
            param.put("title",goods.getTitle());
            param.put("img",goods.getMainImg());
            double price = goods.getPrice().doubleValue() * Double.valueOf(num[i]);
            param.put("price",price);
            param.put("num",num[i]);
            goodsList.add(param);
        }
        data.put("goods",goodsList);
        return data;
    }


    //用户订单
    @RequestMapping("findOrdersByUserId")
    @ResponseBody
    public PageInfo<Map<String, Object>> findOrdersByUserId(@RequestParam Map<String, Object> map) {
        PageInfo<Map<String, Object>> pages = new PageInfo<>();
        Page<Map<String, Object>> page = ordersService.findOrdersByUserId(map);
        pages.setList(page.getResult());
        pages.setPageNo(page.getPageNum());
        pages.setTotalCount((int) page.getTotal());
        pages.setPageSize(page.getPageSize());
        pages.setPageCount(page.getPages());
        return pages;
    }


    //根据订单编号查询订单明细
    @RequestMapping("findOrdersDetail")
    @ResponseBody
    public List<Map<String, Object>> findOrdersDetail(@RequestParam("oid") Integer oid) {
        return ordersService.findOrdersDetail(oid);
    }

    //根据订单编号查询订单信息
    @RequestMapping("findOrders")
    @ResponseBody
    public Orders findOrders(@RequestParam("oid") Integer oid) {
        return ordersService.getOne(new QueryWrapper<Orders>().eq("order_id", oid));
    }

    //查询用户订单的待付款待收货总记录数
    @RequestMapping("findTotal")
    @ResponseBody
    public List<Map<String, Object>> findTotal(@RequestParam("uid") Integer uid) {
        return ordersService.findTotal(uid);
    }

    //根据订单号查询地址表的相关信息
    @RequestMapping("findAddressByOrder")
    @ResponseBody
    public Map<String, Object> findAddressByOrder(@RequestParam("oid") Integer oid) {
        return ordersService.findAddressByOrder(oid);
    }

    //-----------------------------后台模块----------------------------
    //查询所有订单详情
    @RequestMapping("/orderList/{pageIndex}/{pageSize}/{orderNo}/{name}")
    @ResponseBody
    public PageInfo<Map<String, Object>> orderList(@PathVariable("pageIndex") Integer pageIndex, @PathVariable("pageSize") Integer pageSize, @PathVariable("orderNo") String orderNo, @PathVariable("name") String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);
        map.put("name", name);
        Page<Map<String, Object>> maps = ordersService.orderList(map, pageIndex, pageSize);
        PageInfo<Map<String, Object>> page = new PageInfo<>();
        List<Map<String, Object>> result = maps.getResult();
        //封装查询数据
        page.setList(result);
        //封装总记录数
        page.setTotalCount((int) maps.getTotal());
        return page;
    }

    //修改回显
    @RequestMapping("/ByIdOrders/{orderNo}")
    @ResponseBody
    public List<Map<String, Object>> ByIdOrders(@PathVariable("orderNo") String orderNo) {
        return ordersService.ByIdOrders(orderNo);
    }

    //修改订单状态
    @RequestMapping("/updateStatus")
    @ResponseBody
    public int updateStatus(@RequestParam("orderNo") String orderNo, @RequestParam("status") int status) {
        Orders orders=new Orders();
        orders.setStatus(status);
        orders.setOrderNo(orderNo);
        //判断修改状态试是否是5已完成(如果是将引入评价表状态改为0为未评价)
        if(Integer.valueOf(status)==5){  //查询订单中得商品
            List<Map<String, Object>> maps = ordersService.byGoodId(orderNo);
            //循环写入评价表
            for(Map<String, Object> list:maps){
                Evaluate evaluate=new Evaluate();
                evaluate.setGoodsId(Integer.valueOf(list.get("goods_id").toString()));
                evaluate.setUserId(Integer.valueOf(list.get("user_id").toString()));
                evaluate.setOid(Integer.valueOf(list.get("order_id").toString()));
                evaluate.setTime(new Date());
                evaluate.setState(0);   //未评价
                evaluateService.save(evaluate);
            }
        }
        return  ordersService.updateStatus(orders);

    }

    //删除订单
    @RequestMapping("/deleteOrders")
    @ResponseBody
    public int deleteOrders(@RequestParam("orderId") Integer orderId,@RequestParam("status")Integer status) {
        //先删除订单详情表
        int i = orderDetailService.deletOrderDetail(orderId);
        //删除订单时判断是否时已完成状态如果是删除评价表相关信息(因为评价表信息是通过订单状态添加进去)
         if(status==5){
          //删除评价表信息
        Map<String,Object>map=new HashMap<String,Object>();
        map.put("order_id",orderId);
        evaluateService.removeByMap(map);
        }
        int num = 0;
        if (i > 0) {
            num = ordersService.deleteOrder(orderId);
        }
        return num;
    }

    //计算本月退单数量,订单数量和金额,本月订单实际金额,本月未付款金额
    @RequestMapping("/inquiryAmount")
    @ResponseBody
    public Map<String, Object> inquiryAmount() {
        return ordersService.orderQuantity();
    }

    //查询最近一周的订单金额,未付金额,退款金额,实际金额
    @RequestMapping("/weekInquiryAmount")
    @ResponseBody
    public Map<String, Object> weekInquiryAmount() {
        return ordersService.weekOrderQuantity();
    }

    //查询最近一周对应日期的订单金额,未付金额,退款金额,实际金额
    @RequestMapping("/weekOrder")
    @ResponseBody
    public List<Map<String, Object>> weekOrder() {
        return ordersService.weekOrder();
    }


    //查看订单详情购买商品信息
    @RequestMapping("/orderDetail")
    @ResponseBody
    public PageInfo<Map<String, Object>> orderDetail(@RequestParam("order_no") String order_no, @RequestParam("page") int page, @RequestParam("limit") int limit) {
        PageInfo<Map<String, Object>> pages = new PageInfo<>();
        Page<Map<String, Object>> maps = ordersService.orderDetail(order_no, page, limit);
        List<Map<String, Object>> result = maps.getResult();
        //封装查询数据
        pages.setList(result);
        //封装总记录数
        pages.setTotalCount((int) maps.getTotal());
        return pages;
    }


}
