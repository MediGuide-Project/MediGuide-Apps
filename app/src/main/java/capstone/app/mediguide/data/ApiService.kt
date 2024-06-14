package capstone.app.mediguide.data

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/generate")
    suspend fun generateResponse(@Body request: GenerateRequest): Response<GenerateResponse>
}
