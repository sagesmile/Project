package Service;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.*;

public class maotai {

    private static String getStoreUrl = "https://api.booking.zhihuihulian.com/store-booking-api-server/mobile/mobileBookingStore/list";
    private static String getStoreParam = "5a0b09abf12c908d39758aafb97f894239bb842308c5d5a7b14521558bb0a3d38ef5c3754feaec9f92a66dd2065765faadf965e5f3998a5c841ab3272b8d61155c54bb28f858d96feb3627684d8d27e7a998adf8c34f396aad5bd4e5023bb2d90ef8522281b31387d03fc6e4ee5ccaee";
    private static String getStoreResult = "81C063C936C1134E81723F879549FC8D8FE6EFE6CD1B661D14EFE85385E556FD";

    public static void main(String[] args) {
        try {
            String sendStoreResult = send(getStoreUrl, getStoreParam);
            isUpNew(sendStoreResult);
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static String send(String url, String text) throws Exception{

        CloseableHttpClient client = HttpClients.createDefault();
        String body = "";


        //创建post方式请求对象
        HttpPost httpPost = new HttpPost(url);

        //装填参数
        StringEntity s = new StringEntity(text, "utf-8");
        //设置参数到请求对象中
        httpPost.setEntity(s);
        //设置header信息
        httpPost.setHeader(":Host","wxmall.topsports.com.cn");
        httpPost.setHeader("authority","api.booking.zhihuihulian.com");
        httpPost.setHeader("scheme","gzip, deflate, br");
        httpPost.setHeader("path","/store-booking-api-server/mobile/mobileBookingStore/list");
        httpPost.setHeader("Connection","keep-alive");
        httpPost.setHeader("Referer","https://servicewechat.com/wxe816d1f52e464065/4/page-frame.html");
        httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat");
        httpPost.setHeader("clientheader","m.1.0.0");
        httpPost.setHeader("areaid","");
        httpPost.setHeader("content-type","application/json");
        httpPost.setHeader("timestamp","");
        httpPost.setHeader("version","1.0.0");
        httpPost.setHeader("accept-encoding","");
        httpPost.setHeader("timestamp","gzip, deflate, br");
        httpPost.setHeader("sign","5149e32080780f645dd6776cd1df12ccf024fc94f9cd0fe37152475c932e1fa6");
        httpPost.setHeader("token","0f64758b07189bf10cafa83692c5a452");

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

    private static void isUpNew(String text){
        if (!text.equals(getStoreResult)){
            System.out.println(text);
            for (int i = 0; i < 30; i++) {
                Toolkit.getDefaultToolkit().beep();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
