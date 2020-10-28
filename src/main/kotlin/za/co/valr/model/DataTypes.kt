package za.co.valr.model

import com.google.gson.annotations.SerializedName

data class Orderbook(
        @SerializedName("asks") val asks: List<OrderbookRecordItem>,
        @SerializedName("bids") val bids: List<OrderbookRecordItem>
)

data class Order(
        @SerializedName("direction") val direction: OrderDirection,
        @SerializedName("currencyPair") val currencyPair: CurrencyPair,
        @SerializedName("type") val type: OrderType,
        @SerializedName("quantity") val quantity: Double,
        @SerializedName("price") val price: Double
)


class TransactionHistory(@SerializedName("transaction_history") val transactionHistory: List<TransactionHistoryItem>)

class TransactionHistoryItem(@SerializedName("direction") val direction: OrderDirection,
                             @SerializedName("quantity") val quantity: Long,
                             @SerializedName("amount") val amount: Long,
                             @SerializedName("price") val price: Long,
                             @SerializedName("transaction_ts") val transactionTs: Long)
