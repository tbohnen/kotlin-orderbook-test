package za.co.valr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runBlockingTest
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

class OrderbookServiceTests {


    private val gson: Gson
    private val now: Long

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(OrderDirection::class.java, DirectionDeserializer())
        gson = gsonBuilder.create()
        now = DateTime.now(DateTimeZone.UTC).millis
    }

    @Test
    fun `can parse orderbook to json`() {
        val orderbook = Orderbook(listOf(
                OrderbookRecordItem(
                        OrderDirection.ASK, CurrencyPair.BTCZAR, 123.45, 0.112, 2)),
                listOf(
                        OrderbookRecordItem(OrderDirection.BID, CurrencyPair.BTCZAR, 543.21, 0.114, 1))
        )
        println(gson.toJson(orderbook))
    }
    @Test
    fun `can read in orders`() {
        val orderJson = """{
            "side": "sell",
            "quantity": "5",
            "price": "6",
            "currencyPair": "BTCZAR",
            "orderCount": 1
        }"""

        var order = gson.fromJson(orderJson, OrderbookRecordItem::class.java)
        println(order.toString())
        Assertions.assertEquals(OrderDirection.ASK, order.direction)
        Assertions.assertEquals(5.0, order.quantity)
        Assertions.assertEquals(6.0, order.price)
        Assertions.assertEquals(CurrencyPair.BTCZAR, order.currencyPair)
        Assertions.assertEquals(1, order.count)

    }

    @Test
    fun `can add an order`() {
        val orderbook = OrderbookService(CurrencyPair.BTCZAR)
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,11.0, now, OrderStatus.PENDING))
        val orders = orderbook.getOrders()
        Assertions.assertTrue(orders.isNotEmpty())
    }

    @Test
    fun `can get highest bid`() {
        val orderbook = OrderbookService(CurrencyPair.BTCZAR)
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 11.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 9.3, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 9.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 14.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 2.0, now, OrderStatus.PENDING))

        val order = orderbook.getBestOrder(
                CurrencyPair.BTCZAR,
                OrderType.LIMIT,
                OrderDirection.BID
        )
        Assertions.assertEquals(14.0, order!!.price)
    }

    @Test
    fun `can get lowest offer`() {
        val orderbook = OrderbookService(CurrencyPair.BTCZAR)
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,11.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,9.3, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,9.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,14.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,2.0, now, OrderStatus.PENDING))

        val order = orderbook.getBestOrder(
                CurrencyPair.BTCZAR,
                OrderType.LIMIT,
                OrderDirection.ASK
        )
        Assertions.assertEquals(2.0, order!!.price)
    }

    @Test
    fun `can get a match`() {
        val orderbook = OrderbookService(CurrencyPair.BTCZAR)
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 11.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 9.0, now, OrderStatus.PENDING))

        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,9.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,14.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,2.0, now, OrderStatus.PENDING))

        val order = orderbook.getBestOrder(
                CurrencyPair.BTCZAR,
                OrderType.LIMIT,
                OrderDirection.ASK
        )
        Assertions.assertEquals(9.0, order!!.price)
    }

    @Test
    fun `can get the orderbook`() = runBlockingTest {
    val orderbook = OrderbookService(CurrencyPair.BTCZAR)
        orderbook.start()
        Assert.assertEquals(State.START, orderbook.getState())
        orderbook.addOrder(OrderRecord(OrderDirection.ASK, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0, 9.0, now, OrderStatus.PENDING))
        orderbook.addOrder(OrderRecord(OrderDirection.BID, CurrencyPair.BTCZAR, OrderType.LIMIT, 10.0,9.0, now, OrderStatus.PENDING))
        delay(15_000)
//        orderbook.stop()
    }



    @Test
    fun `can get the orderbook from valr`() {
        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val request: Request = Request.Builder()
                .url("https://api.valr.com/v1/public/BTCZAR/orderbook")
                .method("GET", null)
                .build()
        val response: Response = client.newCall(request).execute()
        println(response.body!!.string())

        Assertions.assertTrue(true)
    }

}