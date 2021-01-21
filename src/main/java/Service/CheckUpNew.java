package Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import utils.AESUtil;
import utils.HttpUtil;

import javax.xml.transform.Result;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CheckUpNew {

    private static String[] token = {
            "Bearer ff349c1e-e2da-43ad-afbe-bc4ad45c62e1",
            "Bearer 9576dac7-21a6-4f5f-a7ab-f93636ab57d8"
    };

    private static String[] shoppingcartId = {
            "023cb9441d4240218735249bc6ea5f33",
            "24b2787f15a04bbd97d3e91b2f91d230"
    };

    private static String itemNo[] = {"DD1162-001","CT0979-602","CV8480-300"};

    private static String host = "https://wxmall-lv.topsports.com.cn";
    private static String searchUrl = "/search/shopCommodity/list";
    private static String commodityUrl = "/shopCommodity/queryShopCommodityDetail/";
    private static String createOrder = "/order/create";
    private static String initOrder="/order/confirmationOrder";
    private static int temp = 0;


    public static void main(String[] args) {
        CheckUpNew checkUpNew = new CheckUpNew();
        boolean isNotCode = true;
        String key = checkUpNew.damaLogin();
        long start = System.currentTimeMillis();
        long now = 0;
        List<String> challengeList = new ArrayList<>();
        List<String> validateList = new ArrayList<>();
        while (true){
            now = System.currentTimeMillis();
            if (isNotCode || now-start>540000){
                challengeList.clear();
                validateList.clear();
                System.out.println("-------开始打码！！！-------");
                for (int i = 0; i < 10; i++) {
                    String getchallenge = checkUpNew.getChallenge(token[0]);
                    if (getchallenge == null){
                        break;
                    }
                    String codeText = checkUpNew.damaStart(key, getchallenge);
                    System.out.println("-------第"+i+"次打码，打码结果："+codeText+"--------");
                    if (codeText.length() ==65){
                        String chanllenge = codeText.substring(0, codeText.indexOf("|"));
                        challengeList.add(chanllenge);
                        String validate = codeText.substring(codeText.indexOf("|") + 1);
                        validateList.add(validate);
                    }
                    if (challengeList.size()==token.length && validateList.size()==token.length){
                        isNotCode = false;
                        System.out.println("-------打码完成！！！------");
                        break;
                    }
                }
            }
            String commodyId = checkUpNew.searchUpNew();
            try {
            if (commodyId!=null){
                String commodyDetail = checkUpNew.getdetail(commodyId);
                checkUpNew.initAndPush(commodyDetail,challengeList,validateList);
                isNotCode = true;
                Thread.sleep(10000);
            }
            Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void initAndPush(String detail,List<String> challengeList,List<String> validateList){
        int cishu = 0;
        JSONObject commodyJson = JSONObject.parseObject(detail).getJSONObject("data");
        String id = commodyJson.getString("id");
        String productCode = commodyJson.getString("productCode");
        String shopNo = commodyJson.getString("shopNo");
        JSONArray skuList = commodyJson.getJSONArray("skuList");
        for (int i = 0; i < skuList.size(); i++) {
            if (cishu<token.length) {
                JSONObject skuListJson = skuList.getJSONObject(i);
                Integer stock = skuListJson.getInteger("stock");
                if (stock > 1) {
                    String sizeNo = skuListJson.getString("sizeNo");
                    String sizeCode = skuListJson.getString("sizeCode");
                    String skuNo = skuListJson.getString("skuNo");
                    JSONObject orderJson = JSONObject.parseObject(order);
                    JSONArray subOrderArray = orderJson.getJSONArray("subOrderList");
                    JSONObject subOrderList = subOrderArray.getJSONObject(0);
                    subOrderList.put("shopNo", shopNo);
                    JSONArray commodityList = subOrderList.getJSONArray("commodityList");
                    JSONObject commodity = commodityList.getJSONObject(0);
                    commodity.put("productCode", productCode);
                    commodity.put("shoppingcartId", shoppingcartId[cishu]);
                    commodity.put("sizeNo", sizeNo);
                    commodity.put("sizeCode", sizeCode);
                    commodity.put("skuNo", skuNo);
                    commodity.put("shopCommodityId", id);
                    commodityList.set(0, commodity);
                    subOrderList.put("commodityList", commodityList);
                    subOrderArray.set(0, subOrderList);
                    orderJson.put("subOrderList", subOrderArray);
                    String token = CheckUpNew.token[cishu];
                    orderJson.put("validate",validateList.get(cishu));
                    orderJson.put("seccode",validateList.get(cishu)+"|jordan");
                    orderJson.put("challenge",challengeList.get(cishu));
//                    pushOrder(token, orderJson);
                    new Thread(() -> {
                        System.out.println("提交订单");
                        pushOrder(token, orderJson);
                    }).start();
                    cishu++;
                }
            }
            if (cishu >=2){
                break;
            }
        }
    }

    private void pushOrder(String token,JSONObject jsonObject){
        String body = "";
        try {
            System.out.println("准备！！");
            String url = host+createOrder+AESUtil.getTssign(createOrder);
            System.out.println("1");
            CloseableHttpClient client = HttpClients.createDefault();
            System.out.println("2");


            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);
            System.out.println("3");

            //装填参数
            StringEntity s = new StringEntity(jsonObject.toString(), "utf-8");
            System.out.println("3");
            //设置参数到请求对象中
            httpPost.setEntity(s);
            System.out.println("5");
            //设置header信息
            httpPost.setHeader(":Host","wxmall.topsports.com.cn");
            httpPost.setHeader("Authorization",token);
            httpPost.setHeader("Accept-Encoding","gzip, deflate, br");
            httpPost.setHeader("content-type","application/json");
            httpPost.setHeader("Connection","keep-alive");
            httpPost.setHeader("Referer","https://servicewechat.com/wx71a6af1f91734f18/40/page-frame.html");
            httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat");
            //执行请求操作，并拿到结果（同步阻塞）
            System.out.println("6");
            CloseableHttpResponse response = client.execute(httpPost);
            System.out.println("7");
            //获取结果实体
            HttpEntity entity = response.getEntity();
            System.out.println("8");
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "utf-8");
                System.out.println(body);
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getdetail(String id){
        String commodityUrl = CheckUpNew.commodityUrl + id;
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

    private String searchUpNew(){
        String url = host+searchUrl;
        HashMap<String, String> map = new HashMap<>();
        map.put("searchKeyword","");
        map.put("current","1");
        map.put("pageSize","10");
        map.put("sortColumn","upShelfTime");
        map.put("sortType","desc");
        map.put("filterIds","TS100101");
        map.put("shopNo","");//NKND20
        map.put("tssign",AESUtil.getTssign(searchUrl).substring(8));
        String commodityId= null;
        try {
            HttpURLConnection conn = HttpUtil.getConn(url, map);
            String result = HttpUtil.get(conn);
            JSONArray jsonArray = JSONObject.parseObject(result).getJSONObject("data").getJSONObject("spu").getJSONArray("list");
            for (int i = 0; i < jsonArray.size(); i++) {
                String productCode = jsonArray.getJSONObject(i).getString("productCode");
                if (Arrays.asList(itemNo).contains(productCode)){
                    commodityId = jsonArray.getJSONObject(i).getString("id");
                    return commodityId;
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return commodityId;
    }



    private String getChallenge(String token){
        String challenge = null;
        String url = host+initOrder+ AESUtil.getTssign(initOrder);
        String initResult = null;
        try {
            initResult = send(url, initOrderParam, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        challenge = JSONObject.parseObject(initResult).getJSONObject("data").getJSONObject("verificMap").getString("challenge");
        return challenge;
    }

    private String damaStart(String key,String challenge){
        String url = "http://www.damagou.top/apiv1/jiyanRecognize.html?gt=a53a5b6472732e344c776ba27d65302e&type=1005&userkey="+ key +"&challenge="+challenge;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        String body = null;
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
            //获取结果实体
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    private String damaLogin(){
        String url="http://www.damagou.top/apiv1/login.html?username=961714893@qq.com&password=123456";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        String body = null;
        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "utf-8");
            }
            EntityUtils.consume(entity);
            //释放链接
            response.close();
            if (body.length() == 32){
                System.out.println("-------打码狗登录成功！！！-------");
            }else{
                while(true){
                    System.out.println("-------打码狗登录失败！！！--------");
                }
            }
            //获取结果实体
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    private String send(String url,String param,String token)throws Exception{
        CloseableHttpClient client = HttpClients.createDefault();
        String body = "";


        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        StringEntity s = new StringEntity(param, "utf-8");
        //设置参数到请求对象中
        httpPost.setEntity(s);
        //设置header信息
        httpPost.setHeader(":Host","wxmall.topsports.com.cn");
        httpPost.setHeader("Authorization",token);
        httpPost.setHeader("Accept-Encoding","gzip, deflate, br");
        httpPost.setHeader("content-type","application/json");
        httpPost.setHeader("Connection","keep-alive");
        httpPost.setHeader("Referer","https://servicewechat.com/wx71a6af1f91734f18/40/page-frame.html");
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat");
        //执行请求操作，并拿到结果（同步阻塞）
        CloseableHttpResponse response = client.execute(httpPost);
        //获取结果实体
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            //按指定编码转换结果实体为String类型
            body = EntityUtils.toString(entity, "utf-8");
        }
        EntityUtils.consume(entity);
        //释放链接
        response.close();
        return body;
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

    private static String initOrderParam = "{\n" +
            "    \"merchantNo\":\"TS\",\n" +
            "    \"shippingId\":\"\",\n" +
            "    \"subOrderList\":[\n" +
            "        {\n" +
            "            \"shopNo\":\"MTDL01\",\n" +
            "            \"expressType\":2,\n" +
            "            \"commodityList\":[\n" +
            "                {\n" +
            "                    \"productCode\":\"Q47184\",\n" +
            "                    \"skuNo\":\"20201223000948\",\n" +
            "                    \"sizeNo\":\"20160426000046\",\n" +
            "                    \"sizeCode\":\"9\",\n" +
            "                    \"paterId\":\"\",\n" +
            "                    \"num\":1,\n" +
            "                    \"itemFlag\":0,\n" +
            "                    \"assignProNo\":\"CPT210112000326\",\n" +
            "                    \"liveType\":0,\n" +
            "                    \"roomId\":\"\",\n" +
            "                    \"roomName\":\"\",\n" +
            "                    \"shoppingcartId\":\"5c84ae05cd084ae096b0dfff542983b3\",\n" +
            "                    \"shopCommodityId\":\"9f5488e6444e49839dc266463103a8f9\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"purchaseType\":2,\n" +
            "    \"usedPlatformCouponList\":[\n" +
            "\n" +
            "    ]\n" +
            "}";

}
