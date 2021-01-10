package Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.annotation.Order;
import utils.AESUtil;
import utils.HttpUtil;

import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

public class getService {

    private static String[] token = {"Bearer afa789b8-9ed7-4ee7-9507-7c7242e9a58a"};
    private static String[] shoppingcartId = {"b899a3aa33174ff7878cf72bca4d0b8f"};
    private static String challenge[] = {
            "c28f07807485534fd66ae23a45ccce1f",
    };
    private static String validate[] = {
            "42a55e22767d31f5321f3ca06f777c38",
    };

    private static String host = "https://wxmall-lv.topsports.com.cn";
    private static String searchUrl = "/search/shopCommodity/list";
    private static String commodityUrl = "/shopCommodity/queryShopCommodityDetail/";
    private static String createOrder = "/order/create";
    private static int temp = 0;

    public static void main(String[] args) {
        getService service = new getService();
        String getdetail = service.getdetail("5a63d3b9c11a47968c13fa7e65452dee");
        ArrayList<JSONObject> jsonObjects = service.initOrder(getdetail);
        for (int i = 0; i < jsonObjects.toArray().length; i++) {
//            System.out.println(jsonObjects.get(i));
            service.pushOrder(token[i],jsonObjects.get(i) );
        }
    }

    private String search(){
        String commodyId = null;
        try {
            HttpURLConnection conn = HttpUtil.getConn(searchUrl, null);
            String searchResult = HttpUtil.get(conn);
            System.out.println(searchResult);
            JSONObject jsonObject = JSONObject.parseObject(searchResult);
            commodyId =  jsonObject.getJSONObject("data").getJSONObject("spu").getJSONArray("list").getJSONObject(0).getString("id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commodyId;
    }


    private String getdetail(String id){
        String commodityUrl = getService.commodityUrl + id;
        String url = host+"/shopCommodity/queryShopCommodityDetail/"+id+AESUtil.getTssign(commodityUrl);
        System.out.println(url);
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
        JSONObject commodyJson = JSONObject.parseObject(commodityDetail).getJSONObject("data");
        String id = commodyJson.getString("id");
        String productCode = commodyJson.getString("productCode");
        String shopNo = commodyJson.getString("shopNo");
        JSONArray skuList = commodyJson.getJSONArray("skuList");
        for (int i = 0; i < skuList.size(); i++) {
            JSONObject skuListJson = skuList.getJSONObject(i);
            Integer stock = skuListJson.getInteger("stock");
            if (stock >temp*2){
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
                commodity.put("shoppingcartId",shoppingcartId[temp++]);
                commodity.put("sizeNo",sizeNo);
                commodity.put("sizeCode",sizeCode);
                commodity.put("skuNo",skuNo);
                commodity.put("shopCommodityId",id);
                commodityList.set(0,commodity);
                subOrderList.put("commodityList",commodityList);
                subOrderArray.set(0,subOrderList);
                orderJson.put("subOrderList",subOrderArray);
                list.add(orderJson);
            }
            if (temp==shoppingcartId.length){
                temp=0;
                break;
            }
        }
        return list;
    }

//    private String getTssign(String url){
//
//    }

    private void pushOrder(String token,JSONObject jsonObject){
        jsonObject.put("validate",validate[temp]);
        jsonObject.put("seccode",validate[temp]+"|jordan");
        jsonObject.put("challenge",challenge[temp]);
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
