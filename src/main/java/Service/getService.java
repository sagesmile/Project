package Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.annotation.Order;
import utils.AESUtil;
import utils.HttpUtil;

import javax.xml.ws.Service;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

public class getService {

    private static String[] token = {"Bearer 8ef75c5f-401d-40bb-baa0-aadcd816f203","Bearer 07ea1649-df6f-4039-b5cb-89b5043f7db0"};
    private static String[] shoppingcartId = {"10ff72e3f2a246339ed193b5b5cc0447","287400666a1946bfa8a0c1d2cfbc8c1b"};
    private static String challenge[] = {
            "178c906e7533fb7e2a71a7b444d772f3",
            "59b6851c23b658f9db75d8af259d7228"
    };
    private static String validate[] = {
            "a2e88b746e2dd8dd8bdbe401256f11ec",
            "4a892b979b0ea3541adb6d02850890b0"
    };

    private static String host = "https://wxmall-lv.topsports.com.cn";
    private static String searchUrl = "/search/shopCommodity/list";
    private static String commodityUrl = "/shopCommodity/queryShopCommodityDetail/";
    private static String createOrder = "/order/create";
    private static int temp = 0;

    public static void main(String[] args) {
        getService service = new getService();
        int moshi = 1;
        boolean flag = true;
        String getdetail = null;
        switch (moshi){
            //有链接 有库存 定时上架模式
            case 1:
                getdetail = service.getdetail("8be887163edc48ae8367fc60a48e8d01");
                ArrayList<JSONObject> orderJson = service.initOrder(getdetail);
                while (flag){
                    getdetail = service.getdetail("8be887163edc48ae8367fc60a48e8d01");
                    Integer integer = JSONObject.parseObject(getdetail).getJSONObject("data").getInteger("status");
                    if (integer ==3){
                        for (int i = 0; i < orderJson.size(); i++) {
                            service.pushOrder(token[i],orderJson.get(i) );
                        }
                        System.out.println(getdetail);
                        flag = false;
                    }
                }
            //有链接 无库存 补货模式
            case 2:

                while (flag){
                    getdetail = service.getdetail("");
                    Integer integer = JSONObject.parseObject(getdetail).getJSONObject("data").getInteger("stock");
                    if (integer>0){
                        service.initAndPushOrder(getdetail);
                        flag = false;
                    }
                }
            //无链接 店铺上新
            case 3:
                while(flag){
                    try {
                        String id = service.search("CT0979-1071","NKCQ46");
                        if (id!=null){
                            getdetail = service.getdetail(id);
                            ArrayList<JSONObject> jsonObjects = service.initOrder(getdetail);
                            for (int i = 0; i < jsonObjects.toArray().length; i++) {
                                service.pushOrder(token[i],jsonObjects.get(i) );
                            }
                            System.out.println(getdetail);
                            flag = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            case 4:
        }


    }


    /**
     *
     * @param keyword 货号
     * @param shopNo 店铺号
     * @return
     * @throws Exception
     */
    private  String search(String keyword,String shopNo) throws Exception{
        String url = host+searchUrl;
        HashMap<String, String> map = new HashMap<>();
        map.put("searchKeyword",keyword);
        map.put("current","1");
        map.put("pageSize","5");
        map.put("sortColumn","");
        map.put("sortType","asc");
        map.put("filterIds","TS300301");
        map.put("shopNo",shopNo);//NKND20
        map.put("tssign",AESUtil.getTssign(searchUrl).substring(8));
        HttpURLConnection conn = HttpUtil.getConn(url, map);
        String result = HttpUtil.get(conn);
        JSONArray jsonArray = JSONObject.parseObject(result).getJSONObject("data").getJSONObject("spu").getJSONArray("list");
        if (!jsonArray.isEmpty()){
            String id = jsonArray.getJSONObject(0).getString("id");
            return id;
        }else{
            return null;
        }
    }


    private String getdetail(String id){
        String commodityUrl = getService.commodityUrl + id;
        String url = host+"/shopCommodity/queryShopCommodityDetail/"+id+AESUtil.getTssign(commodityUrl);
        String getResult = null;
        try {
            HttpURLConnection conn = HttpUtil.getConn(url, null);
            getResult = HttpUtil.get(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getResult;
    }

//    /**
//     * 从购物车中获取shopCartId
//     * @param token
//     * @return
//     */
//    private String getShopCartId(String token){
//        HttpURLConnection conn = null;
//        String getCartResult = null;
//        try {
//            conn = HttpUtil.getConn(getCartUrl, null);
//            getCartResult = HttpUtil.get(conn);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        //从购物车中获取一个shopCartId
//        String shopCartId = null;
//        JSONObject jsonObject = JSONObject.parseObject(getCartResult);
//        JSONArray willBuyList = jsonObject.getJSONObject("data").getJSONArray("willBuyList");
//        if (!willBuyList.isEmpty()){
//            shopCartId = willBuyList.getJSONObject(0).getJSONArray("buyCommodityVOList").getJSONObject(0).get("shoppingcartId").toString();
//        }else{
//            System.out.println("购物车中无商品！！！");
//            JSONArray failureList = jsonObject.getJSONObject("data").getJSONArray("failureList");
//            if (!failureList.isEmpty()){
//                shopCartId = failureList.getJSONObject(0).get("shoppingcartId").toString();
//            }
//            System.out.println("购物车中无商品！！！");
//        }
//        return shopCartId;
//    }



    private ArrayList<JSONObject> initOrder(String commodityDetail){
        //解析商品信息 productNo、尺码、skuId不需要
        ArrayList<JSONObject> list = new ArrayList<>();
        Integer cishu = 0;
        JSONObject commodyJson = JSONObject.parseObject(commodityDetail).getJSONObject("data");
        String id = commodyJson.getString("id");
        String productCode = commodyJson.getString("productCode");
        String shopNo = commodyJson.getString("shopNo");
        JSONArray skuList = commodyJson.getJSONArray("skuList");
        for (int i = 0; i < skuList.size(); i++) {
            JSONObject skuListJson = skuList.getJSONObject(i);
            Integer stock = skuListJson.getInteger("stock");
            if (stock >1){
                String sizeNo = skuListJson.getString("sizeNo");
                String sizeCode = skuListJson.getString("sizeCode");
                String skuNo = skuListJson.getString("skuNo");
                JSONObject orderJson = JSONObject.parseObject(order);
                JSONArray subOrderArray = orderJson.getJSONArray("subOrderList");
                JSONObject subOrderList = subOrderArray.getJSONObject(0);
                subOrderList.put("shopNo",shopNo);
                JSONArray commodityList = subOrderList.getJSONArray("commodityList");
                JSONObject commodity = commodityList.getJSONObject(0);
                commodity.put("productCode",productCode);
                commodity.put("shoppingcartId",shoppingcartId[cishu]);
                commodity.put("sizeNo",sizeNo);
                commodity.put("sizeCode",sizeCode);
                commodity.put("skuNo",skuNo);
                commodity.put("shopCommodityId",id);
                commodityList.set(0,commodity);
                subOrderList.put("commodityList",commodityList);
                subOrderArray.set(0,subOrderList);
                orderJson.put("subOrderList",subOrderArray);
                list.add(orderJson);
                cishu++;
            }
            if (cishu>=token.length){
                break;
            }
        }
        return list;
    }

    private void initAndPushOrder(String commodityDetail){
        //解析商品信息 productNo、尺码、skuId不需要
        ArrayList<JSONObject> list = new ArrayList<>();
        int cishu = 0;
        JSONObject commodyJson = JSONObject.parseObject(commodityDetail).getJSONObject("data");
        String id = commodyJson.getString("id");
        String productCode = commodyJson.getString("productCode");
        String shopNo = commodyJson.getString("shopNo");
        JSONArray skuList = commodyJson.getJSONArray("skuList");
        if (cishu<token.length){
            for (int i = 0; i < skuList.size(); i++) {
                JSONObject skuListJson = skuList.getJSONObject(i);
                Integer stock = skuListJson.getInteger("stock");
                if (stock >2){
                    String sizeNo = skuListJson.getString("sizeNo");
                    String sizeCode = skuListJson.getString("sizeCode");
                    String skuNo = skuListJson.getString("skuNo");
                    JSONObject orderJson = JSONObject.parseObject(order);
                    JSONArray subOrderArray = orderJson.getJSONArray("subOrderList");
                    JSONObject subOrderList = subOrderArray.getJSONObject(0);
                    subOrderList.put("shopNo",shopNo);
                    JSONArray commodityList = subOrderList.getJSONArray("commodityList");
                    JSONObject commodity = commodityList.getJSONObject(0);
                    commodity.put("productCode",productCode);
                    commodity.put("shoppingcartId",shoppingcartId[cishu]);
                    commodity.put("sizeNo",sizeNo);
                    commodity.put("sizeCode",sizeCode);
                    commodity.put("skuNo",skuNo);
                    commodity.put("shopCommodityId",id);
                    commodityList.set(0,commodity);
                    subOrderList.put("commodityList",commodityList);
                    subOrderArray.set(0,subOrderList);
                    orderJson.put("subOrderList",subOrderArray);
                    String token = getService.token[cishu];
                    System.out.println(token);
                    new Thread(()->{
                        pushOrder(token,orderJson);
                    });
                    cishu++;
                }
            }
        }
    }

//    private String getTssign(String url){
//
//    }

    private void pushOrder(String token,JSONObject jsonObject){
        jsonObject.put("validate",validate[temp]);
        jsonObject.put("seccode",validate[temp]+"|jordan");
        jsonObject.put("challenge",challenge[temp]);
        temp++;
        try {
            String url = host+createOrder+AESUtil.getTssign(createOrder);
            System.out.println(HttpUtil.send(url, jsonObject, token));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String order = "{\n" +
            "    \"merchantNo\":\"TS\",\n" +
            "    \"rid\":\"\",\n" +
            "    \"shippingId\":\"8a7a099774d3ca74017520de7a8a7a7d\",\n" +
            "    \"subOrderList\":[\n" +
            "        {\n" +
            "            \"shopNo\":\"ADCA60\",\n" +
            "            \"totalNum\":1,\n" +
            "            \"totalPrice\":null,\n" +
            "            \"virtualShopFlag\":0,\n" +
            "            \"expressType\":2,\n" +
            "            \"remark\":null,\n" +
            "            \"fullDiscountAmount\":null,\n" +
            "            \"fullReductionAmount\":null,\n" +
            "            \"couponAmount\":\"0.00\",\n" +
            "            \"commodityList\":[\n" +
            "                {\n" +
            "                    \"map\":{\n" +
            "\n" +
            "                    },\n" +
            "                    \"orderByClause\":null,\n" +
            "                    \"shoppingcartId\":\"c37c0bda5ad84e0db6732ff18f0ebac1\",\n" +
            "                    \"paterId\":\"\",\n" +
            "                    \"productCode\":\"EE6462\",\n" +
            "                    \"colorNo\":\"00\",\n" +
            "                    \"colorName\":\"默认\",\n" +
            "                    \"sizeNo\":\"20160426000035\",\n" +
            "                    \"sizeCode\":\"3.5\",\n" +
            "                    \"sizeEur\":\"36\",\n" +
            "                    \"brandDetailNo\":\"AO01\",\n" +
            "                    \"proNo\":\"HPT201211000486\",\n" +
            "                    \"assignProNo\":\"HPT201211000486\",\n" +
            "                    \"skuNo\":\"20200911007010\",\n" +
            "                    \"shopCommodityId\":\"2c8e3803dc9146259ba81bdcfaf854b7\",\n" +
            "                    \"salePrice\":1099,\n" +
            "                    \"tagPrice\":1099,\n" +
            "                    \"num\":1,\n" +
            "                    \"status\":3,\n" +
            "                    \"itemFlag\":0,\n" +
            "                    \"usedTicket\":null,\n" +
            "                    \"activityType\":1,\n" +
            "                    \"activityTypeStr\":\"满折\",\n" +
            "                    \"usedTickets\":null,\n" +
            "                    \"liveType\":0,\n" +
            "                    \"roomId\":null,\n" +
            "                    \"roomName\":\"\",\n" +
            "                    \"zoneQsLevel\":null,\n" +
            "                    \"live_type\":0,\n" +
            "                    \"room_id\":\"\",\n" +
            "                    \"room_name\":\"\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ticketCodes\":null,\n" +
            "            \"vipPrefAmount\":\"0.00\",\n" +
            "            \"prefAmount\":\"54.95\",\n" +
            "            \"ticketDtos\":[\n" +
            "\n" +
            "            ],\n" +
            "            \"unTicketDtos\":[\n" +
            "\n" +
            "            ],\n" +
            "            \"orderTickets\":null,\n" +
            "            \"ticketPresentDtos\":null,\n" +
            "            \"expressAmount\":\"0.00\",\n" +
            "            \"expressAmountStr\":\"包邮\",\n" +
            "            \"cashOnDelivery\":0,\n" +
            "            \"salePriceAmount\":\"1044.05\",\n" +
            "            \"promotionAmount\":\"54.95\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"purchaseType\":2,\n" +
            "    \"usedPlatformCouponList\":[\n" +
            "\n" +
            "    ],\n" +
            "    \"verificationType\":2,\n" +
            "    \"validate\":\"47dc6234d70edb4de9c895a561b89a5f\",\n" +
            "    \"seccode\":\"47dc6234d70edb4de9c895a561b89a5f|jordan\",\n" +
            "    \"challenge\":\"4ec37e90bb5113d7fca91acb62213687\"\n" +
            "}";




}
