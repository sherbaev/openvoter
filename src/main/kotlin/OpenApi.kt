import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OpenApi {
    @POST("user/validate_phone/")
    fun sendPhone(@Body phoneRequest: PhoneRequest): Call<PhoneResponse>

    @POST("user/temp/vote/")
    fun sendSms(@Body smsRequest: SmsRequest): Call<ResponseBody>
}