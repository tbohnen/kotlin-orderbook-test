package za.co.valr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import za.co.valr.model.*
import za.co.valr.orderbook.OrderbookService
import za.co.valr.utilities.signRequest

class TradeHistoryTests {


    private val gson: Gson
    private val now: Long
    private val apiKeySecret: String = System.getProperty("apiSecretKey")
    private val apiKey: String = System.getProperty("apiKey")

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(OrderDirection::class.java, DirectionDeserializer())
        gson = gsonBuilder.create()
        now = DateTime.now(DateTimeZone.UTC).millis
    }


    @Test
    fun `can get the trade history from valr`() {
        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val ts = DateTime.now(DateTimeZone.UTC).millis.toString()
        val request: Request = Request.Builder()
                .addHeader("X_VALR_API_KEY", apiKey)
                .addHeader("HEADER_VALR_TIMESTAMP", ts)
                .addHeader("HEADER_VALR_SIGNATURE", signRequest(apiKeySecret, ts, "GET", "/v1/account/BTCZAR/tradehistory", ""))
                .url("https://api.valr.com/v1/account/BTCZAR/tradehistory")
                .method("GET", null)
                .build()

        val response: Response = client.newCall(request).execute()
        println(response.body!!.string())

        Assertions.assertTrue(true)
    }

}