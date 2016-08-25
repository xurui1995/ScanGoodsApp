package com.example.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TempData {
	// 扫描到的所有sn
	public static ArrayList<String> templist = new ArrayList<String>();
	// 存放配置文件code.txt中的合法前缀
	public static ArrayList<String> prelist = new ArrayList<String>();
	// 录入商品数量
	public static int count = 0;
	// 经销商名称
	public static String name;
	// 作为ExpandableListView中的Group
	public static List<String> parent = new ArrayList<String>();
	// 作为ExpandableListView中的Child
	public static Map<String, List<String>> map = new HashMap<String, List<String>>();
	
	// 将sn分类
	public static void sort(String sn) {
		String shortsn="";
		for(String pre : prelist){
			if(sn.startsWith(pre)){
				shortsn = pre;
				break;
			}
		}
		

		if (!parent.contains(shortsn)) {
			parent.add(shortsn);
		}

		if (!map.keySet().contains(shortsn)) {
			List<String> childlist = new ArrayList<String>();
			childlist.add(sn);
			map.put(shortsn, childlist);
		} else {
			map.get(shortsn).add(sn);
		}

	}

	public static void add(String resultString) {
		TempData.count++;
		TempData.templist.add(resultString);
		TempData.sort(resultString);
	}

	// 删除一台机器
	public static void deleteBySn(String s, int groupPosition, int childPosition) {
		templist.remove(s);
		count--;
		String key = parent.get(groupPosition);
		map.get(key).remove(childPosition);
		if (map.get(key).isEmpty()) {
			map.remove(key);
			parent.remove(key);
		}

	}

	// 删除同一种型号
	public static void deleteByType(String s) {
		Iterator<String> ittemp = templist.iterator();
		while (ittemp.hasNext()) {
			String e = ittemp.next();
			if (e.contains(s)) {
				ittemp.remove();
				count--;
			}
		}

		map.remove(s);
		parent.remove(s);
	}

	// 清空数据
	public static void clean() {
		TempData.count = 0;
		TempData.map.clear();
		TempData.parent.clear();
		TempData.templist.clear();
		TempData.name = null;
	}

}
