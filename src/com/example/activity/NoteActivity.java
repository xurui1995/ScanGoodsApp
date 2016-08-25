package com.example.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.Data.GlobalConstants;
import com.example.Data.TempData;
import com.example.myscanapp.R;
import com.example.utils.AutoCloseDialog;
import com.example.utils.JsonUtils;
import com.example.utils.OneClickUtils;

/**
 * 清单界面
 * 
 * @author dell
 * 
 */
public class NoteActivity extends Activity {
	private TextView noteText;
	private ExpandableListView expandableListView;
	private ImageView submit;
	private ImageView exit;
	private Intent intent;
	private MyAdapter expAdapter;
	private View mychildView;
	private Button add;
	private boolean flag;
	private TextView totalText;
	private RequestQueue mRequestQueue;
	private AlertDialog.Builder mbuilder;

	/**
	 * 判断网络是否连接
	 * 
	 */
	public boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.delivery_note);

		initView();
		initlistview();

	}

	/**
	 * ListView初始化
	 */
	public void initlistview() {
		expandableListView = (ExpandableListView) findViewById(R.id.expandable_Listview);
		expAdapter = new MyAdapter();
		expandableListView.setAdapter(new MyAdapter());
		expandableListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View childView, int flatPos, long id) {
						mychildView = childView;
						mbuilder = new AlertDialog.Builder(
								NoteActivity.this);
						mbuilder.setTitle("警告");
						// 长按child事件
						if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
							mychildView.setBackgroundColor(Color.RED);
							long packedPos = ((ExpandableListView) parent)
									.getExpandableListPosition(flatPos);
							final int groupPosition = ExpandableListView
									.getPackedPositionGroup(packedPos);
							final int childPosition = ExpandableListView
									.getPackedPositionChild(packedPos);
							final String s = (String) expAdapter.getChild(
									groupPosition, childPosition);
							mbuilder.setMessage("是否删除sn号为" + s + "的机器");
							mbuilder.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											TempData.deleteBySn(s,
													groupPosition,
													childPosition);
											intent = new Intent(
													NoteActivity.this,
													NoteActivity.class);
											startActivity(intent);
											finish();
										}
									});
							mbuilder.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mychildView.setBackgroundColor(Color
													.parseColor("#C0C0C0"));
											dialog.dismiss();
										}
									});
						}
						// 长按Group事件
						else {
							int groupPosition = ExpandableListView
									.getPackedPositionGroup(id);
							
							final String s = (String) expAdapter
									.getGroup(groupPosition);
							mbuilder.setMessage("是否删除机型为" + s + "的机器");
							mbuilder.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											TempData.deleteByType(s);
											intent = new Intent(
													NoteActivity.this,
													NoteActivity.class);
											startActivity(intent);
											finish();
										}
									});
							mbuilder.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									});
						}
						AlertDialog adialog = mbuilder.create();
						adialog.setCanceledOnTouchOutside(false);
						adialog.show();
						return true;
					}

				});
	}

	/**
	 * 返回按键监听
	 */
	@Override
	public void onBackPressed() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog dialog = builder.create();
		View view = View.inflate(this, R.layout.exit_dialog, null);
		dialog.setView(view, 0, 0, 0, 0);
		Button btnOK = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				TempData.clean();

				Log.d("tsg", JsonUtils.list.toString() + "********");
				Intent intent = new Intent(NoteActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();

			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.dismiss();
			}
		});
		dialog.show();

	};

	/**
	 * 一些控件的初始化
	 */
	private void initView() {
		mRequestQueue = Volley.newRequestQueue(getApplicationContext());
		Intent intent = getIntent();
		String sn = intent.getStringExtra("sn");
		if (sn == null) {
			sn = "无";
		}
		noteText = (TextView) findViewById(R.id.note_Text);
		noteText.setText("经销商名称：   " + TempData.name + "\n" + "本次扫描结果: " + sn);
		totalText = (TextView) findViewById(R.id.total_Text);
		totalText.setText("总计：    " + TempData.count + " 台");
		add = (Button) findViewById(R.id.add);
		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(NoteActivity.this,
						CaptureActivity.class);
				startActivity(intent);
				finish();
			}
		});

		exit = (ImageView) findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		submit = (ImageView) findViewById(R.id.submit);
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 防止多次连续点击
				if (OneClickUtils.isFastClick())
					return;
				if (!isNetworkConnected(getApplicationContext())) {
					 mbuilder = new AlertDialog.Builder(
							NoteActivity.this);
					mbuilder.setTitle("提示");
					mbuilder.setMessage("请检查网络");
					AlertDialog dialog = mbuilder.create();
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
				} else if (TempData.count == 0) {

					mbuilder = new AlertDialog.Builder(
							NoteActivity.this);
					mbuilder.setTitle("提示");
					mbuilder.setMessage("录入商品数量为0");
					AlertDialog dialog = mbuilder.create();
					dialog.setCanceledOnTouchOutside(true);
					dialog.show();
				} else {
					if (!flag) {
						postMyData();

					}
				}
			}

		});
	}

	/**
	 * 模拟提交数据(真实提交请取消注释代码)
	 * 
	 * 
	 */
	public void postMyData(){
		flag = true;
		TempData.clean();
		mbuilder = new AlertDialog.Builder(
				NoteActivity.this);
		mbuilder.setTitle("提示");
		mbuilder.setCancelable(false);
		mbuilder.setMessage("数据提交成功");
		AlertDialog dialog = mbuilder.create();
		AutoCloseDialog d = new AutoCloseDialog(
				NoteActivity.this, dialog);
		// 显示2秒，自动跳转
		d.show(2000);
		
	}
	/*public void postMyData() {
		flag = true;

		JSONObject data = JsonUtils.buildJSONObject(TempData.name,
				TempData.parent, TempData.map);

		System.out.println("+++" + data);
		String url = GlobalConstants.PostData_URL;
		JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
				url, data, new Listener<JSONObject>() {

					@Override
					// 响应成功
					public void onResponse(JSONObject response) {

						System.out.println("success");
						TempData.clean();
						mbuilder = new AlertDialog.Builder(
								NoteActivity.this);
						mbuilder.setTitle("提示");
						mbuilder.setCancelable(false);
						mbuilder.setMessage("数据提交成功");
						AlertDialog dialog = mbuilder.create();
						AutoCloseDialog d = new AutoCloseDialog(
								NoteActivity.this, dialog);
						// 显示2秒，自动跳转
						d.show(2000);

					}
				}, new Response.ErrorListener() {

					@Override
					// 响应失败
					public void onErrorResponse(VolleyError error) {
						System.out.println(error.toString());
						System.out.println("error");
						mbuilder = new AlertDialog.Builder(
								NoteActivity.this);
						mbuilder.setTitle("提示");
						mbuilder.setMessage("提交失败，请联系技术人员");
						AlertDialog dialog = mbuilder.create();
						dialog.show();
						flag = false;
					}

				});
		// 设置连接超时时间
		request.setRetryPolicy(new DefaultRetryPolicy(3 * 1000, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		mRequestQueue.add(request);

	}*/

	/**
	 * ExpandableListView的自定义适配器
	 * 
	 * @author xurui
	 * 
	 */
	class MyAdapter extends BaseExpandableListAdapter {

		@Override
		public int getGroupCount() {

			return TempData.parent.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {

			String key = TempData.parent.get(groupPosition);

			return TempData.map.get(key).size();
		}

		@Override
		public Object getGroup(int groupPosition) {

			return TempData.parent.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {

			String key = TempData.parent.get(groupPosition);
			return TempData.map.get(key).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {

			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {

			return childPosition;
		}

		@Override
		public boolean hasStableIds() {

			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) NoteActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.layout_parent, null);

			}
			TextView tv = (TextView) convertView
					.findViewById(R.id.parent_textview);
			tv.setText(TempData.parent.get(groupPosition) + "    "
					+ getChildrenCount(groupPosition) + "台");
			return tv;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			String key = TempData.parent.get(groupPosition);
			String info = TempData.map.get(key).get(childPosition);
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) NoteActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.layout_children, null);

			}
			TextView tv = (TextView) convertView
					.findViewById(R.id.second_textview);
			tv.setText(info);
			return tv;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}
