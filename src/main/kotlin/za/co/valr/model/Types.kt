package za.co.valr.model

enum class OrderDirection(val direction: String) {
    BID("buy"),
    ASK("sell")
}

enum class CurrencyPair(val currency: String) {
    BTCZAR("btczar")
}

enum class OrderType(val orderType: String) {
    INSTANT("instant"),
    LIMIT("limit")
}

enum class OrderStatus(val orderStatus: String) {
    PENDING("pending"),
    PARTIALLY_FILLED("partiallyFilled"),
    FILLED("filled")
}

enum class State(val state: String) {
    START("start"),
    PULL("pull"),
    MATCH("match"),
    FILL("fill"),
    STOP("stop"),
    ERROR("error")
}