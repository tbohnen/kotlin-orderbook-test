package za.co.valr.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import kotlin.properties.Delegates

data class OrderbookRecordItem(
        @SerializedName("side") val direction: OrderDirection,
        @SerializedName("currencyPair") val currencyPair: CurrencyPair,
        @SerializedName("price") val price: Double,
        @SerializedName("quantity") var quantity: Double,
        @SerializedName("orderCount") val count: Int)

class TransactionHistoryRecord(val direction: OrderDirection,
                               val currencyPair: CurrencyPair,
                               val amount: Double,
                               val price: Double,
                               val transactionTs: Long)

class OrderRecord(val direction: OrderDirection,
                  val currencyPair: CurrencyPair,
                  val type: OrderType,
                  val quantity: Double,
                  val price: Double,
                  val transactionTs: Long,
                  var status: OrderStatus) {
    var calculatedQty by Delegates.notNull<Double>()
    var calculatedPrice by Delegates.notNull<Double>()

    fun priceOrder() {
        this.calculatedPrice = price // this will be different for an instant order
        calculatedQty = if (direction === OrderDirection.ASK) quantity;
        else quantity.div(price)
    }
}

internal class DirectionDeserializer : JsonDeserializer<OrderDirection?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): OrderDirection? {
        val directions: Array<OrderDirection> = OrderDirection.values()
        for (direction in directions) {
            if (direction.direction == json.asString) return direction
        }
        return null
    }
}

internal class CurrencyDeserializer : JsonDeserializer<CurrencyPair?> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): CurrencyPair? {
        val currencies: Array<CurrencyPair> = CurrencyPair.values()
        for (currency in currencies) {
            if (currency.currency == json.asString) return currency
        }
        return null
    }
}