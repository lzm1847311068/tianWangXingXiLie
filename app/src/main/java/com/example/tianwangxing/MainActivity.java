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
 * ????????????????????????
 * ????????????????????????????????????
 * ????????????????????????
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
    ???????????????????????????????????????3??????
    ??????????????????count+1???
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
    private static  String APP = "2";  //??????????????????version


    /**
     * ????????????????????????
     * 1???MainActivity
     * 2???build.gradle????????????
     * 3???AndroidMainfest.xml??????
     * 4???Update??????
     * 5???KeepAlive??????
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
//    private static final String TITLE = "???????????????";
//    private static final String SUCCESS_TI_SHI = "?????????????????????";
//    private static final String TI_SHI = "?????????App?????????";
//    private static final String CHANNELID = "tianwangxingSuccess";
//    private static final String APK_PACKAGE = "com.zzhshop.tianwangxing";
//    private static final String DEVICE = "device-id";
//    private static int ICON = R.mipmap.tianwangxing;
//    private static final int JIE_DAN_SUCCESS = R.raw.twx_success;
//    private static final int JIE_DAN_FAIL = R.raw.twx_fail;


    private static final String PT_NAME = "tianLangXing";
    private static final String TITLE = "???????????????";
    private static final String SUCCESS_TI_SHI = "?????????????????????";
    private static final String TI_SHI = "?????????App?????????";
    private static final String CHANNELID = "tianlangxingSuccess";
    private static final String APK_PACKAGE = "com.zzhshop.tianlangxing";
    private static final String DEVICE = "device_id";
    private static int ICON = R.mipmap.tianlangxing;
    private static final int JIE_DAN_SUCCESS = R.raw.tlx_success;
    private static final int JIE_DAN_FAIL = R.raw.tlx_fail;

//    private static final String PT_NAME = "tianMaXingKong";
//    private static final String TITLE = "??????????????????";
//    private static final String SUCCESS_TI_SHI = "????????????????????????";
//    private static final String TI_SHI = "????????????App?????????";
//    private static final String CHANNELID = "tianmaxingkongSuccess";
//    private static final String APK_PACKAGE = "com.zzhshop.tianmaxingkong";
//    private static final String DEVICE = "device_id";
//    private static int ICON = R.mipmap.tianmaxingkong;
//    private static final int JIE_DAN_SUCCESS = R.raw.tmxk_success;
//    private static final int JIE_DAN_FAIL = R.raw.tmxk_fail;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //???????????????
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, KeepAliveService.class);
        startService(intent);//??????????????????
        ignoreBatteryOptimization();//??????????????????

        if(!checkFloatPermission(this)){
            //??????????????????
            requestSettingCanDrawOverlays();
        }
        initView();
    }


    private void initView(){
        //????????????
        UpdateApk.update(MainActivity.this);
        //????????????????????????
        openNotification();
        //???????????????????????????
        WindowPermissionCheck.checkPermission(this);
        //??????????????????
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
        getUserInfo();//??????????????????
        //??????textView??????????????????
        tvLog.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvLog.setTextIsSelectable(true);
        tvStart.setOnClickListener(this);
        tvStop.setOnClickListener(this);
        tvAppOpen.setOnClickListener(this);
        tvAppDown.setOnClickListener(this);

        tvLog.setText("????????????????????????????????????lzm9465"+"\n");
        tvLog.append("----------------------------------"+"\n");
        tvLog.append("??????????????????????????????????????????????????????~"+"\n");

        buyerNumList = new ArrayList<>();
        decId = String.valueOf((int)(Math.random()*9+1)*100000);
    }


    /**
     * ????????????
     */
    public void announcementDialog(String[] lesson){

        dialog = new AlertDialog
                .Builder(this)
                .setTitle("??????")
                .setCancelable(false) //??????????????????????????????????????????????????? false
                .setPositiveButton("????????????", null)
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
                //10-200  ??????5???
                jingDu = new BigDecimal(10+Math.random()*(200-10)).setScale(5,BigDecimal.ROUND_HALF_UP).toString();
                weiDu = new BigDecimal(10+Math.random()*(200-10)).setScale(5,BigDecimal.ROUND_HALF_UP).toString();
                /*
                ?????????????????????Handler??????Runnable????????????????????????????????????????????????
                 */
                mHandler.removeCallbacksAndMessages(null);

                if(LOGIN_URL == ""){
                    tvLog.setText("?????????????????????,???3????????????...");
                }else {
                    userLogin(etUname.getText().toString().trim(),etPaw.getText().toString().trim());
                }
                break;
            case R.id.tv_stop:
                stop();
                break;
            case R.id.tv_appDown:
                if(DOWNLOAD == ""){
                    tvLog.setText("?????????????????????,???3????????????...");
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
     * ??????activity???onKeyDown????????????????????????????????????activity
     * ????????????https://blog.csdn.net/qq_36713816/article/details/71511860
     * ?????????????????????????????????onBackPressed??????????????????????????????????????????????????????
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
                                Toast.makeText(MainActivity.this, "????????????????????????????????????", Toast.LENGTH_LONG).show();
                                return;
                            }
                            LOGIN_URL = ptAddrObj.getString("ptUrl");
                            DOWNLOAD = ptAddrObj.getString("apkDownload");
                            VERSION = ptAddrObj.getString("apkVersion");
                            minPl = Integer.parseInt(ptAddrObj.getString("pinLv"));
                            //????????????
                            String[] gongGao = ptAddrObj.getString("ptAnnoun").split(";");
                            announcementDialog(gongGao);
                        }catch (Exception e){
                            sendLog2("???????????????"+e.getMessage());
                        }

                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("????????????????????????~");
                    }
                });
    }



    /**
     * ??????
     * @param username
     * @param password
     * @param
     */
    private void userLogin(String username, String password){
        tvLog.setText(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": ???????????????..."+"\n");
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
                            //????????????
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
                            sendLog2("?????????"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("??????ERR???"+response.getException().toString());
                    }
                });
    }


    /**
     * ?????????????????????
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
                                //???????????????list??????
                                JSONArray tbArr = tbObj.getJSONArray("data");
                                buyerNumList.clear();
                                for (int i = 0; i < tbArr.size(); i++) {
                                    JSONObject tbInfo = tbArr.getJSONObject(i);
                                    //1  ?????????2 ?????????
                                    if("1".equals(tbInfo.getString("type"))){
                                        String tbId = tbInfo.getString("id");
                                        String tbName = tbInfo.getString("account");
                                        //String rYJ = tbInfo.getString("df_total");
                                        Integer rKJ = tbInfo.getInteger("surplus_df");
                                        if(rKJ == 0){
                                            sendLog("???"+tbName+"???????????????,???????????????~");
                                            continue;
                                        }
                                        buyerNumList.add(new BuyerNum(tbId,tbName));
                                    }
                                }
                                tbNameArr = new String[buyerNumList.size()];
                                sendLog("?????????"+buyerNumList.size()+"??????????????????");

                                for (int i = 0; i < buyerNumList.size(); i++) {
                                    tbNameArr[i] = buyerNumList.get(i).getName();
                                }
                                showSingleAlertDialog();
                            }else {
                                sendLog(tbObj.getString("msg"));
                            }
                        }catch (Exception e){
                            sendLog2(e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("??????????????????"+response.getException());
                    }
                });
    }


    /**
     * ???????????????
     */
    public void showSingleAlertDialog(){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("????????????????????????");
        alertBuilder.setCancelable(false); //??????????????????????????????????????????????????? false
        alertBuilder.setSingleChoiceItems( tbNameArr, -1, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int index) {
                //????????????????????????????????????id
                List<BuyerNum> buyerNum = buyerNumList.stream().
                        filter(p -> p.getName().equals(tbNameArr[index])).collect(Collectors.toList());
                tbId = buyerNum.get(0).getId();
            }
        });
        alertBuilder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //TODO ??????????????????
                if(tbId == null){
                    sendLog("????????????????????????");
                    return;
                }
                List<BuyerNum> buyerNum = buyerNumList.stream().
                        filter(p -> p.getId().equals(tbId)).collect(Collectors.toList());
                sendLog("????????? "+buyerNum.get(0).getName()+" ????????????");
                //??????????????????
                getAddress();
                // ???????????????
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
                .headers(DEVICE,decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .execute(new StringCallback() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(Response<String> response) {
                        /**
                         * {"code":401,"msg":"???????????????????????????","time":1666167598,"data":[]}
                         */
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            address = obj.getJSONObject("data").getString("address");
                            checkTbInfo();
                        }catch (Exception e){
                            sendLog2("getAddress???"+e.getMessage());
                        }
                    }
                });
    }



    /**
     * ????????????????????????????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkTbInfo(){
        HttpClient.getInstance().get(CHECK_TB_INFO+tbId, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers(DEVICE,decId)
                .headers("version",VERSION)
                .headers("app",APP)
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        JSONObject obj = JSONObject.parseObject(response.body());
                        if("200".equals(obj.getString("code"))){  //?????????
                            getTaskParam();
                        }else {
                            sendLog2(obj.getString("msg"));
                        }
                    }
                });
    }




    /**
     * ?????????????????????????????????
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getTaskParam() {

        HttpClient.getInstance().post(GET_AUTH, LOGIN_URL)
                .params("account_id",tbId)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers(DEVICE,decId)
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
                                //?????????????????????????????????????????????
                                sendLog(obj.getString("msg"));
                                playMusic(JIE_DAN_FAIL,3000,0);
                                //{"code":400,"msg":"?????????????????????","time":1644573968,"data":[]}
                                //{"code":400,"msg":"?????????????????????????????????","time":1640954628,"data":[]}
                            }
                        }catch (Exception e){
                            sendLog2("getTaskParam???"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("getTaskParam???"+response.getException());
                    }
                });
    }


    /**
     * ??????
     */
    private void getTask() {
        HttpClient.getInstance().get(GET_TASK+taskId+"&lat="+jingDu+"&lng="+weiDu+"&address="+address, LOGIN_URL)
                .headers("authorization",authorization)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .headers(DEVICE,decId)
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
                                String taskId = obj.getJSONObject("data").getString("id");  //?????????????????????
                                //String tbName = obj.getJSONObject("data").getString("u_account");
                                if (minYj > Double.parseDouble(wage)){
                                    sendLog("?????????"+wage+"?????????????????????,???????????????~");
                                    //???????????????????????????????????????
                                    cancelTask(taskId);
                                    return;
                                }
                                sendLog2("?????????,????????????!  ?????????"+wage);
                                receiveSuccess(wage);
                                sendLog2("-------------------------------");
                                sendLog2("????????????????????????????????????????????????"+obj.getJSONObject("data").getString("img"));
                                playMusic(JIE_DAN_SUCCESS,3000,2);
                            }else {
                                //???????????????????????????
                                //{"code":400,"msg":"?????????????????????????????????","time":1632585376,"data":[]}
                                //{"code":400,"msg":"????????????????????????","time":1632402201,"data":[]}
                                //{"code":400,"msg":"?????????GPS????????????","time":1632402563,"data":[]}
                                //{"code":400,"msg":"????????????????????????,????????????????????????","time":1632489187,"data":[]}
                                sendLog(obj.getString("msg"));
                                jieDan();
                            }
                        }catch (Exception e){
                            sendLog2("????????????:"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("????????????ERR:"+response.getException());
                        jieDan();
                    }
                });
    }

    private void jieDan(){
        //?????????????????????????????????????????????????????????????????????
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getTask();
            }
        }, minPl);
    }



    /**
     * ????????????
     *
     * ????????????????????????????????????????????????????????????????????????,???????????????
     * @param taskId
     */
    private void cancelTask(String taskId){

        HttpClient.getInstance().post(QUIT_TASK, LOGIN_URL)
                .params("id",taskId)
                .params("reason","???????????????")
                .headers("app",APP)
                .headers("authorization",authorization)
                .headers("version",VERSION)
                .headers(DEVICE,decId)
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
                                sendLog("?????????????????????"+obj.getString("msg"));
                            }
                        }catch (Exception e){
                            sendLog2("????????????"+e.getMessage());
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        sendLog2("????????????ERR???"+response.getException());
                    }
                });
    }



    /**
     * ????????????
     */
    private void stopQiangDan(){
        HttpClient.getInstance().get(STOP_TASK+taskId, LOGIN_URL)
                .headers("app",APP)
                .headers("authorization",authorization)
                .headers("version",VERSION)
                .headers(DEVICE,decId)
                .headers("user-agent","Dart/2.13 (dart:io)")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        try {
                            JSONObject obj = JSONObject.parseObject(response.body());
                            sendLog(obj.getString("msg"));
                            //{"code":200,"msg":"????????????","time":1662438811,"data":[]}
                            OkGo.getInstance().cancelAll();
                        }catch (Exception e){
                            sendLog2(e.getMessage());
                        }
                    }
                });
    }



    /**
     * ????????????
     */
    public void stop(){
        if(authorization != null)
            stopQiangDan();
        //Handler????????????????????????removeCallbacksAndMessages?????????Message???Runnable
        mHandler.removeCallbacksAndMessages(null);
    }


    /**
     * ???????????????????????????
     * @param voiceResId ????????????
     * @param milliseconds ????????????????????????
     */
    private void playMusic(int voiceResId, long milliseconds,int total){
        count = total;//?????????????????????
        //????????????
        MediaPlayer player = MediaPlayer.create(MainActivity.this, voiceResId);
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //??????????????????
                if(count != 0){
                    player.start();
                    count --;
                }
            }
        });

        //??????
        Vibrator vib = (Vibrator) this.getSystemService(Service.VIBRATOR_SERVICE);
        //??????????????????
        vib.vibrate(milliseconds);
    }

    /**
     * ????????????
     * @param log
     */
    public void sendLog(String log){
        scrollToTvLog();
        if(tvLog.getLineCount() > 40){
            tvLog.setText("");
        }
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
    }

    public void sendLog2(String log){
        scrollToTvLog();
        tvLog.append(new SimpleDateFormat("HH:mm:ss").format(new Date()) + ": "+log+"\n");
    }


    /**
     * ??????????????????
     */

    public void ignoreBatteryOptimization() {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(getPackageName());
            //  ????????????APP??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if(!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:"+getPackageName()));
                startActivity(intent);
            }
        }


    }


    /**
     * ??????????????????
     */
    private void saveUserInfo(String username,String password, String yj1){

        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        SharedPreferences.Editor editor = userInfo.edit();//??????Editor
        //??????Editor?????????????????????????????????
        editor.putString("username",username);
        editor.putString("password", password);
        editor.putString("yj1", yj1);
        editor.commit();//????????????

    }

    /**
     * ??????????????????
     */
    private void getUserInfo(){
        userInfo = getSharedPreferences("userData", MODE_PRIVATE);
        String username = userInfo.getString("username", null);//??????username
        String passwrod = userInfo.getString("password", null);//??????password
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



    //?????????????????????????????????   context???????????????Activity.??????tiis
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

    //????????????
    private void requestSettingCanDrawOverlays() {
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.O) {//8.0??????
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivityForResult(intent, 1);
        } else if (sdkInt >= Build.VERSION_CODES.M) {//6.0-8.0
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1);
        } else {//4.4-6.0??????
            //???????????????
        }
    }

    private void openNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //???????????????????????????????????????
            NotificationSetUtil.OpenNotificationSetting(this);
        }
    }


    /**
     * ????????????????????????
     */
    @SuppressLint("WrongConstant")
    protected void receiveSuccess(String dianPuName){
        //???????????????id????????????
        String channelId = CHANNELID;
        //??????????????????????????????
        String channelName = "???????????????????????????";
        //???????????????????????????????????????????????????????????????
        int importance = NotificationManager.IMPORTANCE_HIGH;

        // 2. ??????????????????????????????
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        // 3. ??????NotificationChannel(???????????????channelId?????????????????????channelId????????????????????????????????????????????????)
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelId,channelName, importance);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }
        //???????????????????????????Activity
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        // 1. ??????????????????(????????????channelId)
        Notification notification = new NotificationCompat.Builder(this,channelId)
                .setContentTitle(SUCCESS_TI_SHI)
                .setContentText("??????:"+dianPuName)
                .setSmallIcon(ICON)
                .setContentIntent(pendingIntent)//??????????????????Activity
                .setPriority(NotificationCompat.PRIORITY_HIGH) //?????????????????????????????????!!!!!!!!!!
                .setCategory(Notification.CATEGORY_TRANSPORT) //??????????????????
                .setVisibility(Notification.VISIBILITY_PUBLIC)  //????????????????????????????????????????????????
                .build();

        // 4. ????????????
        notificationManager.notify(2, notification);
    }



    public void onResume() {
        super.onResume();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //???????????????id????????? (??????????????????Context????????????Notification)
        notificationManager.cancel(2);
        //??????????????????
        //notificationManager.cancelAll();
    }

}


