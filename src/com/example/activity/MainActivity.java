package com.example.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.Data.GlobalConstants;
import com.example.Data.TempData;
import com.example.Receiver.MReceiver;
import com.example.myscanapp.R;
import com.example.utils.CharacterParser;
import com.example.utils.ClearEditText;
import com.example.utils.JsonUtils;
import com.example.utils.PinyinComparator;
import com.example.utils.SideBar;
import com.example.utils.SortAdapter;
import com.example.utils.SortModel;
import com.example.utils.SideBar.OnTouchingLetterChangedListener;

/**
 * 经销商选择界面
 * 
 * @author dell
 * 
 */
public class MainActivity extends Activity {
	private ListView sortListView;
	private SideBar sideBar;
	private TextView dialog;
	private SortAdapter adapter;
	private ClearEditText mClearEditText;
	private MReceiver mReceive;
	private ImageView refreshImageView;
	private Animation anim;
	private RequestQueue mQueue;

	/**
	 * 汉字转换成拼音的类
	 */
	private CharacterParser characterParser;
	private List<SortModel> SourceDateList;

	/**
	 * 根据拼音来排列ListView里面的数据类
	 */
	private PinyinComparator pinyinComparator;

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				// 若网络连接则发送网络请求，请求经销商名
				sendRequest();
				break;
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initRefreshImage();
		anim = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.round_loading);
		mQueue = Volley.newRequestQueue(this);

		// 注册监听网络状态改变广播接受者
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		mReceive = new MReceiver(this);
		registerReceiver(mReceive, intentFilter);
		
	}

	// 刷新旋钮初始化
	private void initRefreshImage() {

		refreshImageView = (ImageView) findViewById(R.id.refresh);
		refreshImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				refreshImageView.startAnimation(anim);
				sendRequest();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceive);
	}

	/**
	 * 发送网络请求(这里我用模拟数据源,原本方法为注释代码)
	 */
	private void sendRequest(){
		 String[] NAMES = new String[] { "宋江", "卢俊义", "吴用",
			"公孙胜", "关胜", "林冲", "秦明", "呼延灼", "花荣", "柴进", "李应", "朱仝", "鲁智深",
			"武松", "董平", "张清", "杨志", "徐宁", "索超", "戴宗", "刘唐", "李逵", "史进", "穆弘",
			"雷横", "李俊", "阮小二", "张横", "阮小五", " 张顺", "阮小七", "杨雄", "石秀", "解珍",
			" 解宝", "燕青", "朱武", "黄信", "孙立", "宣赞", "郝思文", "韩滔", "彭玘", "单廷珪",
			"魏定国", "萧让", "裴宣", "欧鹏", "邓飞", " 燕顺", "杨林", "凌振", "蒋敬", "吕方",
			"郭 盛", "安道全", "皇甫端", "王英", "扈三娘", "鲍旭", "樊瑞", "孔明", "孔亮", "项充",
			"李衮", "金大坚", "马麟", "童威", "童猛", "孟康", "侯健", "陈达", "杨春", "郑天寿",
			"陶宗旺", "宋清", "乐和", "龚旺", "丁得孙", "穆春", "曹正", "宋万", "杜迁", "薛永", "施恩",
			 };
		 JSONArray array=new JSONArray();
		for(int i=0;i<NAMES.length;i++){
			JSONObject object = new JSONObject();
			try {
				object.put("name", NAMES[i]);
				array.put(object);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		System.out.println(array.toString());
		JsonUtils.parseJSONWithJSONObject(array.toString());
		refreshImageView.clearAnimation();
		refreshImageView.setVisibility(View.INVISIBLE);
		initViews();
		
		
		
	}
	/*private void sendRequest() {
		String url = GlobalConstants.Agency_URL;
		StringRequest stringRequest = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					// 成功响应
					public void onResponse(String response) {

						JsonUtils.parseJSONWithJSONObject(response);
						refreshImageView.clearAnimation();
						refreshImageView.setVisibility(View.INVISIBLE);
						initViews();

					}
				}, new Response.ErrorListener() {
					@Override
					// 响应失败
					public void onErrorResponse(VolleyError error) {

						Toast.makeText(MainActivity.this, "获取数据失败", 1).show();
						refreshImageView.setVisibility(View.VISIBLE);
						refreshImageView.clearAnimation();
					}
				});
		mQueue.add(stringRequest);
		
	}*/

	/**
	 * 初始化视图
	 */
	private void initViews() {
		// 实例化汉字转拼音类
		characterParser = CharacterParser.getInstance();
		pinyinComparator = new PinyinComparator();
		sideBar = (SideBar) findViewById(R.id.sidrbar);
		dialog = (TextView) findViewById(R.id.dialog);
		sideBar.setTextView(dialog);

		// 设置右侧触摸监听
		sideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				// 该字母首次出现的位置
				int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					sortListView.setSelection(position);
				}

			}
		});

		sortListView = (ListView) findViewById(R.id.country_lvcountry);
		// 设置点击事件
		sortListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 这里要利用adapter.getItem(position)来获取当前position所对应的对象
				String name = ((SortModel) adapter.getItem(position)).getName();
				// 将名字存在TempData类中
				TempData.name = name;
				Intent intent = new Intent(MainActivity.this,
						CaptureActivity.class);
				startActivity(intent);
				finish();
			}
		});
		// 得到JsonUtils里的经销商列表数据转换为数组
		String[] arr = JsonUtils.list
				.toArray(new String[JsonUtils.list.size()]);
		// 数组作为数据源
		SourceDateList = filledData(arr);

		// 根据a-z进行排序源数据
		Collections.sort(SourceDateList, pinyinComparator);
		adapter = new SortAdapter(this, SourceDateList);
		sortListView.setAdapter(adapter);

		mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

		// 根据输入框输入值的改变来过滤搜索
		mClearEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
				filterData(s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}

	/**
	 * 为ListView填充数据
	 * 
	 * @param date
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	private List<SortModel> filledData(String[] date) {
		List<SortModel> mSortList = new ArrayList<SortModel>();

		for (int i = 0; i < date.length; i++) {
			SortModel sortModel = new SortModel();
			sortModel.setName(date[i]);
			// 汉字转换成拼音
			String pinyin = characterParser.getSelling(date[i]);
			String sortString = pinyin.substring(0, 1).toUpperCase();
			// 正则表达式，判断首字母是否是英文字母
			if (sortString.matches("[A-Z]")) {
				sortModel.setSortLetters(sortString.toUpperCase());
			} else {
				sortModel.setSortLetters("#");
			}

			mSortList.add(sortModel);
		}
		return mSortList;

	}

	/**
	 * 根据输入框中的值来过滤数据并更新ListView
	 * 
	 * @param filterStr
	 */
	private void filterData(String filterStr) {
		List<SortModel> filterDateList = new ArrayList<SortModel>();

		if (TextUtils.isEmpty(filterStr)) {
			filterDateList = SourceDateList;
		} else {
			filterDateList.clear();
			for (SortModel sortModel : SourceDateList) {
				String name = sortModel.getName();
				if (name.indexOf(filterStr.toString()) != -1
						|| characterParser.getSelling(name).startsWith(
								filterStr.toString())) {
					filterDateList.add(sortModel);
				}
			}
		}
		// 根据a-z进行排序
		Collections.sort(filterDateList, pinyinComparator);
		adapter.updateListView(filterDateList);
	}

}
