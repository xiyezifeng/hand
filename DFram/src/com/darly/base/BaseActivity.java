package com.darly.base;

import java.util.List;

import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

import com.androidquery.AQuery;
import com.darly.common.Literal;
import com.darly.common.NetUtils;
import com.darly.common.ToastApp;
import com.darly.db.SnoteTable;
import com.darly.dfram.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @ClassName: BaseActivity
 * @Description: 
 *               TODO(Activity工具基础类，实现了点击事件的接口，所有Activity全部继承此类，是一个至关重要的父类，整个APP全局的变量可以在这里书写
 *               。)
 * @author 张宇辉 zhangyuhui@octmami.com
 * @date 2014年12月15日 上午8:49:27
 *
 */
public abstract class BaseActivity extends FragmentActivity implements
		OnClickListener {
	protected SnoteTable table;
	protected AQuery aq;
	protected List<View> pageviews;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	protected DisplayImageOptions options;
	protected DisplayImageOptions options_big;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// 初始化LOG
		if (table == null) {
			table = new SnoteTable(this);
		}
		aq = new AQuery(this);

		if (!imageLoader.isInited()) {

			ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(
					this).threadPoolSize(5)
					.threadPriority(Thread.NORM_PRIORITY - 1)
					.denyCacheImageMultipleSizesInMemory()
					.memoryCacheSize(4 * 1024 * 1024)
					.discCacheFileNameGenerator(new Md5FileNameGenerator())
					.tasksProcessingOrder(QueueProcessingType.LIFO).build();
			imageLoader.init(configuration);
		}
		// 设置参数，加载每个图片的详细参数和是否存储、缓存的问题。
		options = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.empty_photo)
				.showImageForEmptyUri(R.drawable.empty_photo)
				.showImageOnFail(R.drawable.empty_photo).cacheInMemory(true)
				.bitmapConfig(Config.RGB_565).cacheOnDisc(true).build();

		options_big = new DisplayImageOptions.Builder()
				.showStubImage(R.drawable.empty_ph_big)
				.showImageForEmptyUri(R.drawable.empty_ph_big)
				.showImageOnFail(R.drawable.empty_ph_big).cacheInMemory(true)
				.bitmapConfig(Config.RGB_565).cacheOnDisc(true).build();

		if (Literal.width == 0 || Literal.height == 0) {
			Literal.getWidth(this);
		}

		initView();
		initData();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub

		if (!NetUtils.isConnected(this)) {
			ToastApp.showShort(this, "网络连接异常，请检查网络！");
		}
		super.onResume();
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		imageLoader.clearMemoryCache();
		return true;
	}

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:48:56 Project_Name:DFram
	 * Description:初始化界面控件，获取界面XML的方法体，可以在这里对所有XML中的控件进行实例。 Throws
	 */
	public abstract void initView();

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:13 Project_Name:DFram
	 * Description:加载数据方法体，所有的参数数据都可以放到这里进行实现，当然和InitView功能差不多。 Throws
	 */
	public abstract void initData();

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:31 Project_Name:DFram
	 * Description
	 * :通过Literal.DA_HANDLER调取Handler，由Handler进行调用的一个方法体，可以实现界面更改，界面刷新等功能。
	 * Throws
	 */
	public abstract void refresh(Object object);

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:34 Project_Name:DFram
	 * Description
	 * :通过Literal.GET_HANDLER调取Handler，由Handler进行调用的一个方法体，可以实现界面更改，界面刷新等功能。
	 * Throws
	 */
	public abstract void refreshGet(Object object);

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:37 Project_Name:DFram
	 * Description
	 * :通过Literal.POST_HANDLER调取Handler，由Handler进行调用的一个方法体，可以实现界面更改，界面刷新等功能。
	 * Throws
	 */
	public abstract void refreshPost(Object object);

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:40 Project_Name:DFram
	 * Description
	 * :通过Literal.SHOW_HANDLER调取Handler，由Handler进行调用的一个方法体，可以实现界面更改，界面刷新等功能。
	 * Throws
	 */
	public abstract void refreshShow(Object object);

	/**
	 * Auther:张宇辉 User:zhangyuhui 2015年1月5日 上午9:49:42 Project_Name:DFram
	 * Description
	 * :通过Literal.HINT_HANDLER调取Handler，由Handler进行调用的一个方法体，可以实现界面更改，界面刷新等功能。
	 * Throws
	 */
	public abstract void refreshHint(Object object);

}
