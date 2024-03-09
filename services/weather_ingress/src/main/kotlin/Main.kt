package at.fhtw

import at.fhtw.geosphere.api.client.api.ValidatorApi


suspend fun main() {
    val k =ValidatorApi()
   val res =  k.validateByUrl("https://validator.swagger.io/validator/openapi.json")
    println("file:\\\\\\$res")
}