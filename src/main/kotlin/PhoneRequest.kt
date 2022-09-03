
import com.google.gson.annotations.SerializedName

data class PhoneRequest(
    @SerializedName("application")
    val application: String,
    @SerializedName("key")
    val key: String,
    @SerializedName("phone")
    val phone: String
)