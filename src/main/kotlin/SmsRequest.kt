
import com.google.gson.annotations.SerializedName

data class SmsRequest(
    @SerializedName("application")
    val application: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("token")
    val token: String
)