package com.example.news_app

import android.content.Context
import android.widget.Toast
import com.example.news_app.Models.NewsApiResponse
import com.example.news_app.Models.SourcesApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.Exception
import java.lang.NullPointerException

class RequestManager(
    val context: Context
) {
    val api_key = context.resources.getString(R.string.api_key1)
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://newsapi.org/v2/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun getNewsHeadlines(
        listener: OnFetchDataListener<NewsApiResponse>,
        category: String?,
        query: String?,
        sources: String?,
        country: String?
    ) {
        val callNewsApi: CallNewsApi = retrofit.create(CallNewsApi::class.java)
        val call: Call<NewsApiResponse> = callNewsApi.callHeadlines(
            country = country,
            category = category,
            query = query,
            sources = sources,
            api_key = api_key
        )

        try {
            call.enqueue(object : Callback<NewsApiResponse> {
                override fun onResponse(
                    call: Call<NewsApiResponse>,
                    response: Response<NewsApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        if(response.code() == 429){
                            Toast.makeText(context, "You made too many requests today. Please try again later", Toast.LENGTH_SHORT).show()
                        } else if(response.code() == 500){
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(context, "Error: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                        }
                    } else{
                        try{
                            listener.onFetchData(response.body()!!.articles, response.message())
                        } catch(e: NullPointerException){
                            e.printStackTrace()
                        }
                    }

                }

                override fun onFailure(call: Call<NewsApiResponse>, t: Throwable) {
                    listener.onError("Request failed")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getNewsEverything(
        listener: OnFetchDataListener<NewsApiResponse>,
        query: String?,
        sources: String?,
        language: String? = "en"
    ) {
        val callNewsApi: CallNewsApi = retrofit.create(CallNewsApi::class.java)
        val call: Call<NewsApiResponse> = callNewsApi.callEverything(
            query = query,
            sources = sources,
            language = language,
            api_key = api_key
        )

        try {
            call.enqueue(object : Callback<NewsApiResponse> {
                override fun onResponse(
                    call: Call<NewsApiResponse>,
                    response: Response<NewsApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        if(response.code() == 400){
                            Toast.makeText(context, "Please specify Sources or Language or Keywords", Toast.LENGTH_SHORT).show()
                        } else if(response.code() == 429){
                            Toast.makeText(context, "You made too many requests today. Please try again later", Toast.LENGTH_SHORT).show()
                        } else if(response.code() == 500){
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(context, "Error: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                        }
                    } else{
                        try{
                            listener.onFetchData(response.body()!!.articles, response.message())
                        } catch(e: NullPointerException){
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<NewsApiResponse>, t: Throwable) {
                    listener.onError("Request failed")
                }
            })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getSources(
        listener: OnFetchSourcesListener<SourcesApiResponse>,
        category: String?,
        language: String?,
        country: String?
    ) {
        val callNewsApi: CallNewsApi = retrofit.create(CallNewsApi::class.java)
        val call: Call<SourcesApiResponse> = callNewsApi.callSources(
            category = category,
            language = language,
            country = country,
            api_key = api_key
        )

        try {
            call.enqueue(object : Callback<SourcesApiResponse> {
                override fun onResponse(
                    call: Call<SourcesApiResponse>,
                    response: Response<SourcesApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        if(response.code() == 400){
                            Toast.makeText(context, "Bad Request", Toast.LENGTH_SHORT).show()
                        } else if(response.code() == 429){
                            Toast.makeText(context, "You made too many requests today. Please try again later", Toast.LENGTH_SHORT).show()
                        } else if(response.code() == 500){
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show()
                        } else{
                            Toast.makeText(context, "Error: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                        }
                    } else{
                        try{
                            listener.onFetchSources(response.body()!!.sources, response.message())
                        } catch(e: NullPointerException){
                            e.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<SourcesApiResponse>, t: Throwable) {
                    listener.onError("Request failed")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface CallNewsApi {
        @GET("top-headlines")
        fun callHeadlines(
            @Query("country") country: String?,
            @Query("category") category: String?,
            @Query("q") query: String?,
            @Query("sources") sources: String?,
            @Query("apiKey") api_key: String
        ): Call<NewsApiResponse>

        @GET("everything")
        fun callEverything(
            @Query("q") query: String?,
            @Query("sources") sources: String?,
            @Query("language") language: String?,
            @Query("apiKey") api_key: String?,
        ): Call<NewsApiResponse>

        @GET("top-headlines/sources")
        fun callSources(
            @Query("category") category: String?,
            @Query("language") language: String?,
            @Query("country") country: String?,
            @Query("apiKey") api_key: String?
        ): Call<SourcesApiResponse>
    }
}