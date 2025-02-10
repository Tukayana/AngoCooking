package com.example.angocooking.API

import androidx.room.Query
import com.example.angocooking.Data.User
import com.example.angocooking.models.AuthResponse
import com.example.angocooking.models.Comment
import com.example.angocooking.models.Recipe
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path


interface ApiService {

    @POST("login")
    suspend fun login(@Body credentials: Map<String, String>): retrofit2.Response<AuthResponse>

    @POST("register")
    suspend fun register(@Body user: Map<String, String>): retrofit2.Response<Any>


    @GET("users/profile")
    suspend fun getUserProfile(): retrofit2.Response<User>

    @Multipart
    @PUT("users/profile-photo")
    suspend fun updateProfilePhoto(
        @Part foto: MultipartBody.Part
    ): retrofit2.Response<Map<String, String>>


    @GET("receitas")
    suspend fun getRecipes(): retrofit2.Response<List<Recipe>>

    @GET("receitas/{id}")
    suspend fun getRecipe(@Path("id") id: Int): retrofit2.Response<Recipe>

    @Multipart
    @POST("receitas")
    suspend fun createRecipe(
        @Part("nome") nome: RequestBody,
        @Part("ingredientes") ingredientes: RequestBody,
        @Part("modoPreparo") modoPreparo: RequestBody,
        @Part imagem: MultipartBody.Part?
    ): retrofit2.Response<Map<String, Any>>

    @Multipart
    @PUT("receitas/{id}")
    suspend fun updateRecipe(
        @Path("id") id: Int,
        @Part("nome") nome: RequestBody,
        @Part("ingredientes") ingredientes: RequestBody,
        @Part("modoPreparo") modoPreparo: RequestBody,
        @Part imagem: MultipartBody.Part?
    ): retrofit2.Response<Map<String, String>>

    @DELETE("receitas/{id}")
    suspend fun deleteRecipe(@Path("id") id: Int): retrofit2.Response<Map<String, String>>


    @GET("receitas/{receitaId}/comentarios")
    suspend fun getComments(@Path("receitaId") receitaId: Int): retrofit2.Response<List<Comment>>

    @POST("receitas/{receitaId}/comentarios")
    suspend fun createComment(
        @Path("receitaId") receitaId: Int,
        @Body comment: Map<String, String>
    ): retrofit2.Response<Comment>

    @PUT("comentarios/{id}")
    suspend fun updateComment(
        @Path("id") id: Int,
        @Body comment: Map<String, String>
    ): retrofit2.Response<Map<String, String>>

    @DELETE("comentarios/{id}")
    suspend fun deleteComment(@Path("id") id: Int): retrofit2.Response<Map<String, String>>
   /* @GET("receitas/search/q")
    suspend fun searchRecipes(@Query("q") query: String): List<Recipe>*/

}
