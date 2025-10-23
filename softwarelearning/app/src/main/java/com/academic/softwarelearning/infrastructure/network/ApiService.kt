package com.academic.softwarelearning.infrastructure.network

import com.academic.softwarelearning.domain.model.Arquivo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @Multipart
    @POST("/v1/arquivos")
    suspend fun adicionarArquivo(
        @Part("codigoatividadefirebase") codigo: RequestBody,
        @Part("nome") nome: RequestBody,
        @Part("responsavel") responsavel: RequestBody,
        @Part("tipoarquivo") tipoarquivo: RequestBody,
        @Part arquivo: MultipartBody.Part
    ): Arquivo

    @GET("/v1/arquivos/{responsavel}/{codigoatividadefirebase}")
    suspend fun buscarArquivos(
        @Path("responsavel") responsavel: String,
        @Path("codigoatividadefirebase") codigoatividadefirebase: String
    ): List<Arquivo>

    @GET
    @Streaming
    suspend fun downloadArquivo(
        @Url fileUrl: String
    ): ResponseBody
}