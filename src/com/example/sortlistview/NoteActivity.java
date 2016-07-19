package com.example.sortlistview;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Data.TempData;
import com.zxing.activity.CaptureActivity;

public class NoteActivity extends Activity {
	private TextView noteText;
	private ExpandableListView expandableListView;
	private ImageView add;
	private ImageView exit;
	private Intent intent;
	private MyAdapter expAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.delivery_note);
		Toast.makeText(this, this.toString(), 1);
		noteText = (TextView) findViewById(R.id.note_Text);
		initView();
		initlistview();
	}

	public void initlistview() {
		expandableListView = (ExpandableListView) findViewById(R.id.expandable_Listview);
		expAdapter = new MyAdapter();
		expandableListView.setAdapter(new MyAdapter());
		expandableListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View childView, int flatPos, long id) {
						AlertDialog.Builder builder = new AlertDialog.Builder(NoteActivity.this);
						builder.setTitle("警告");						
						if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
							long packedPos = ((ExpandableListView) parent)
									.getExpandableListPosition(flatPos);
							final int groupPosition = ExpandableListView
									.getPackedPositionGroup(packedPos);
							final int childPosition = ExpandableListView
									.getPackedPositionChild(packedPos);
							final String s = (String) expAdapter.getChild(
									groupPosition, childPosition);
							builder.setMessage("是否删除sn号为"+s+"的机器");
							 builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									TempData.deleteBySn(s, groupPosition, childPosition);
									intent = new Intent(NoteActivity.this,
											NoteActivity.class);
									startActivity(intent);
									finish();
								}
							});
							 builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
						}

						else {
							int groupPosition = ExpandableListView
									.getPackedPositionGroup(id);
							final String s = (String) expAdapter
									.getGroup(groupPosition);
							builder.setMessage("是否删除机型为"+s+"的机器");
							 builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									TempData.deleteByType(s);
									intent = new Intent(NoteActivity.this,
											NoteActivity.class);
									startActivity(intent);
									finish();
								}
							});
							 builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
										dialog.dismiss();
									}
								});
						}	
						AlertDialog adialog=builder.create();
						adialog.setCanceledOnTouchOutside(false);
						adialog.show();
						return true;
					}

				});
	}
	
	  

	@Override
	public void onBackPressed() {
		Dialog alertDialog = new AlertDialog.Builder(this).setTitle("警告")
				.setMessage("记录尚未结交，是否放弃本次记录")
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}

				})
				.setNegativeButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						TempData.clean();
						Intent intent = new Intent(NoteActivity.this,
								MainActivity.class);
						startActivity(intent);
						finish();
					}
				}).create();
		alertDialog.show();
	};

	private void initView() {
		Intent intent = getIntent();
		String sn = intent.getStringExtra("sn");
		if (sn == null) {
			sn = "无";
		}
		noteText.setText("经销商名称：   " + TempData.name + "\n" + "本次扫描结果: " + sn
				+ "\n" + "总计：    " + TempData.count + " 台");
		add = (ImageView) findViewById(R.id.add);
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
	}

	

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
					+ getChildrenCount(groupPosition));
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
