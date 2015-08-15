package mo.com.news;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import mo.com.news.bean.News;
import mo.com.news.image.MySmartImageView;
import mo.com.news.tools.NewsServer;

public class MainActivity extends AppCompatActivity {
    ListView lv;
    List<News> listnews;
    private RelativeLayout R_ProBar;
    private final static int SUCCESS = 0;
    private final static int FAILE = 1;

    NewAdapter newsAdapter;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            int w = msg.what;
            R_ProBar.setVisibility(View.GONE);
            switch (w) {
                case SUCCESS:
                    //显示 数据
                    if (newsAdapter == null) {
                        newsAdapter = new NewAdapter();
                        lv.setAdapter(newsAdapter);
                    } else {
                        newsAdapter.notifyDataSetChanged();
                    }
                    break;
                case FAILE:
                    // 弹 个土司
                    Toast.makeText(MainActivity.this, "获取失败...", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.lv);
        R_ProBar = (RelativeLayout) findViewById(R.id.R_ProBar);

        R_ProBar.setVisibility(View.VISIBLE);
        //开启一个子线程去获取网络的新闻
        new Thread(new Runnable() {
            public void run() {
                try {
                    listnews = NewsServer.getAllNewsItems(getResources().getString(R.string.news_path));
                    if (listnews != null) {
                        Message m = Message.obtain();
                        m.what = SUCCESS;
                        handler.sendMessage(m);
                    } else {

                        //没有值，返回失败信息
                        Message m = Message.obtain();
                        m.what = FAILE;
                        handler.sendMessage(m);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    //抛出异常，访问失败
                    Message m = Message.obtain();
                    m.what = FAILE;
                    handler.sendMessage(m);
                }


            }
        }).start();

    }


    /**
     * 定义新闻的适配器
     */
    class NewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return listnews.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            //先判断view对象是否为空,防止过多创建view对象，导致内存溢出
            if (convertView == null) {
                view = View.inflate(MainActivity.this, R.layout.item_news, null);
            } else {
                view = convertView;
            }

            TextView title = (TextView) view.findViewById(R.id.item_titile);
            TextView description = (TextView) view.findViewById(R.id.item_description);
            MySmartImageView image = (MySmartImageView) view.findViewById(R.id.item_iamge);
            TextView comment = (TextView) view.findViewById(R.id.item_comment);

            //获得新闻对象
            News news = listnews.get(position);

            title.setText(news.getTitle());
            description.setText(news.getDescription());

            if (news.getType().equals("1")) {
                comment.setText("评论("+news.getComment()+")");
            } else if (news.getType().equals("2")) {
                comment.setText("视频");
            } else if (news.getType().equals("3")) {
                comment.setText("LVLE");
            }

            //使用开源框架，直接通过图片的url路径获取图片，
            // 不用再去设置handler子线程去处理
            //框架已经帮我们做好了
            image.setImageUrl(news.getImage(),R.drawable.error);
            return view;
        }
    }


}
