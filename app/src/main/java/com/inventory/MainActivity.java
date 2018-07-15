package com.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.afollestad.materialdialogs.MaterialDialog;
import com.handheld.uhfr.UHFRManager;
import com.uhf.api.cls.Reader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * @author huang
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.btn_scan)
    Button mBtnScan;
    @BindView(R.id.btn_save)
    Button mBtnSave;
    @BindView(R.id.linear1)
    LinearLayout mLinear1;
    @BindView(R.id.spinner_address)
    Spinner mSpinnerAddress;
    @BindView(R.id.spinner_type)
    Spinner mSpinnerType;
    @BindView(R.id.tv_scan_sum)
    TextView mTvScanSum;
    @BindView(R.id.tv_save_status)
    TextView mTvSaveStatus;
    @BindView(R.id.linear2)
    LinearLayout mLinear2;
    @BindView(R.id.epc_list)
    RecyclerView mEpcList;
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    ExecutorService mExecutorService = new ThreadPoolExecutor(3, 200, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    private List<String> mAddressList = new ArrayList<>();
    private List<AddressInfo> mAddressInfoList = new ArrayList<>();
    private List<String> mTypeList = new ArrayList<>();
    private List<TypeInfo> mTypeInfoList = new ArrayList<>();
    private RcvAdapter mRcvAdapter;
    private ArrayAdapter<String> mAddressAdapter;
    private ArrayAdapter<String> mTypeAdapter;
    private RetrofitRequestHelper mRetrofitRequestHelper;
    private Toast mToast;
    private String mOkCode = "200";
    private String mErrorCode = "400";
    private String mTypeError = "getBoxsign-error";
    private String mNoNetError = "Unable to resolve host";
    private String mAddressError = "getCompanyname.type-error";
    private String mAddressError1 = "getCompanyname.type.content-error";
    private String mAddressError2 = "getCompanyname-error";
    private String mUploadError = "saveRfid.rfid.type-error";
    private String mUploadError1 = "saveRfid.rfidlist.empty-error";
    private String mUploadError2 = "saveRfid.rfid.empty-error";
    private String mUploadError3 = "saveRfid.rfid.repeat-error";
    private String mUploadError4 = "saveRfid.sign-error";
    private String mUploadError5 = "saveRfid.buycompanyid-error";
    private String mUploadError6 = "saveRfid.buycompanyid.type-error";
    private String mUploadError7 = "saveRfid.buycompanyid.num-error";
    private long mLastClickTime= 0L;
    private int mSelectedAddressIndex = -1;
    private int mSelectedTypeIndex = -1;
    public MaterialDialog mUploadDialog;

    /**
     * 扫描到的EPC集合
     */
    private List<String> mEpcStrList = new ArrayList<>();
    private boolean keyControl = true;
    private boolean isRunning = true;
    private boolean isStart = false;
    private UHFRManager mUhfManager;
    /**
     * inventory epc
     */
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while (isRunning) {
                if (isStart) {
                    List<Reader.TAGINFO> list1;
                    list1 = mUhfManager.tagInventoryByTimer((short) 50);
                    if (list1 != null && list1.size() > 0) {
                        for (int i = 0; i < list1.size(); i++) {
                            Reader.TAGINFO taginfo = list1.get(i);
                            byte[] epcBytes = taginfo.EpcId;
                            String epcStr = Tools.Bytes2HexString(epcBytes, epcBytes.length).trim();
                            if (!mEpcStrList.contains(epcStr)) {
                                mEpcStrList.add(epcStr);
                            }
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SoundUtil.pasue();
                                SoundUtil.play(1, 0);
                            }
                        });
                    }
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private KeyReceiver mKeyReceiver;
    public class KeyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int keyCode = intent.getIntExtra("keyCode", 0);
            if (keyCode == 0) {
                keyCode = intent.getIntExtra("keycode", 0);
            }
            boolean keyDown = intent.getBooleanExtra("keydown", false);
            if (!keyDown) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_F3:
                    case KeyEvent.KEYCODE_F4:
                        runInventory();
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initRfid();
        initBean();
        initView();
        initListener();
        getTypeData();
        getAddressData();
    }

    @OnClick({R.id.btn_scan, R.id.btn_save})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_scan:
                runInventory();
                break;
            case R.id.btn_save:
                uploadTags();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        long currentTimeMillis = System.currentTimeMillis();
        long exitIntervalTime = 1500L;
        if (currentTimeMillis - mLastClickTime < exitIntervalTime){
            super.onBackPressed();
            mToast.cancel();
        }else {
            mLastClickTime = currentTimeMillis;
            showToast("再次点击退出应用");
        }
    }

    @Override
    protected void onDestroy() {
        isRunning = false;
        isStart = false;
        mUhfManager.close();
        // key receiver
        unregisterReceiver(mKeyReceiver);
        super.onDestroy();
    }

    private void initRfid() {
        mUhfManager = UHFRManager.getIntance(MainActivity.this);
        if (mUhfManager != null) {
            showToast("Rfid初始化成功");
        } else {
            showToast("Rfid初始化失败，尝试重启重试");
        }
        mExecutorService.execute(inventoryTask);
    }

    private void initBean() {
        mAddressList.add("请选择所在地址");
        mTypeList.add("请选择箱子类型");
        mRcvAdapter = new RcvAdapter();
        mAddressAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, mAddressList);
        mTypeAdapter = new ArrayAdapter<>(MainActivity.this, R.layout.support_simple_spinner_dropdown_item, mTypeList);
        mRetrofitRequestHelper = RetrofitRequestHelper.getRetrofitRequestHelper();
        SoundUtil.initSoundPool(MainActivity.this);
        // key Receiver
        mKeyReceiver = new KeyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        filter.addAction("android.intent.action.FUN_KEY");
        registerReceiver(mKeyReceiver, filter);
        //dialog
        mUploadDialog = new MaterialDialog.Builder(MainActivity.this)
                .progress(true, 100)
                .cancelable(false).build();
    }

    private void initView() {
        // keep screen on
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle("");
        }
        mSpinnerAddress.setAdapter(mAddressAdapter);
        mSpinnerType.setAdapter(mTypeAdapter);
        mEpcList.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mEpcList.setAdapter(mRcvAdapter);
    }

    private void initListener(){
        mSpinnerAddress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedAddressIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedTypeIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getTypeData() {
        mRetrofitRequestHelper.getTypeRequest(new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody body = (ResponseBody) response.body();
                try {
                    String bodyStr = body != null ? body.string() : null;
                    if (bodyStr != null) {
                        JSONObject jsonObject = new JSONObject(bodyStr);
                        String code = jsonObject.getString("code");
                        if (mOkCode.equals(code)) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            int length = data.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject object = (JSONObject) data.get(i);
                                String signname = object.getString("signname");
                                mTypeList.add(signname);
                                TypeInfo typeInfo = new TypeInfo();
                                String sign = object.getString("sign");
                                typeInfo.setSign(sign);
                                typeInfo.setSignName(signname);
                                mTypeInfoList.add(typeInfo);
                            }
                        } else {
                            String message = jsonObject.getString("message");
                            if (message.contains(mTypeError)) {
                                showToast("获取箱型失败");
                            } else {
                                showToast("获取箱型失败，未知错误");
                            }
                        }
                    } else {
                        showToast("获取箱型失败，未知错误");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("获取箱型失败，数据转换异常");
                }
            }

            @Override
            public void requestFail(Throwable t) {
                String message = t.getMessage();
                if (message.contains(mNoNetError)) {
                    showToast("获取地址失败，请检查网络连接后重试");
                }
            }
        });
    }

    private void getAddressData() {
        mRetrofitRequestHelper.getAddressRequest(new RetrofitRequestHelper.RetrofitRequestListener() {
            @Override
            public void requestSuccess(Response response) {
                ResponseBody body = (ResponseBody) response.body();
                try {
                    String bodyStr = body != null ? body.string() : null;
                    if (bodyStr != null) {
                        JSONObject jsonObject = new JSONObject(bodyStr);
                        String code = jsonObject.getString("code");
                        if (mOkCode.equals(code)) {
                            JSONArray data = jsonObject.getJSONArray("data");
                            int length = data.length();
                            for (int i = 0; i < length; i++) {
                                JSONObject object = (JSONObject) data.get(i);
                                String name = object.getString("name");
                                mAddressList.add(name);
                                AddressInfo addressInfo = new AddressInfo();
                                String id = object.getString("id");
                                addressInfo.setId(id);
                                addressInfo.setName(name);
                                mAddressInfoList.add(addressInfo);
                            }
                        } else {
                            String message = jsonObject.getString("message");
                            if (message.contains(mAddressError)) {
                                showToast("获取地址失败，type类型不存在");
                            } else if (message.contains(mAddressError1)) {
                                showToast("获取地址失败，type类型错误");
                            } else if (message.contains(mAddressError2)) {
                                showToast("获取地址失败，获取信息失败");
                            } else {
                                showToast("获取地址失败，未知错误");
                            }
                        }
                    } else {
                        showToast("获取地址失败，未知错误");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("获取地址失败，数据转换异常");
                }
            }

            @Override
            public void requestFail(Throwable t) {
                String message = t.getMessage();
                if (message.contains(mNoNetError)) {
                    showToast("获取地址失败，请检查网络连接后重试");
                }
            }
        });
    }

    private void showTags() {
        mRcvAdapter.setList(mEpcStrList);
        mTvScanSum.setText(getString(R.string.scan_sum));
        mTvScanSum.append(mEpcStrList.size() + "个");
    }

    private void uploadTags(){
        if (mSelectedTypeIndex == 0 || mSelectedAddressIndex == 0){
            showToast("请先选择地址和箱型");
        }else {
            mUploadDialog.show();
            String sign = mTypeInfoList.get(mSelectedTypeIndex - 1).getSign();
            String id = mAddressInfoList.get(mSelectedAddressIndex - 1).getId();
            mRetrofitRequestHelper.uploadTagsRequest(mEpcStrList, sign, id, new RetrofitRequestHelper.RetrofitRequestListener() {
                @Override
                public void requestSuccess(Response response) {
                    ResponseBody body = (ResponseBody) response.body();
                    try {
                        String bodyStr = body != null ? body.string() : null;
                        if (bodyStr != null) {
                            JSONObject jsonObject = new JSONObject(bodyStr);
                            String code = jsonObject.getString("code");
                            if (mOkCode.equals(code)) {
                                showToast("保存信息成功");
                                mTvSaveStatus.setText("已保存");
                            } else {
                                String message = jsonObject.getString("message");
                                if (message.contains(mUploadError)) {
                                    showToast("上传失败，rfid类型错误");
                                } else if (message.contains(mUploadError1)) {
                                    showToast("上传失败，rfid列表为空");
                                } else if (message.contains(mUploadError2)) {
                                    showToast("上传失败，rfid列表有空值");
                                } else if (message.contains(mUploadError3)){
                                    showToast("上传失败，rfid有重复数据");
                                } else if (message.contains(mUploadError4)){
                                    showToast("上传失败，箱子类型错误");
                                } else if (message.contains(mUploadError5)){
                                    showToast("上传失败，箱子所在地错误");
                                } else if (message.contains(mUploadError6)){
                                    showToast("上传失败，箱子所在地类型错误");
                                } else if (message.contains(mUploadError7)){
                                    showToast("上传失败，箱子所在地参数不存在");
                                }
                            }
                        } else {
                            showToast("上传失败，未知错误");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showToast("上传失败，数据转换异常");
                    }finally {
                        mUploadDialog.dismiss();
                    }
                }

                @Override
                public void requestFail(Throwable t) {
                    String message = t.getMessage();
                    if (message.contains(mNoNetError)) {
                        showToast("上传失败，请检查网络连接后重试");
                    }
                    mUploadDialog.dismiss();
                }
            });
        }
    }

    public void runInventory() {
        if (mUhfManager == null) {
            showToast("RFID异常，请退出应用重启");
            return;
        }
        if (keyControl) {
            keyControl = false;
            if (!isStart) {
                mBtnSave.setClickable(false);
                mBtnScan.setText(R.string.click_to_stop);
                mUhfManager.setCancleInventoryFilter();
                mUhfManager.setCancleFastMode();
                isStart = true;
            } else {
                isStart = false;
                mUhfManager.stopTagInventory();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mBtnSave.setClickable(true);
                mBtnScan.setText(R.string.click_to_scan);
                showTags();
            }
            keyControl = true;
        }
    }

    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(msg);
            mToast.show();
        }
    }

}
