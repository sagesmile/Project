package utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

public class HttpUtil {

        /**
         * 请求类型： GET
         */
        public final static String GET = "GET";
        /**
         * 请求类型： POST
         */
        public final static String POST = "POST";

        /**
         * 模拟Http Get请求
         * @param urlStr
         *             请求路径
         * @param paramMap
         *             请求参数
         * @return
         * @throws Exception
         */
        public static HttpURLConnection getConn(String urlStr, Map<String, String> paramMap) throws Exception {
            urlStr = urlStr + "?" + getParamString(paramMap);
            System.out.println(urlStr);
            HttpURLConnection conn = null;
            //创建URL对象
            URL url = new URL(urlStr);
            //获取URL连接
            conn = (HttpURLConnection) url.openConnection();
            return conn;
        }

        public static String get(HttpURLConnection conn) throws Exception{
            try{
                //设置通用的请求属性
                setHttpUrlConnection(conn, GET);
                //建立实际的连接
                conn.connect();
                //获取响应的内容
                return readResponseContent(conn.getInputStream());
            }finally{
                if(null!=conn){
                    conn.disconnect();
                }
            }
        }


        public static String send(String url, JSONObject jsonObject,String token) throws Exception{

            CloseableHttpClient client = HttpClients.createDefault();
            String body = "";


            //创建post方式请求对象
            HttpPost httpPost = new HttpPost(url);

            //装填参数
            StringEntity s = new StringEntity(jsonObject.toString(), "utf-8");
            //设置参数到请求对象中
            httpPost.setEntity(s);
            //设置header信息
            httpPost.setHeader(":Host","wxmall.topsports.com.cn");
            httpPost.setHeader("Authorization",token);
            httpPost.setHeader("Accept-Encoding","gzip, deflate, br");
            httpPost.setHeader("content-type","application/json");
            httpPost.setHeader("Connection","keep-alive");
            httpPost.setHeader("Referer","https://servicewechat.com/wx71a6af1f91734f18/39/page-frame.html");
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

        /**
         * 设置Http连接属性
         * @param conn
         *             http连接
         * @return
         * @throws ProtocolException
         * @throws Exception
         */
        private static void setHttpUrlConnection(HttpURLConnection conn, String requestMethod) throws ProtocolException{
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36 MicroMessenger/7.0.9.501 NetType/WIFI MiniProgramEnv/Windows WindowsWechat");
            conn.setRequestProperty("Proxy-Connection", "Keep-Alive");
            conn.setRequestProperty("Referer","https://servicewechat.com/wx71a6af1f91734f18/21/page-frame.html");
            conn.setRequestProperty("Accept-Encoding","gzip, deflate, br");
            conn.setRequestProperty("content-type","application/json");
            if(null!=requestMethod && POST.equals(requestMethod)){
                conn.setDoOutput(true);
                conn.setDoInput(true);
            }
        }

        /**
         * 将参数转为路径字符串
         * @param
         * @return
         */
        public static String getParamString(Map<String, String> paramMap){
            if(null==paramMap || paramMap.isEmpty()){
                return "";
            }
            StringBuilder builder = new StringBuilder();
            for(String key : paramMap.keySet() ){
                builder.append("&")
                        .append(key).append("=").append(paramMap.get(key));
            }
            return builder.deleteCharAt(0).toString();
        }

        /**
         * 读取响应字节流并将之转为字符串
         * @param in
         *         要读取的字节流
         * @return
         * @throws IOException
         */
        private static String readResponseContent(InputStream in) throws IOException{
            Reader reader = null;
            StringBuilder content = new StringBuilder();
            try{
                reader = new InputStreamReader(in);
                char[] buffer = new char[1024];
                int head = 0;
                while( (head=reader.read(buffer))>0 ){
                    content.append(new String(buffer, 0, head));
                }
                return content.toString();
            }finally{
                if(null!=in){
                    in.close();
                }
                if(null!=reader){
                    reader.close();
                }
            }
        }

    }

