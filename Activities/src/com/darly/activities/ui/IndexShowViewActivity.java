package com.darly.activities.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.darly.activities.adapter.CityAdapter;
import com.darly.activities.adapter.LocalAdapter;
import com.darly.activities.app.AppStack;
import com.darly.activities.base.BaseActivity;
import com.darly.activities.common.IAPoisDataConfig;
import com.darly.activities.common.Literal;
import com.darly.activities.common.LogApp;
import com.darly.activities.common.PreferencesJsonCach;
import com.darly.activities.model.BaseCityInfo;
import com.darly.activities.model.BaseOrgInfo;
import com.darly.activities.model.IARoomName;
import com.darly.activities.model.IARoomNameHttp;
import com.darly.activities.model.IARoomPoint;
import com.darly.activities.model.OrgBase;
import com.darly.activities.model.OrgBaseData;
import com.darly.activities.model.RoomInfor;
import com.darly.activities.poll.HttpTasker;
import com.darly.activities.poll.ThreadPoolManager;
import com.darly.activities.widget.intel.BaseInterlgent;
import com.darly.activities.widget.intel.InterlgentUtil;
import com.darly.activities.widget.load.ProgressDialogUtil;
import com.darly.activities.widget.spinner.BaseSpinner;
import com.google.gson.Gson;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

/**
 * @author Zhangyuhui IndexShowViewActivity 上午9:01:37 TODO
 *         展示机构平面图页面，取自帮忙医项目的智能导检。
 */
@ContentView(R.layout.activity_index_show_view)
public class IndexShowViewActivity extends BaseActivity {

	/**
	 * TODO顶部标签卡
	 */
	@ViewInject(R.id.main_header_text)
	private TextView title;

	/**
	 * TODO下拉菜单选择列表
	 */
	@ViewInject(R.id.main_city_spinner)
	private BaseSpinner city_spinner;
	/**
	 * TODO下拉菜单选择列表
	 */
	@ViewInject(R.id.main_org_spinner)
	private BaseSpinner org_spinner;
	/**
	 * TODO线程管理
	 */
	private ThreadPoolManager manager;
	/**
	 * TODO生成图层
	 */
	public BaseInterlgent interlgent;
	/**
	 * TODO图层容器
	 */
	@ViewInject(R.id.main_container)
	private RelativeLayout main_container;
	/**
	 * TODO计时器
	 */
	private Timer timer;

	/**
	 * TODO房间全部信息
	 */
	public ArrayList<RoomInfor> roomInfo;

	/**
	 * 咨询链接
	 */
	private String infoUrl = "http://test.rayelink.com/APIAccount/GetOrganizationInfo";
	/**
	 * 数据链接
	 */
	private String dataUrl = "http://test.rayelink.com/APIQueuingSystem/GetData";

	private final int KEEP = 1000;
	private int NEXTKEEP = 10000;

	/**
	 * TODO 加载过场动画类
	 */
	private ProgressDialogUtil loading;

	/**
	 * 下午2:58:17
	 * 
	 * @author Zhangyuhui IndexShowViewActivity.java TODO 城市机构信息列表。
	 */
	private ArrayList<BaseCityInfo> city_Info;

	/**
	 * 下午3:39:31
	 * 
	 * @author Zhangyuhui IndexShowViewActivity.java TODO 选中的机构信息。
	 */
	private BaseOrgInfo selectOrg;

	private boolean isStop = false;

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.main_container:
			Intent intent = new Intent(this, IndexZoomViewActivity.class);
			startActivity(intent);
			timer.cancel();
			interlgent.setFlag(false);
			isStop = true;
			break;

