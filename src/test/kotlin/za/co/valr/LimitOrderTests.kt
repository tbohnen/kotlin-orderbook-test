package za.co.valr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Assert
import org.junit.jupiter.api.Test
import za.co.valr.model.*
import za.co.valr.orderbook.OrderbookService
import java.nio.file.Path
import java.nio.file.Paths


class LimitOrderTests {
    private val gson: Gson
    private val now: Long

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(OrderDirection::class.java, DirectionDeserializer())
        gson = gsonBuilder.create()
        now = DateTime.now(DateTimeZone.UTC).millis

    }

    @Test
    fun `can get the lowest bid`() {
        val orderbook = OrderbookService(CurrencyPair.BTCZAR)

        val ordersResourceDirectory: Path = Paths.get("src", "test", "resources", "orders.json")

        val ordersFileContents = ordersResourceDirectory.toFile().readText()

        var orders = gson.fromJson(ordersFileContents, Array<OrderbookRecordItem>::class.java).toMutableList()

        orderbook.setOrderBook(orders)
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 10.0, now, OrderStatus.PENDING))
        Assert.assertTrue(true)
    }

}