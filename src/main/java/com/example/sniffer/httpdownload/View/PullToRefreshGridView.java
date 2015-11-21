package com.example.sniffer.httpdownload.View;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.sniffer.httpdownload.R;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 自定义GridView
 */
public class PullToRefreshGridView extends GridView implements OnScrollListener {

    private View headerView;// 顶部布局文件

    private View footerView;//底部布局文件

    private TextView tv_state_text;

    private TextView tv_lastflsh_date;

    private ImageView iv_refresh_arrow;

    private ProgressBar pb_refresh_arrow;

    private int headerViewHight;// 顶部布局文件的高度

    private int firstVisibleItem;// 当前第一个可见的item位置

    private int lastVisibleItem;//当前最后一个可见的item位置

    private int totalItemCount;//item总数

    private int scrollState;

    private boolean isRemark;// 标记，当前是在listview最顶端摁下的

    private boolean isLoading = false;//标记，是否正在加载

    private int startY;// 摁下时的Y值

    private int state;// 当前的状态

    private final int NONE = 0;// 正常状态
    private final int PULL = 1;// 提示下拉状态
    private final int RELESE = 2;// 提示释放状态
    private final int REFLASHING = 3;// 刷新状态
    private SharedPreferences sp;
    private RotateAnimation animnext;
    private RotateAnimation animlast;
    private RfreshDataListener rfreshlistener;
    private OnScrollDataListener scrolllistener;
    private int footerViewHight;

    public PullToRefreshGridView(Context context) {
        super(context);
        initView(context);
    }

    public PullToRefreshGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public View getHeadView() {
        return headerView;
    }

    public View getFooterView() {
        return footerView;
    }

    public interface OnScrollDataListener {
        void onScrollState(int state);

        void onScrolldata(int firstVisibleItem, int visibleItemCount);

        void onLoad();

    }

    public interface RfreshDataListener {

        void onRfreshData();
    }

    public void setRfreshDataInterface(RfreshDataListener rfreshlistener) {
        this.rfreshlistener = rfreshlistener;
    }

    public void setScrollDataInterface(OnScrollDataListener scrolllistener) {
        this.scrolllistener = scrolllistener;
    }

    /**
     * 初始化界面
     * getMeasuredHeight——获取布局的高度
     *
     * @param context
     */
    private void initView(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        headerView = layoutInflater.inflate(R.layout.header_layout, null);
        footerView = layoutInflater.inflate(R.layout.footer_layout, null);
        tv_state_text = (TextView) headerView.findViewById(R.id.tv_state_text);
        tv_lastflsh_date = (TextView) headerView.findViewById(R.id.tv_lastflsh_date);
        iv_refresh_arrow = (ImageView) headerView.findViewById(R.id.iv_refresh_arrow);
        pb_refresh_arrow = (ProgressBar) headerView.findViewById(R.id.pb_refresh_arrow);
        sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        animnext = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animnext.setDuration(500);
        animnext.setFillAfter(true);
        animlast = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animlast.setDuration(500);
        animlast.setFillAfter(true);
        measureView(headerView);
        measureView(footerView);
        headerViewHight = headerView.getMeasuredHeight();
        footerViewHight = footerView.getMeasuredHeight();
        Log.i("initView", "headerViewHight——" + headerViewHight);
        topPadding(headerView, -headerViewHight);
        topPadding(footerView, -footerViewHight);
        this.setOnScrollListener(this);
    }

    /**
     * 设置header布局的上边距
     *
     * @param toppadding 上边距
     */
    private void topPadding(View view, int toppadding) {
        view.setPadding(view.getPaddingLeft(), toppadding, view.getPaddingRight(),
                view.getPaddingBottom());
        Log.e("Padding", "PaddingLeft:" + view.getPaddingLeft() + "/PaddingTop:" + view.getPaddingTop()
                + "/PaddingRight:" + view.getPaddingRight() + "/PaddingBottom" + view.getPaddingBottom());
        view.invalidate();
    }

