package com.inventory;

import android.net.sip.SipSession;
import android.support.annotation.NonNull;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Field;

/**
 * @author huang
 */
public class RetrofitRequestHelper {
    private static RetrofitRequestHelper sRetrofitRequestHelper;
    private static RetrofitInterface sRetrofitInterface;
    private RetrofitRequestHelper(){}
    public static RetrofitRequestHelper getRetrofitRequestHelper(){
        // 创建Retrofit对象
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://wechat.geek-q.cc/stock/App/")
                .build();
        // 创建 网络请求接口 的实例
        sRetrofitInterface = retrofit.create(RetrofitInterface.class);
        if (sRetrofitRequestHelper == null){
            sRetrofitRequestHelper = new RetrofitRequestHelper();
        }
        return sRetrofitRequestHelper;
    }

    public void getAddressRequest(final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.getAddressCall("1");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    public void getTypeRequest(final RetrofitRequestListener listener){
        Call<ResponseBody> typeCall = sRetrofitInterface.getTypeCall();
        typeCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }

    public void uploadTagsRequest(List<String> rfid, String sign, String buycompanyid,final RetrofitRequestListener listener){
        Call<ResponseBody> call = sRetrofitInterface.saveRfidCall(rfid, sign, buycompanyid);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                listener.requestSuccess(response);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                listener.requestFail(t);
            }
        });
    }


    /**
     * 网络请求回掉接口
     */
    public interface RetrofitRequestListener{
        /**
         * 请求成功
         * @param response 返回的回复体
         */
        void requestSuccess(Response response);
        /**
         * 请求失败
         * @param t 抛出的异常信息
         */
        void requestFail(Throwable t);
    }
}
