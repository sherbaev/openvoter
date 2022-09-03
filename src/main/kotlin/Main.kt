import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.KeyboardButton
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.swing.text.MaskFormatter


val bot by lazy { TelegramBot("5533465448:AAGLbtqsNHTiZNQMk_39IUIwyU4XBCB18KU") }

val tokenMap = HashMap<Long, String>()
val phoneMap = HashMap<Long, String>()

val service by lazy { NetworkService.api }

fun main() {
    bot.setUpdatesListener { updates ->
        updates.map { handle(it) }
        return@setUpdatesListener UpdatesListener.CONFIRMED_UPDATES_ALL
    }
}

private fun handle(update: Update) {
    val message = update.message()
    val applicationId = "125778"
    message?.let {
        val chatId = message.chat().id()
        println(message.toString())
        if (message.contact() != null) {
            val uuid = UUID.randomUUID().toString().filter { it != '-' }.plus("453987f1")
            val phoneMask = "+### (##) ###-##-##"
            val phoneNumber = message.contact().phoneNumber()
            phoneMap[chatId] = phoneNumber
            val maskFormatter = MaskFormatter(phoneMask)
            maskFormatter.valueContainsLiteralCharacters = false
            val number = maskFormatter.valueToString(phoneNumber)
            sendPhoneNumber(PhoneRequest(application = applicationId, key = uuid, phone = number), chatId)
        } else {
            val text = update.message().text()
            if (!text.isNullOrEmpty()) {
                when (text) {
                    "/start" -> {
                        sendMessage("Assalomu alaykum!", chatId, requireContact())
                    }

                    else -> {
                        val token = tokenMap[chatId]
                        val phone = phoneMap[chatId]
                        if (isNumeric(text) && token != null && phone != null) {
                            val otp = text
                            val smsRequest = SmsRequest(applicationId, otp, phone, token)
//                        sendMessage(smsRequest.toString(), chatId)
                            sendSms(smsRequest, chatId)
                        }
                    }
                }
            } else {
                sendMessage("Buyruq topilmadi!", chatId)
            }
        }

    }
}

fun sendMessage(text: String, chatId: Long, keyboard: ReplyKeyboardMarkup? = null) {
    if (keyboard != null) {
        bot.execute(SendMessage(chatId, text).replyMarkup(keyboard))
    } else {
        bot.execute(SendMessage(chatId, text))
    }
}

fun requireContact() = ReplyKeyboardMarkup(
    KeyboardButton("Kontaktni ulashing").requestContact(true)
).oneTimeKeyboard(true).resizeKeyboard(true).selective(true)

fun sendSms(smsRequest: SmsRequest, chatId: Long) {
    service.sendSms(smsRequest)
        .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(p0: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    sendMessage("Ovoz berildi!", chatId)
                } else {
                    sendMessage("OpenBudget tizimda nosozlik, birozdan so'ng qayta urinib ko'ring", chatId)
                }
            }

            override fun onFailure(p0: Call<ResponseBody>, p1: Throwable) {
                p1.printStackTrace()
                println(p1.stackTraceToString())
                sendMessage("Nosozlik birozdan so'ng qayta urinib ko'ring", chatId)
            }

        })
}

fun sendPhoneNumber(phoneRequest: PhoneRequest, chatId: Long) {
    service.sendPhone(phoneRequest)
        .enqueue(object : Callback<PhoneResponse> {
            override fun onResponse(p0: Call<PhoneResponse>, response: Response<PhoneResponse>) {
                print(response)
                if (response.isSuccessful && response.body() != null) {
                    sendMessage("Sms yuborildi kuting!", chatId)
                    tokenMap[chatId] = response.body()!!.token
                } else if (response.code() == 400) {
                    sendMessage("Ushbu raqam ovoz berish uchun oldin ishlatilgan", chatId)
                } else {
                    sendMessage("OpenBudget tizimda nosozlik, birozdan so'ng qayta urinib ko'ring", chatId)
                }
            }

            override fun onFailure(p0: Call<PhoneResponse>, p1: Throwable) {
                p1.printStackTrace()
                println(p1.stackTraceToString())
                sendMessage("Nosozlik birozdan so'ng qayta urinib ko'ring", chatId)
            }

        })
}

fun isNumeric(toCheck: String): Boolean {
    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return toCheck.matches(regex)
}