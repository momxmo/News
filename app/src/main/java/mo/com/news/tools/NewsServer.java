package mo.com.news.tools;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import mo.com.news.bean.News;

/**
 * Created by Administrator on 2015/8/12.
 */
public class NewsServer {

    /**
     * 这个方法是用于获取信息中，解析xml数据中的信息
     *
     * @param path 资源的路径
     */
    public static List<News> getAllNewsItems(String path) throws IOException, XmlPullParserException {

        URL mUrl = new URL(path);

        List<News> list = new ArrayList<News>();
        //获取HTTP连接对象
        HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();

        conn.setRequestMethod("GET");   //设置向服务器的请求方式//必须要大写
        conn.setConnectTimeout(10000);      //设置连接的超时时间
        conn.setReadTimeout(5000);      //设置读取资源的超时时间
        conn.connect();         //开始连接

        int code = conn.getResponseCode();//获取响应的状态码

        if (code == 200) {
            //表示成功获取资源文件
            InputStream in = conn.getInputStream();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(in, "UTF-8");

            int evenType = parser.getEventType();
            News news = null;

            while (evenType != XmlPullParser.END_DOCUMENT) {
                if (evenType == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        news = new News();
                    } else if ("title".equals(parser.getName())) {
                        news.setTitle(parser.nextText());
                    } else if ("description".equals(parser.getName())) {
                        news.setDescription(parser.nextText());
                    } else if ("image".equals(parser.getName())) {
                        news.setImage(parser.nextText());
                    } else if ("type".equals(parser.getName())) {
                        news.setType(parser.nextText());
                    } else if ("comment".equals(parser.getName())) {
                        news.setComment(parser.nextText());
                    }
                } else if (evenType == XmlPullParser.END_TAG) {

                    //当item标签位置结束的时候，将对象加入到集合中
                    if ("item".equals(parser.getName())) {
                        list.add(news);
                    }
                }

                evenType = parser.next();
            }
        }
        return list;
    }
}