    /**
     * 通知父布局，占用的宽 高
     *
     * @param view 子布局
     */
    private void measureView(View view) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, lp.width);
        int height;
        int temoHeight = lp.height;
        if (temoHeight > 0) {
            height = MeasureSpec.makeMeasureSpec(temoHeight, MeasureSpec.EXACTLY);
        } else {
            height = MeasureSpec.makeMeasureSpec(temoHeight, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
        Log.e("lp", "lp.width:" + lp.width + "/lp.height:" + lp.height +
                "/width:" + width + "/height:" + height);
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        if (scrolllistener != null) {
            scrolllistener.onScrollState(scrollState);

            if (lastVisibleItem == totalItemCount
                    && scrollState == SCROLL_STATE_IDLE) {
                if (!isLoading) {
                    isLoading = true;
                    topPadding(footerView, 50);
                    scrolllistener.onLoad();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.lastVisibleItem = firstVisibleItem + visibleItemCount;
        this.firstVisibleItem = firstVisibleItem;
        this.totalItemCount = totalItemCount;
        if (scrolllistener != null) {
            scrolllistener.onScrolldata(firstVisibleItem, visibleItemCount);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (firstVisibleItem == 0) {
                    isRemark = true;
                    startY = (int) ev.getRawY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                if (state == RELESE) {
                    state = REFLASHING;
                    reflashViewByState();
                    //加载数据
                    if (rfreshlistener != null) {
                        rfreshlistener.onRfreshData();
                    }
                } else if (state == PULL) {
                    state = NONE;
                    isRemark = false;
                    reflashViewByState();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * 判断移动过程操作
     *
     * @param ev
     */
    private void onMove(MotionEvent ev) {
        if (!isRemark) {
            return;
        }
        int tempY = (int) ev.getRawY();
        //移动的距离
        int space = (tempY - startY) / 3;
        //Log.e("距离", "—tempY——" + tempY + "——startY——" + startY);
        int toppadding = space - headerViewHight;
        //Log.e("距离", "toppadding——" + toppadding+"-headerViewHight——"+headerViewHight);
        switch (state) {
            case NONE:
                if (space > 0) {
                    state = PULL;
                    iv_refresh_arrow.setVisibility(View.VISIBLE);
                    pb_refresh_arrow.setVisibility(View.GONE);
                    tv_state_text.setText("下拉刷新数据");
                    String time = sp.getString("time", "");
                    tv_lastflsh_date.setText("最近更新:" + time);
                }
                break;
            case PULL:
                topPadding(headerView, toppadding);
                if (space > headerViewHight + 10 && scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    state = RELESE;
                    reflashViewByState();
                }
                break;
            case RELESE:
                topPadding(headerView, toppadding);
                if (space < headerViewHight + 10) {
                    state = PULL;
                    reflashViewByState();
                } else if (space <= 0) {
                    state = NONE;
                    isRemark = false;
                    reflashViewByState();
                }
                break;
        }
    }

    /**
     * 根据滑动的状态,改变界面显示
     */
    public void reflashViewByState() {
        switch (state) {
            case NONE:
                iv_refresh_arrow.clearAnimation();
                topPadding(headerView, -headerViewHight);
                break;
            case PULL:
                iv_refresh_arrow.setVisibility(View.VISIBLE);
                pb_refresh_arrow.setVisibility(View.GONE);
                iv_refresh_arrow.clearAnimation();
                iv_refresh_arrow.setAnimation(animlast);
                tv_state_text.setText("下拉刷新数据");
                break;
            case RELESE:
                tv_state_text.setText("松开刷新数据");
                iv_refresh_arrow.clearAnimation();
                iv_refresh_arrow.setAnimation(animnext);
                break;
            case REFLASHING:
                topPadding(headerView, 0);
                iv_refresh_arrow.setVisibility(View.GONE);
                pb_refresh_arrow.setVisibility(View.VISIBLE);
                tv_state_text.setText("正在刷新数据...");
                iv_refresh_arrow.clearAnimation();
                break;
        }
    }

    /**
     * 保存更新数据的时间
     */
    public void reflashComplete() {
        state = NONE;
        isRemark = false;
        reflashViewByState();
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM年dd月 hh:mm:ss");
        Date data = new Date(System.currentTimeMillis());
        String time = format.format(data);
        sp.edit().putString("time", time).commit();
    }
    public void loadComplete() {
        isLoading = false;
        topPadding(footerView, -footerViewHight);
    }

}
