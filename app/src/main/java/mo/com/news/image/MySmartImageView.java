package mo.com.news.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Administrator on 2015/8/13.
 * <p/>
 * 自己制作的图片加载框架
 * 玩玩而已啦
 */
public class MySmartImageView extends ImageView {
    private static final int SUCCESS = 1;
    private static final int ERROR = 2;
    private File imag_file = null;

    public MySmartImageView(Context context) {
        super(context);
    }

    public MySmartImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySmartImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int w = msg.what;
            switch (w) {
                case SUCCESS:

                    Bitmap bmp = (Bitmap) msg.obj;
                    setImageBitmap(bmp);
                    break;
                case ERROR:
                    int errorCode = (Integer) msg.obj;
                    setImageResource(errorCode);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * * 作业：
     * 图片缓存
     * <p/>
     * 图片请求的步骤：
     * 1.  请求内存里面有没有图片 --没有 --执行2  --有 --执行6
     * 2.	去看本地文件夹里面有没有这张图片 -- 没有 ，执行3 --有--执行6
     * 3. 去网络请求图片。--执行4
     * 4.把这张图片存到内存里面 -- 5
     * 5。把这张图片存到磁盘里面 --6
     * 6.显示这正图片
     * <p/>
     * <p/>
     * 通过url直接加载图片到ImageView对象中
     *
     * @param path
     * @param errorCode
     */
    public void setImageUrl(final String path, final int errorCode) {

        //性能的优化，使用图片缓存，减少浏览的损耗

        //获取文件名
        String filename = path.substring(path.lastIndexOf("/"));
        File file = getContext().getCacheDir();
        imag_file = new File(file, filename);

        //如果图片存在
        if (imag_file.exists()) {

            FileInputStream in = null;
            try {
                in = new FileInputStream(imag_file);
                Log.i("MySmartImageView", "----------");
                Bitmap bmp = BitmapFactory.decodeStream(in);

                setImageBitmap(bmp);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else { //如果缓存中没有图片，直接去网络中获取

            //创建子线程进行网路图片的加载
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(path);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                        conn.setConnectTimeout(10000);  //设置连接超时时间
                        conn.setReadTimeout(5000);    //设置读取数据时间

                        int code = conn.getResponseCode(); //获取响应码

                        if (code == 200) {
                            //连接成功，并返回数据
                            InputStream in = conn.getInputStream();

                            //将图片写到缓存区中
                            FileOutputStream out = null;
                            try {
                                out = new FileOutputStream(imag_file);
                                byte[] bt = new byte[1024];
                                int len = 0;
                                while ((len = in.read(bt)) != -1) {
                                    out.write(bt, 0, len);

                                    out.flush();
                                }
                                out.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Bitmap  bm = BitmapFactory.decodeStream(in);

                            //成功获取到图片数据，发送给主线程
                            Message mes = new Message();
                            mes.what = SUCCESS;
                            mes.obj = bm;
                            handler.sendMessage(mes);

                            in.close();

                        } else {
                            //获取图片失败，发送给主线程
                            Message mes = new Message();
                            mes.what = ERROR;
                            mes.obj = errorCode;
                            handler.sendMessage(mes);
                        }

                    } catch (Exception e) {
                        //获取图片失败，发送给主线程
                        Message mes = new Message();
                        mes.what = ERROR;
                        mes.obj = errorCode;
                        handler.sendMessage(mes);
                        e.printStackTrace();
                    }
                }
            }).start();
        }

    }


}
