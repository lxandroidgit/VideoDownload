package com.example.sniffer.httpdownload.View;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 继承TextView类实现跑马灯效果
 * @author sniffer
 *
 */
public class marqueeTextView extends TextView{

	//有style样式的话会用有defStyle参数的构造方法
	public marqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
	}
	//有属性时会用有attrs参数的构造方法
	public marqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	//用代码new对象时，会用最后一个构造方法
	public marqueeTextView(Context context) {
		super(context);
	}
	//isFocused方法为有没有获取焦点，想要TextView获取焦点，可以强制性return true
	@Override
	public boolean isFocused() {
	return true;
}
}
