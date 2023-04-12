package ke.co.osl.kisiifarmermappingapp.api

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {

    @POST("/api/v1/create")
    suspend fun createFarmer(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("/api/v1/employees")
    suspend fun getFarmers(): Response<ResponseBody>
}