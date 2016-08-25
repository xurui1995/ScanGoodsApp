package com.example.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.activity.MainActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
/**
 * 这是一个dialog的装饰类
 * 起延时作用
 * @author xurui
 *
 */
public class AutoCloseDialog{  
      
    private AlertDialog dialog;  
    private Context mcontext;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();  
      
    public AutoCloseDialog(Context context,AlertDialog dialog){  
        this.dialog = dialog; 
        mcontext=context;
    }  
      
    public void show(long duration){  
        //创建自动关闭任务  
        Runnable runner = new Runnable() {  
            @Override  
            public void run() {  
                dialog.dismiss();  
                Intent intent = new Intent(mcontext,
						MainActivity.class);
                mcontext.startActivity(intent);
                Activity activity = (Activity) mcontext;
                activity.finish();
                
            }  
        };  
        //新建调度任务  
        executor.schedule(runner, duration, TimeUnit.MILLISECONDS);  
        dialog.show();  
    }  
      
}  