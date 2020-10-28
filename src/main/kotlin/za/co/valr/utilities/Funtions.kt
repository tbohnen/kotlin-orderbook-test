package za.co.valr.utilities

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Signs the request payload using the api key secret
 *
 * @param apiKeySecret - the api key secret
 * @param timestamp    - the unix timestamp of this request e.g. Clock.systemUTC().millis()
 * @param verb         - Http verb - GET, POST, PUT or DELETE
 * @param path         - path excluding host name, e.g. '/v1/withdraw
 * @param body         - http request body as a string, optional
 * @return the signature of the request
 */

fun signRequest(apiKeySecret: String, timestamp: String, verb: String, path: String, body: String): String {
    val hmacSHA512 = Mac.getInstance("HmacSHA512")
    val secretKeySpec = SecretKeySpec(apiKeySecret.encodeToByteArray(), "HmacSHA512")
    hmacSHA512.init(secretKeySpec)
    hmacSHA512.update(timestamp.encodeToByteArray())
    hmacSHA512.update(verb.toUpperCase().encodeToByteArray())
    hmacSHA512.update(path.encodeToByteArray())
    hmacSHA512.update(body.encodeToByteArray())
    val digest = hmacSHA512.doFinal()

    return toHexString(digest)
}

fun toHexString(a: ByteArray): String {
    val sb: StringBuilder = StringBuilder(a.size * 2)
    for (b in a) sb.append(String.format("%02x", b))
    return sb.toString()
}