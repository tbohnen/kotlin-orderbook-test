package za.co.valr.orderbook

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import za.co.valr.model.*

fun main(args: Array<String>) {
    val obs = OrderbookService(CurrencyPair.BTCZAR)
    obs.start()
}

open class OrderbookService constructor(
        private val currencyPair: CurrencyPair) {

    private var orderbookItems: List<OrderbookRecordItem>
    private var transactions: MutableList<TransactionHistoryRecord>
    private var orders: MutableList<OrderRecord>
    private var state: State

    init {
        orderbookItems = emptyList()
        transactions = mutableListOf()
        orders = emptyList<OrderRecord>().toMutableList()
        state = State.START
    }


    open fun start() {
        println("Starting ...")
        GlobalScope.launch {
            state = State.START
            pull(3000)
        }
    }

    fun stop() {
        println("Stopping the Orderbook Service")
    }

    fun setOrderBook(orderbookItem: MutableList<OrderbookRecordItem>) {
        this.orderbookItems = orderbookItem
    }

    open fun getOrderBook(): Orderbook {
        return Orderbook(orderbookItems.filter { it.direction == OrderDirection.ASK }, orderbookItems.filter { it.direction == OrderDirection.BID })
    }

    open fun getTradeHistory(): List<TransactionHistoryRecord> {
        return this.transactions
    }

    fun setOrders(orders: MutableList<OrderRecord>) {
        this.orders = orders
    }

    fun getOrders(): List<OrderRecord> {
        return this.orders
    }

    fun setTransactions(transactions: MutableList<TransactionHistoryRecord>) {
        this.transactions = transactions
    }

    fun getTransactions(skip: Int = 0, limit: Int = 100): List<TransactionHistoryRecord> {
        return this.transactions.subList(skip, skip + limit)
    }

    fun addOrder(newOrder: OrderRecord) {
        println("Added Order")
        orders.add(newOrder)
        orderbookItems.filter { it.direction == newOrder.direction }.filter { it.price == newOrder.price }.map { it.quantity += newOrder.quantity }
    }

    fun getBestOrder(currencyPair: CurrencyPair, orderType: OrderType, orderDirection: OrderDirection): OrderRecord? {
        val direction = if (orderDirection == OrderDirection.BID) -1 else 1
        var order = orders.filter { it.direction == orderDirection }.filter {
            it.currencyPair == currencyPair && it.type == orderType
        }.minByOrNull { it.price * direction }
        order?.priceOrder()
        return order
    }

    private suspend fun changeState(state: State, delay: Long = 0, highestBidOrder: OrderRecord?, lowestAskOrder: OrderRecord?, message: String? = null) {
        this.state = state
        when (this.state) {
            State.PULL -> pull(delay)
            State.START -> pull(delay)
            State.MATCH -> match(highestBidOrder, lowestAskOrder)
            State.FILL -> fill(highestBidOrder, lowestAskOrder)
            State.ERROR -> error(message)
            State.STOP -> {
            }

        }
    }

    private suspend fun match(highestBidOrder: OrderRecord?, lowestAskOrder: OrderRecord?) {
        println("MATCH")

        if (highestBidOrder!!.calculatedPrice >= lowestAskOrder!!.calculatedPrice) {
            this.changeState(State.FILL, 0, highestBidOrder, lowestAskOrder)
            return
        }
        this.changeState(State.PULL, 5000, highestBidOrder, lowestAskOrder)
    }

    private suspend fun fill(highestBidOrder: OrderRecord?, lowestAskOrder: OrderRecord?) {
        println("FILL")

        val now = DateTime.now(DateTimeZone.UTC).millis

        if (highestBidOrder!!.calculatedQty == lowestAskOrder!!.calculatedQty) {
            highestBidOrder.status = OrderStatus.FILLED
            lowestAskOrder.status = OrderStatus.FILLED
            orderbookItems.filter { it.direction == highestBidOrder.direction }.filter { it.price == highestBidOrder.price }.map { it.quantity -= highestBidOrder.calculatedQty }
            orderbookItems.filter { it.direction == lowestAskOrder.direction }.filter { it.price == lowestAskOrder.price }.map { it.quantity -= lowestAskOrder.calculatedQty }

            transactions.add(TransactionHistoryRecord(highestBidOrder.direction, highestBidOrder.currencyPair, highestBidOrder.calculatedQty, highestBidOrder.calculatedPrice, now))
            transactions.add(TransactionHistoryRecord(lowestAskOrder.direction, lowestAskOrder.currencyPair, lowestAskOrder.calculatedQty, lowestAskOrder.calculatedPrice, now))

        }

        // the offer is less than the bid
        if (highestBidOrder.calculatedQty > lowestAskOrder.calculatedQty) {
            highestBidOrder.status = OrderStatus.PARTIALLY_FILLED
            lowestAskOrder.status = OrderStatus.FILLED
            orderbookItems.filter { it.direction == highestBidOrder.direction }.filter { it.price == highestBidOrder.price }.map { it.quantity -= highestBidOrder.calculatedQty }
            transactions.add(TransactionHistoryRecord(highestBidOrder.direction, highestBidOrder.currencyPair, highestBidOrder.calculatedQty, highestBidOrder.calculatedPrice, now))

        }

        // the bid is less than the offer
        if (highestBidOrder.calculatedQty < lowestAskOrder.calculatedQty) {
            lowestAskOrder.status = OrderStatus.FILLED
            lowestAskOrder.status = OrderStatus.PARTIALLY_FILLED
            orderbookItems.filter { it.direction == lowestAskOrder.direction }.filter { it.price == lowestAskOrder.price }.map { it.quantity -= lowestAskOrder.calculatedQty }
            transactions.add(TransactionHistoryRecord(lowestAskOrder.direction, lowestAskOrder.currencyPair, lowestAskOrder.calculatedQty, lowestAskOrder.calculatedPrice, now))
        }

        this.changeState(State.PULL, 5000, highestBidOrder, lowestAskOrder)

    }

    private fun error(message: String?) {
        println("Error : $message")
    }

    //TODO: since we only have limit orders we do not have to keep a list of current orders
    private suspend fun pull(delay: Long) {
        val highestBidOrder = getBestOrder(currencyPair, OrderType.LIMIT, OrderDirection.BID)
        val lowestAskOrder = getBestOrder(currencyPair, OrderType.LIMIT, OrderDirection.ASK)

        //can't be a match if there is not at least a bid and an ask
        if (highestBidOrder == null || lowestAskOrder == null) {
            delay(delay)
            changeState(State.PULL, delay, highestBidOrder, lowestAskOrder)
        } else {
            changeState(State.MATCH, delay, highestBidOrder, lowestAskOrder)

        }
    }

    fun getState(): State {
        return this.state
    }


}
