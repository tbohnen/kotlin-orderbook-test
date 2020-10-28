package za.co.valr

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.vertx.core.AbstractVerticle
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import za.co.valr.model.*
import za.co.valr.orderbook.OrderbookService


fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(Server(OrderbookService(CurrencyPair.BTCZAR)))
}

class Server(private val orderbookService: OrderbookService) : AbstractVerticle() {

    override fun start() {
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(OrderDirection::class.java, DirectionDeserializer())
        gsonBuilder.registerTypeAdapter(CurrencyPair::class.java, CurrencyDeserializer())

        val gson = gsonBuilder.create()
        orderbookService.start()
        router.get("/orderbook").handler { context ->
            val orderbook = orderbookService.getOrderBook()
            val rsp = gson.toJson(orderbook)

            context.response()
                    .putHeader("content-type", "application/json")
                    .end(rsp)
        }

        router.get("/tradehistory").handler { context ->
            val tradeHistory = orderbookService.getTradeHistory()
            val rsp = gson.toJson(tradeHistory)

            context.response()
                    .putHeader("content-type", "application/paint")
                    .end(rsp)
        }

        router.post("/order").handler { context ->
            val now = DateTime.now(DateTimeZone.UTC).millis
            val order = gson.fromJson(context.bodyAsString, Order::class.java)
            orderbookService.addOrder(OrderRecord(order.direction, order.currencyPair, OrderType.LIMIT, order.quantity, order.price, now, OrderStatus.PENDING))
            context.response()
                    .putHeader("content-type", "text/paint")
                    .end()
        }

        server.requestHandler(router).listen(8080)

        println("Server started on 8080")
        println()
    }
}