		default:
			break;
		}
	}

	@Override
	public void initView(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		ViewUtils.inject(this);// 注入view和事件
		loading = new ProgressDialogUtil(this);
		loading.setMessage("加载中...");
		loading.show();
		main_container.setLayoutParams(new LinearLayout.LayoutParams(
				Literal.width, Literal.width * IAPoisDataConfig.babaibanh
						/ IAPoisDataConfig.babaibanw));

		setSpinner();
		// 初始化从第一项开始
		city_spinner.getSpinner().setSelection(0);
		org_spinner.getSpinner().setSelection(0);

		initImageAndThread();
	}

	/**
	 * 
	 * 下午2:44:51
	 * 
	 * @author Zhangyuhui IndexShowViewActivity.java TODO 设置下拉列表。
	 */
	private void setSpinner() {
		// 由于服务端没有城市机构信息。故而虚拟一下数据
		city_Info = new ArrayList<BaseCityInfo>();

		ArrayList<BaseOrgInfo> org = new ArrayList<BaseOrgInfo>();
		org.add(new BaseOrgInfo(12, "静安", 1));
		org.add(new BaseOrgInfo(24, "徐汇", 1));
		org.add(new BaseOrgInfo(31, "八百", 1));

		city_Info.add(new BaseCityInfo(2001, "上海", org));
		city_Info.add(new BaseCityInfo(2002, "北京", org));

		// TODO Auto-generated method stub
		city_spinner.getSpinner().setAdapter(
				new CityAdapter(city_Info, R.layout.ia_guide_item_city, this));

		city_spinner.getSpinner().setOnItemSelectedListener(
				new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						BaseCityInfo info = (BaseCityInfo) parent
								.getItemAtPosition(position);
						org_spinner.getSpinner().setAdapter(
								new LocalAdapter(info.city_org,
										R.layout.ia_guide_item_city,
										IndexShowViewActivity.this));
						org_spinner.getSpinner().setOnItemSelectedListener(
								new OnItemSelectedListener() {

									@Override
									public void onItemSelected(
											AdapterView<?> parent, View view,
											int position, long id) {
										// TODO Auto-generated method stub
										// 选择正确的机构。
										selectOrg = (BaseOrgInfo) parent
												.getItemAtPosition(position);
										firstStep();
									}

									@Override
									public void onNothingSelected(
											AdapterView<?> parent) {
										// TODO Auto-generated method stub

									}
								});
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}

				});
	}

	/**
	 * 上午11:00:34
	 * 
	 * @author Zhangyuhui MainActivity.java TODO初始化ImageLoader和线程池选项
	 */
	private void initImageAndThread() {
		manager = ThreadPoolManager.getInstance(ThreadPoolManager.TYPE_FIFO, 5);
	}

	/**
	 * 
	 * 上午11:03:04
	 * 
	 * @author Zhangyuhui MainActivity.java
	 *         TODO判断网络是否正常。正常则继续请求数据，异常状态使用上次缓存下来资料
	 */
	public void firstStep() {
		if (!AppStack.isNetworkConnected(this)) {
			if (loading != null) {
				loading.dismiss();
			}
			Toast.makeText(this, "网络异常，请检查网络！", KEEP).show();
			String info = PreferencesJsonCach.getInfo("GETINFO"
					+ selectOrg.org_id, this);
			String data = PreferencesJsonCach.getInfo("GETDATA"
					+ selectOrg.org_id, this);
			// 初次没有缓存则直接跳过
			if (info != null) {
				getOrgAndPoint(new Gson().fromJson(info, IARoomNameHttp.class));
			}
			// 初次没有缓存则直接跳过
			if (data != null) {
				interlgent.ReDraw(setInfoRoom(
						new Gson().fromJson(data, OrgBase.class).getModel(),
						roomInfo));
			}
		} else {
			// 请求服务器平面图数据。
			JSONObject object = new JSONObject();
			try {
				object.put("OrganizationID", "" + selectOrg.org_id);
			} catch (Exception e) {
				// TODO: handle exception
			}
			ArrayList<BasicNameValuePair> par = new ArrayList<BasicNameValuePair>();
			par.add(new BasicNameValuePair("param", object.toString()));
			manager.start();
			// 获取屏幕的宽高。这几
			manager.addAsyncTask(new HttpTasker(IndexShowViewActivity.this,
					par, infoUrl, null, handler, true, Literal.GET_HANDLER,
					true));
			// 请求服务器平面图数据。
		}
	}

	/**
	 * @param roomOrgpari
	 * @auther Darly Fronch 2015 下午2:20:34 TODO获取机构信息类，即获取房间功能对应表格roomOrgpari
	 */
	public void getOrgAndPoint(IARoomNameHttp roomOrgpari) {
		// -----------如何建立关系----------
		int[] arg = null;
		switch (selectOrg.org_id) {
		case 12:
			arg = getResources().getIntArray(R.array.jingan_roomnub);
			break;
		case 24:
			arg = getResources().getIntArray(R.array.xuhui_roomnub);
			break;
		case 31:
			arg = getResources().getIntArray(R.array.babaiban_roomnub);
			break;
		default:
			break;
		}

		ArrayList<IARoomPoint> point = IAPoisDataConfig.getModelTest(arg,
				selectOrg.org_id);
		roomInfo = getRoomInfr(roomOrgpari.model, point/*
														 * roomOrgpari.point
														 * 由于服务器暂时还未传递，制造假数据
														 */);
		// -----------如何建立关系----------
		main_container.removeAllViews();
		interlgent = new BaseInterlgent(this, roomInfo);
		// 获取到背景图片后进行Bitmap缓存。
		imageLoader.loadImage(roomOrgpari.Organizationplan,
				new ImageLoadingListener() {

					@Override
					public void onLoadingStarted(String arg0, View arg1) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingFailed(String arg0, View arg1,
							FailReason arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onLoadingComplete(String arg0, View arg1,
							Bitmap arg2) {
						// TODO Auto-generated method stub

						Bitmap back = InterlgentUtil.zoomImage(arg2,
								Literal.width, Literal.width
										* IAPoisDataConfig.babaibanh
										/ IAPoisDataConfig.babaibanw);
						LogApp.i(back.toString());
						interlgent.setBackGroud(back);
					}

					@Override
					public void onLoadingCancelled(String arg0, View arg1) {
						// TODO Auto-generated method stub

					}
				});
		main_container.addView(interlgent);
		main_container.setOnClickListener(this);

	}

	@Override
	public void initData() {
		// TODO Auto-generated method stub
		title.setText(getClass().getSimpleName());

	}

	@Override
	public void refreshGet(Object object) {
		// TODO Auto-generated method stub
		Log.i("handler", "IALiteral.GETINFO");
		if (object != null) {
			String jsonInfo = (String) object;
			PreferencesJsonCach.putValue("GETINFO" + selectOrg.org_id,
					jsonInfo, this);
			IARoomNameHttp roomOrgpari = new Gson().fromJson(jsonInfo,
					IARoomNameHttp.class);
			if (roomOrgpari != null && roomOrgpari.model != null) {
				getOrgAndPoint(roomOrgpari);
				getDataFHttp();
			}
		}
	}

	@Override
	public void refreshPost(Object object) {
		// TODO Auto-generated method stub
		Log.i("handler", "IALiteral.GETDATA");
		if (object != null) {
			String jsonData = (String) object;
			PreferencesJsonCach.putValue("GETDATA" + selectOrg.org_id,
					jsonData, this);
			OrgBase base = new Gson().fromJson(jsonData, OrgBase.class);
			startTimer();
			interlgent.ReDraw(setInfoRoom(base.getModel(), roomInfo));
		}
	}

	/**
	 * @auther Darly Fronch 2015 下午2:12:53 TODO启动计时，每隔10s进行一次数据刷新
	 */
	public void startTimer() {
		// TODO Auto-generated method stub
		if (timer == null) {
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub\
					getDataFHttp();
				}
			}, KEEP, NEXTKEEP);
		}
	}

	/**
	 * @auther Darly Fronch 2015 下午2:54:39 TODO生成全部信息
	 */
	private ArrayList<RoomInfor> getRoomInfr(ArrayList<IARoomName> model,
			ArrayList<IARoomPoint> point) {
		ArrayList<RoomInfor> room = new ArrayList<RoomInfor>();
		// 获取所有对应关系表格。
		for (int i = 0, len = point.size(); i < len; i++) {
			for (int j = 0, dat = model.size(); j < dat; j++) {
				if (model.get(j).RoomID.equals(point.get(i).getRoomNum())) {
					room.add(new RoomInfor(point.get(i).getRoomNum(), Integer
							.parseInt(model.get(j).DepartmentID), -1, point
							.get(i).getRoomPoint()));
				}
			}
		}
		return room;
	}

	/**
	 * @auther Darly Fronch 2015 下午3:01:05 TODO 对原始数据进行变更。替换状态
	 */
	public ArrayList<RoomInfor> setInfoRoom(OrgBaseData models,
			ArrayList<RoomInfor> roomIn) {
		for (int i = 0, len = roomIn.size(); i < len; i++) {
			// 判断获取到的数据假如没有此字段，则展示原始页面。
			// 剔除不用体检的项目
			if (loading != null) {
				loading.dismiss();
			}
			if (models.getAll() != null) {
				for (int s = 0, lent = models.getAll().length; s < lent; s++) {
					if (roomIn.get(i).getDepartId() == Integer.parseInt(models
							.getAll()[s] + "")) {
						roomIn.get(i).setRoomStauts(0);
						break;
					}
				}
			}
			// 剔除已经体检完成的项目
			if (models.getDone() != null) {
				for (int a = 0, lent = models.getDone().size(); a < lent; a++) {
					if (roomIn.get(i).getDepartId() == models.getDone().get(a)
							.getDepartmentID()) {
						roomIn.get(i).setRoomStauts(1);
					}
				}
			}
			if (models.getNext() != null) {
				for (int b = 0, lent = models.getNext().size(); b < lent; b++) {

					String num = models.getNext().get(b).getRoomID();
					if (num.contains("-")) {
						num = num.substring(0, num.indexOf("-"));
					}
					String room = roomIn.get(i).getRoomNum();
					if (room.contains("-")) {
						room = num.substring(0, num.indexOf("-"));
					}
					if (room.equals(num)) {
						roomIn.get(i).setRoomStauts(2);
						int X = 0;
						int Y = 0;
						ArrayList<Point> pos = null;
						for (RoomInfor roomInfor : roomIn) {
							if (roomInfor.getRoomNum().equals(room)) {
								pos = roomInfor.getRoomPoint();
								break;
							}
						}
						if (pos != null) {
							int lenth = pos.size();
							for (int s = 0; s < lenth; s++) {
								// 无论多少个点。现在获取所有的X轴和与Y轴和。
								Point p = pos.get(s);
								X += p.x;
								Y += p.y;
							}
							// 获取到背景图片后进行Bitmap缓存。
							Drawable drawable = getResources().getDrawable(
									R.drawable.next_check);
							Bitmap nextImage = ((BitmapDrawable) drawable)
									.getBitmap();
							int heighe = nextImage.getHeight();
							int width = nextImage.getWidth();
							LogApp.i(nextImage.toString() + heighe + width);
							interlgent.setNextImage(nextImage, X / lenth
									- width / 2, Y / lenth - heighe);
						}

					}
				}
			}
		}

		return roomIn;
	}

	/**
	 * @auther Darly Fronch 2015 下午2:21:12 TODO获取机构刷新数据。
	 */
	public void getDataFHttp() {
		JSONObject object = new JSONObject();
		try {
			// 测试数据
			object.put("UserMobile", "18321127312");
			object.put("OrganizationID", "" + selectOrg.org_id);
		} catch (Exception e) {
			Log.i("getDataFHttp", e.getMessage().toString());
		}
		ArrayList<BasicNameValuePair> par = new ArrayList<BasicNameValuePair>();
		par.add(new BasicNameValuePair("param", object.toString()));
		manager.addAsyncTask(new HttpTasker(IndexShowViewActivity.this, par,
				dataUrl, null, handler, true, Literal.POST_HANDLER, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.darly.activities.base.BaseActivity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (isStop) {
			loading.show();
			firstStep();
			isStop = false;
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (timer != null) {
			timer.cancel();
		}
		super.onDestroy();
	}
}
