package com.example.tianwangxing;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.example.tianwangxing.bean.BuyerNum;
import com.example.tianwangxing.service.KeepAliveService;
import com.example.tianwangxing.util.CommonUtils;
import com.example.tianwangxing.util.HttpClient;
import com.example.tianwangxing.util.IpUtil;
import com.example.tianwangxing.util.NotificationSetUtil;
import com.example.tianwangxing.util.UpdateApk;
import com.example.tianwangxing.util.WindowPermissionCheck;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;


/**
 * 佣金支持卡小数点
 * 停止接单取消所有网络请求
 * 远程公告、频率等
 * try catch
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText etUname,etPaw,etYj1;
    private TextView tvStart,tvStop,tvLog,tvAppDown,tvAppOpen,tvTitle;
    private Handler mHandler;
    private String tbId;
    private String address;
    private String taskId;
    private String jingDu;
    private String weiDu;
    /*
    接单成功音乐提示播放次数（3次）
    播放的次数是count+1次
     */
    private int count;
    private SharedPreferences userInfo;
    private int minPl;
    private double minYj;
    private AlertDialog dialog;
    private String authorization;
    private List<BuyerNum> buyerNumList;
    private AlertDialog alertDialog2;
    private String[] tbNameArr;
    private String decId;

    private static  String LOGIN_URL = "";
    private static  String DOWNLOAD = "";
    private static  String VERSION = "";
    private static  String APP = "2";  //这里不变，变version  测试git


    /**
     * 需要更改的地方：
     * 1、MainActivity
     * 2、build.gradle配置文件
     * 3、AndroidMainfest.xml文件
     * 4、Update文件
     * 5、KeepAlive文件
     */

    private static final String LOGIN = "/member/login";
    private static final String GET_TB_INFO = "/useraccount/itemlist";
    private static final String GET_ADDRESS = "/identify/realname_result";
    private static final String CHECK_TB_INFO = "/order/checkoutAccount?account_id=";
    private static final String GET_AUTH = "/order/qiangdan";
    private static final String GET_TASK = "/order/listen_task?task_id=";
    private static final String STOP_TASK = "/order/stop?task_id=";
    private static final String QUIT_TASK = "/homework/abandon";



//    private static final String PT_NAME = "tianWangXing";
//    private static final String TITLE = "天王星助手";
//    private static final String SUCCESS_TI_SHI = "天王星接单成功";
//    private static final String TI_SHI = "天王星App未安装";
//    private static final String CHANNELID = "tianwangxingSuccess";
//    private static final String APK_PACKAGE = "com.zzhshop.tianwangxing";
//    private static final String DEVICE = "device-id";
//    private static int ICON = R.mipmap.tianwangxing;
//    private static final int JIE_DAN_SUCCESS = R.raw.twx_success;
//    private static final int JIE_DAN_FAIL = R.raw.twx_fail;


    private static final String PT_NAME = "tianLangXing";
    private static final String TITLE = "天狼星助手";
    private static final String SUCCESS_TI_SHI = "天狼星接单成功";
    private static final String TI_SHI = "天狼星App未安装";
    private static final String CHANNELID = "tianlangxingSuccess";
    private static final String APK_PACKAGE = "com.zzhshop.tianlangxing";
    private static final String DEVICE = "device_id";
    private static int ICON = R.mipmap.tianlangxing;
    private static final int JIE_DAN_SUCCESS = R.raw.tlx_success;
    private static final int JIE_DAN_FAIL = R.raw.tlx_fail;

