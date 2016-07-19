package com.example.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonUtils {

	public static List<String> list=new ArrayList<String>();
	public static void parseJSONWithJSONObject(String jsondata){
		try {
			list.clear();
			JSONArray jsonArray=new JSONArray(jsondata);
			for(int i=0;i<jsonArray.length();i++){
				JSONObject jsonObject=jsonArray.getJSONObject(i);
				String name=jsonObject.getString("name");
				list.add(name);
				//System.out.println(DataUtils.list);
			}
			
		} catch (Exception e) {
			
		}
		
	}
}
