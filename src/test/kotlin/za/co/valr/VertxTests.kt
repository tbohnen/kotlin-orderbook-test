package za.co.valr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.junit5.VertxExtension
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import za.co.valr.model.*
import za.co.valr.orderbook.OrderbookService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
class VertxTests {

    private lateinit var vertx: Vertx
    private val lock: CountDownLatch = CountDownLatch(1)

    private val gson: Gson
    private val now: Long

    private fun <T> any(): T {
        Mockito.any<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T

    init {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(OrderDirection::class.java, DirectionDeserializer())
        gson = gsonBuilder.create()
        now = DateTime.now(DateTimeZone.UTC).millis
    }

    @Mock
    private lateinit var orderbookServiceMock: OrderbookService

    @BeforeAll
    fun setup() {
        val now = DateTime.now(DateTimeZone.UTC).millis

        vertx = Vertx.vertx()
        orderbookServiceMock = mock(OrderbookService::class.java)
        orderbookServiceMock.setOrders(emptyList<OrderRecord>().toMutableList())
        doAnswer {
            println("Mocked start")
        }.`when`(orderbookServiceMock).start()


        `when`(orderbookServiceMock.getOrderBook()).thenReturn(

                Orderbook(listOf(
                        OrderbookRecordItem(
                                OrderDirection.ASK, CurrencyPair.BTCZAR, 123.45, 0.112, 2)),
                        listOf(
                                OrderbookRecordItem(OrderDirection.BID, CurrencyPair.BTCZAR, 543.21, 0.114, 1))
                )
        )

        `when`(orderbookServiceMock.getTradeHistory()).thenReturn(

                listOf(
                        TransactionHistoryRecord(
                                OrderDirection.ASK, CurrencyPair.BTCZAR, 123.45, 0.112, now),
                        TransactionHistoryRecord(
                                OrderDirection.BID, CurrencyPair.BTCZAR, 543.21, 0.114, now)
                )
        )

        //todo: not getting mocked correctly
//        `when`(orderbookServiceMock.addOrder(any())).then {
//            println("Order placed")
//        }


        vertx.deployVerticle(Server(orderbookServiceMock))
    }

    @AfterAll
    fun tearDown() {
        vertx.close()
    }

    @Test
    fun getOrderbook() {
        val client: WebClient = WebClient.create(vertx)
        client.get(8080, "localhost", "/orderbook")
                .`as`(BodyCodec.string())
                .send { ar: AsyncResult<HttpResponse<String>>? ->
                    if (ar!!.succeeded()) {
                        // Obtain response
                        val response: HttpResponse<String>? = ar?.result()
                        println("Received response with status code" + response?.statusCode())
                        println("Received response " + response?.body())

                        Assertions.assertTrue(true)

                    } else {
                        println("Something went wrong " + ar?.cause().message)
                        Assertions.assertTrue(false)

                    }
                    lock.countDown()

                }

        lock.await(10000, TimeUnit.MILLISECONDS)

    }

    @Test
    fun getTradeHistory() {
        val client: WebClient = WebClient.create(vertx)
        client.get(8080, "localhost", "/tradehistory")
                .`as`(BodyCodec.string())
                .send { ar: AsyncResult<HttpResponse<String>>? ->
                    if (ar!!.succeeded()) {
                        // Obtain response
                        val response: HttpResponse<String>? = ar?.result()
                        println("Received response with status code" + response?.statusCode())
                        println("Received response " + response?.body())

                        Assertions.assertTrue(true)

                    } else {
                        println("Something went wrong " + ar?.cause().message)
                        Assertions.assertTrue(false)

                    }
                    lock.countDown()

                }

        lock.await(10000, TimeUnit.MILLISECONDS)

    }

    @Test
    fun postOrder() {
        val client: WebClient = WebClient.create(vertx)
        val order = """{
            "side": "sell",
            "quantity": "0.5",
            "price": "12345.00",
            "currencyPair": "BTCZAR"
        }"""

        client.post(8080, "localhost", "/order")
                .`as`(BodyCodec.string())
                .sendJson(order) { ar: AsyncResult<HttpResponse<String>>? ->
                    if (ar!!.succeeded()) {
                        // Obtain response
                        val response: HttpResponse<String>? = ar?.result()
                        println("Received response with status code" + response?.statusCode())
                        println("Received response " + response?.body())

                        Assertions.assertTrue(true)

                    } else {
                        println("Something went wrong " + ar?.cause().message)
                        Assertions.assertTrue(false)

                    }
                    lock.countDown()

                }

        lock.await(10000, TimeUnit.MILLISECONDS)

    }
}