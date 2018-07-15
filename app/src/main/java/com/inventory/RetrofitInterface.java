package com.inventory;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * @author huang 3004240957@qq.com
 */
public interface RetrofitInterface {
    /**
     * 获取箱子类型列表
     * @return 箱子类型列表
     */
    @POST("Boxsign/getBoxsign")
    Call<ResponseBody> getTypeCall();

    /**
     * 获取所在地址列表
     * @param type 公司类型，此处传固定值1，普通公司
     * @return 所在地址列表
     */
    @FormUrlEncoded
    @POST("Company/getCompanyname")
    Call<ResponseBody> getAddressCall(@Field("type") String type);

    /**
     * 上传标签信息数组
     * @param rfid 标签数组
     * @param sign 箱子类型
     * @param buycompanyid 所在地址id
     * @return 上传结果
     */
    @FormUrlEncoded
    @POST("Tags/saveRfid")
    Call<ResponseBody> saveRfidCall(@Field("rfid[]") List<String> rfid,@Field("sign") String sign, @Field("buycompanyid") String buycompanyid);
}