//    private static final String PT_NAME = "tianMaXingKong";
//    private static final String TITLE = "天马星空助手";
//    private static final String SUCCESS_TI_SHI = "天马星空接单成功";
//    private static final String TI_SHI = "天马星空App未安装";
//    private static final String CHANNELID = "tianmaxingkongSuccess";
//    private static final String APK_PACKAGE = "com.zzhshop.tianmaxingkong";
//    private static final String DEVICE = "device_id";
//    private static int ICON = R.mipmap.tianmaxingkong;
//    private static final int JIE_DAN_SUCCESS = R.raw.tmxk_success;
//    private static final int JIE_DAN_FAIL = R.raw.tmxk_fail;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去掉标题栏
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);//启动保活服务
        ignoreBatteryOptimization();//忽略电池优化

        if(!checkFloatPermission(this)){
            //权限请求方法
            requestSettingCanDrawOverlays();
        }
        initView();
    }


    private void initView(){
        //检查更新
        UpdateApk.update(MainActivity.this);
        //是否开启通知权限
        openNotification();
        //是否开启悬浮窗权限
        WindowPermissionCheck.checkPermission(this);
        //获取平台地址
        getPtAddress();
        mHandler = new Handler();
        etYj1 = findViewById(R.id.et_yj1);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(TITLE);
        tvAppDown = findViewById(R.id.tv_appDown);
        tvAppOpen = findViewById(R.id.tv_appOpen);
        etUname = findViewById(R.id.et_username);
        etPaw = findViewById(R.id.et_password);
        tvStart = findViewById(R.id.tv_start);
        tvStop = findViewById(R.id.tv_stop);
        tvLog = findViewById(R.id.tv_log);
        getUserInfo();//读取用户信息
        //设置textView为可滚动方式
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);

        tvLog.setText("需要其他平台请联系微信：lzm9465"+"\n");
        tvLog.append("----------------------------------"+"\n");
        tvLog.append("不想接单时，一定要在平台点击停止接单~"+"\n");

        buyerNumList = new ArrayList<>();
        decId = String.valueOf((int)(Math.random()*9+1)*100000);
    }


    /**
     * 弹窗公告
     */
    public void announcementDialog(String[] lesson){

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("公告")
                .setCancelable(false) //触摸窗口边界以外是否关闭窗口，设置 false
                .setPositiveButton("我知道了", null)
                //.setMessage("")
                .setItems(lesson,null)
                .create();
        dialog.show();
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_start:

                if("".equals(etYj1.getText().toString().trim())){
                    etYj1.setText("1");
                }
                if(Double.parseDouble(etYj1.getText().toString().trim()) > 4){
                    etYj1.setText("4");
                }

                minYj = Double.parseDouble(etYj1.getText().toString().trim());
                //10-200  保留5位
                jingDu = new BigDecimal(10+Math.random()*(200-10)).setScale(5,BigDecimal.ROUND_HALF_UP).toString();
                weiDu = new BigDecimal(10+Math.random()*(200-10)).setScale(5,BigDecimal.ROUND_HALF_UP).toString();

                /*
                先清除掉之前的Handler中的Runnable，不然会和之前的任务一起执行多个
                 */
                mHandler.removeCallbacksAndMessages(null);

                if(LOGIN_URL == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim());
                }
                break;
            case R.id.tv_stop:
                stop();
                break;
            case R.id.tv_appDown:
                if(DOWNLOAD == ""){
                    tvLog.setText("获取最新网址中,请3秒后重试...");
                }else {
                    Uri uri = Uri.parse(DOWNLOAD);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                break;
            case R.id.tv_appOpen:
                openApp(APK_PACKAGE);
                break;
        }

    }


    private void openApp(String packName){
        PackageManager packageManager = this.getPackageManager();
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packName);
        List<ResolveInfo> apps = packageManager.queryIntentActivities(resolveIntent, 0);
        if (apps.size() == 0) {
            Toast.makeText(this, TI_SHI, Toast.LENGTH_LONG).show();
            return;
        }
        ResolveInfo resolveInfo = apps.iterator().next();
        if (resolveInfo != null) {
            String className = resolveInfo.activityInfo.name;
            Intent intent2 = new Intent(Intent.ACTION_MAIN);
            intent2.addCategory(Intent.CATEGORY_LAUNCHER);
            intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName cn = new ComponentName(packName, className);
            intent2.setComponent(cn);
            this.startActivity(intent2);
        }
    }



    /**
     * 重写activity的onKeyDown方法，点击返回键后不销毁activity
     * 可参考：https://blog.csdn.net/qq_36713816/article/details/71511860
     * 另外一种解决办法：重写onBackPressed方法，里面不加任务内容，屏蔽返回按钮
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    public void getPtAddress(){

        HttpClient.getInstance().get("/ptVersion/checkUpdate","http://47.94.255.103")
                .params("ptName",PT_NAME)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject ptAddrObj = JSONObject.parseObject(response.body());
                            if(ptAddrObj == null){
                                Toast.makeText(MainActivity.this, "没有配置此平台更新信息！", Toast.LENGTH_LONG).show();
                                return;
                            }
                            LOGIN_URL = ptAddrObj.getString("ptUrl");
                            DOWNLOAD = ptAddrObj.getString("apkDownload");
                            VERSION = ptAddrObj.getString("apkVersion");
                            minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));
                            //公告弹窗
                            String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                            announcementDialog(gongGao);
                        }catch (Exception e){
                            sendLog("获取网址："+e.getMessage());
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("服务器出现问题啦~");
                    }
                });
    }



    /**
     * 登录
     * @param username
     * @param password
     * @param
     */
    private void userLogin(String username, String password){
        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": 正在登陆中..."+"\n");
        HttpClient.getInstance().post(LOGIN, LOGIN_URL)
                .params("username",username)
                .params("password",password)
                .params("device_id",decId)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers("Content-Type","application/json")
                .headers("version",VERSION)
                .headers("app",APP)
                .headers(DEVICE,decId)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject loginJsonObj = JSONObject.parseObject(response.body());
                            //登陆成功
                            if("200".equals(loginJsonObj.getString("code"))){
                                //  serialId = serialNum;
                                saveUserInfo(username,password, etYj1.getText().toString().trim());
                                authorization = loginJsonObj.getJSONObject("data").getJSONObject("token").getString("access_token");
                                sendLog(loginJsonObj.getString("msg"));
                                getTbInfo();
                            }else {
                                sendLog(loginJsonObj.getString("msg"));
                            }
                        }catch (Exception e){
                            sendLog("登录："+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("登录ERR："+response.getException().toString());
                    }
                });
    }


    /**
     * 获取淘宝号信息
     */
    private void getTbInfo() {
        HttpClient.getInstance().get(GET_TB_INFO, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers(DEVICE,decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject tbObj = JSONObject.parseObject(response.body());
                            if("200".equals(tbObj.getString("code"))){
                                //获取淘宝号list列表
                                JSONArray tbArr = tbObj.getJSONArray("data");
                                buyerNumList.clear();
                                for (int i = 0; i < tbArr.size(); i++) {
                                    JSONObject tbInfo = tbArr.getJSONObject(i);
                                    //1  淘宝，2 拼多多
                                    if("1".equals(tbInfo.getString("type"))){
                                        String tbId = tbInfo.getString("id");
                                        String tbName = tbInfo.getString("account");
                                        //String rYJ = tbInfo.getString("df_total");
                                        Integer rKJ = tbInfo.getInteger("surplus_df");
                                        if(rKJ == 0){
                                            sendLog("【"+tbName+"】日已接满,已自动过滤~");
                                            continue;
                                        }
                                        buyerNumList.add(new BuyerNum(tbId,tbName));
                                    }
                                }
                                tbNameArr = new String[buyerNumList.size()];
                                sendLog("获取到"+buyerNumList.size()+"个可用淘宝号");

                                for (int i = 0; i < buyerNumList.size(); i++) {
                                    tbNameArr[i] = buyerNumList.get(i).getName();
                                }
                                showSingleAlertDialog();
                            }else {
                                sendLog(tbObj.getString("msg"));
                            }
                        }catch (Exception e){
                            sendLog(e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("获取淘宝号："+response.getException());
                    }
                });
    }


    /**
     * 展示淘宝号
     */
    public void showSingleAlertDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("请选择接单淘宝号");
        alertBuilder.setCancelable(false); //触摸窗口边界以外是否关闭窗口，设置 false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                //根据选择的淘宝名获取淘宝id
                List<BuyerNum> buyerNum = buyerNumList.stream().
                        filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                tbId = buyerNum.get(0).getId();
            }
        });
        alertBuilder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO 业务逻辑代码
                if(tbId == null){
                    sendLog("未选择接单淘宝号");
                    return;
                }
                List<BuyerNum> buyerNum = buyerNumList.stream().
                        filter(p -> p.getId().equals(tbId)).collect(Collectors.toList());
                sendLog("将使用 "+buyerNum.get(0).getName()+" 进行接单");
                //获取地址信息
                getAddress();
                // 关闭提示框
                alertDialog2.dismiss();
            }
        });
        alertDialog2 = alertBuilder.create();
        alertDialog2.show();
    }



    private void getAddress(){
        HttpClient.getInstance().get(GET_ADDRESS, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers("DEVICE",decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .execute(new StringCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        address = obj.getJSONObject("data").getString("address");
                        checkTbInfo();
                    }
                });
    }



    /**
     * 检查淘宝号状态是否已验号
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkTbInfo(){
        HttpClient.getInstance().get(CHECK_TB_INFO+tbId, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers("DEVICE",decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if("200".equals(obj.getString("code"))){  //已验号
                            getTaskParam();
                        }else {
                            sendLog(obj.getString("msg"));
                        }
                    }
                });
    }




    /**
     * 根据淘宝号获取抢单参数
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTaskParam() {

        HttpClient.getInstance().post(GET_AUTH, LOGIN_URL)
                .params("account_id",tbId)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers("DEVICE",decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .headers("Content-Type","application/json;charset=UTF-8")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if("200".equals(obj.getString("code"))){
                                taskId = obj.getJSONObject("data").getString("task_id");
                                getTask();
                            }else {
                                //如果有未完成的订单，这里会提示
                                sendLog(obj.getString("msg"));
                                playMusic(JIE_DAN_FAIL,3000,0);
                                //{"code":400,"msg":"账号被处罚冻结","time":1644573968,"data":[]}
                                //{"code":400,"msg":"过于频繁提交请稍后重试","time":1640954628,"data":[]}
                            }
                        }catch (Exception e){
                            sendLog("getTaskParam："+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("getTaskParam："+response.getException());
                    }
                });
    }


    /**
     * 抢单
     */
    private void getTask() {
        HttpClient.getInstance().get(GET_TASK+taskId+"&lat="+jingDu+"&lng="+weiDu+"&address="+address, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers("DEVICE",decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .headers("Content-Type","application/json;charset=UTF-8")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if("200".equals(obj.getString("code"))){
                                String wage = obj.getJSONObject("data").getString("wage");
                                String taskId = obj.getJSONObject("data").getString("id");  //放弃任务时用的
                                //String tbName = obj.getJSONObject("data").getString("u_account");
                                if (minYj > Double.parseDouble(wage)){
                                    sendLog("佣金："+wage+"不满足设置要求,已自动放弃~");
                                    //放弃成功后需要重新获取参数
                                    cancelTask(taskId);
                                    return;
                                }
                                sendLog("恭喜您,接单成功!  佣金："+wage);
                                receiveSuccess(wage);
                                sendLog("-------------------------------");
                                sendLog("商品图（复制网址到浏览器打开）："+obj.getJSONObject("data").getString("img"));
                                playMusic(JIE_DAN_SUCCESS,3000,2);
                            }else {
                                //一定要点击停止接单
                                //{"code":400,"msg":"过于频繁提交请稍后重试","time":1632585376,"data":[]}
                                //{"code":400,"msg":"您有未做单的订单","time":1632402201,"data":[]}
                                //{"code":400,"msg":"请开启GPS定位服务","time":1632402563,"data":[]}
                                //{"code":400,"msg":"有打标任务需提交,请完成提交再接单","time":1632489187,"data":[]}
                                sendLog(obj.getString("msg"));
                                jieDan();
                            }
                        }catch (Exception e){
                            sendLog("接取任务:"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("接取任务ERR:"+response.getException());
                        jieDan();
                    }
                });
    }

    private void jieDan(){
        //单个淘宝号接单，也会存在您有未做单的订单不提醒
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getTask();
            }
        }, minPl);
    }



    /**
     * 取消任务
     *
     * 放弃任务之后需要重新获取抢单参数，不然报抢单异常,请重新发起
     * @param taskId
     */
    private void cancelTask(String taskId){

        HttpClient.getInstance().post(QUIT_TASK, LOGIN_URL)
                .params("id",taskId)
                .params("reason","找不到商品")
                .headers("app",APP)
                .headers("authorization",authorization)
                .headers("version",VERSION)
                .headers("DEVICE",decId)
                .headers("Content-Type","application/json;charset=UTF-8")
                .headers("user-agent","Dart/2.13 (dart:io)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            if("200".equals(obj.getString("code"))){
                                mHandler.postDelayed(new Runnable() {
                                    @RequiresApi(api = Build.VERSION_CODES.N)
                                    @Override
                                    public void run() {
                                        getTaskParam();
                                    }
                                }, 4000);
                            }else {
                                sendLog("放弃任务失败："+obj.getString("msg"));
                            }
                        }catch (Exception e){
                            sendLog("取消任务"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog("取消任务ERR："+response.getException());
                    }
                });
    }



    /**
     * 停止接单
     */
    private void stopQiangDan(){
        HttpClient.getInstance().get(STOP_TASK+taskId, LOGIN_URL)
                .headers("app",APP)
                .headers("authorization",authorization)
                .headers("version",VERSION)
                .headers("DEVICE",decId)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            sendLog(obj.getString("msg"));
                            //{"code":200,"msg":"停止接单","time":1662438811,"data":[]}
                            OkGo.getInstance().cancelAll();
                        }catch (Exception e){

                        }
                    }
                });
    }



    /**
     * 停止接单
     */
    public void stop(){
        if(authorization != null)
            stopQiangDan();
        //Handler中已经提供了一个removeCallbacksAndMessages去清除Message和Runnable
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * 接单成功后通知铃声
     * @param voiceResId 音频文件
     * @param milliseconds 需要震动的毫秒数
     */
    private void playMusic(int voiceResId, long milliseconds,int total){

        count = total;//不然会循环播放

        //播放语音
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //播放完成事件
                if(count != 0){
                    player.start();
                }
                count --;
            }
        });

        //震动
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //延迟的毫秒数
        vib.vibrate(milliseconds);
    }

    /**
     * 日志更新
     * @param log
     */
    public void sendLog(String log){
        scrollToTvLog();
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
        //如果日志大于100条，则清空
//        if(tvLog.getLineCount() > 100){
//            tvLog.setText("");
//        }
    }


    /**
     * 忽略电池优化
     */

    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  判断当前APP是否有加入电池优化的白名单，如果没有，弹出加入电池优化的白名单的设置对话框。
            if(!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent);
            }
        }


    }


    /**
     * 保存用户信息
     */
    private void saveUserInfo(String username,String password, String yj1){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//获取Editor
        //得到Editor后，写入需要保存的数据
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("yj1", yj1);
        editor.commit();//提交修改

    }

    /**
     * 读取用户信息
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//读取username
        String passwrod = userInfo.getString("password", null);//读取password
        String yj1 = userInfo.getString("yj1",null);
        if(username!=null && passwrod!=null){
            etUname.setText(username);
            etPaw.setText(passwrod);
            etYj1.setText(yj1);
        }
    }


    public void scrollToTvLog(){
        int tvHeight = tvLog.getHeight();
        int tvHeight2 = getTextViewHeight(tvLog);
        if(tvHeight2>tvHeight){
            tvLog.scrollTo(0,tvHeight2-tvLog.getHeight());
        }
    }

    private int getTextViewHeight(TextView textView) {
        Layout layout = textView.getLayout();
        int desired = layout.getLineTop(textView.getLineCount());
        int padding = textView.getCompoundPaddingTop() +
                textView.getCompoundPaddingBottom();
        return desired + padding;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    //判断是否开启悬浮窗权限   context可以用你的Activity.或者tiis
    public static boolean checkFloatPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
                return mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    //权限打开
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0以上
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0以下
            //无需处理了
        }
    }

    private void openNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //判断是否需要开启通知栏功能
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }


    /**
     * 接单成功执行逻辑
     */
    @SuppressLint("WrongConstant")
    protected void receiveSuccess(String dianPuName){
        //前台通知的id名，任意
        String channelId = CHANNELID;
        //前台通知的名称，任意
        String channelName = "接单成功状态栏通知";
        //发送通知的等级，此处为高，根据业务情况而定
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. 获取系统的通知管理器
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. 创建NotificationChannel(这里传入的channelId要和创建的通知channelId一致，才能为指定通知建立通知渠道)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        //点击通知时可进入的Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. 创建一个通知(必须设置channelId)
        Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("佣金:"+dianPuName)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//点击通知进入Activity
                .setPriority(NotificationCompat.PRIORITY_HIGH) //设置通知的优先级为最大!!!!!!!!!!
                .setCategory(Notification.CATEGORY_TRANSPORT) //设置通知类别
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //控制锁定屏幕中通知的可见详情级别
                .build();

        // 4. 发送通知
        notificationManager.notify(2, notification);
    }



    public void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //移除标记为id的通知 (只是针对当前Context下的所有Notification)
        notificationManager.cancel(2);
        //移除所有通知
        //notificationManager.cancelAll();

    }

}


