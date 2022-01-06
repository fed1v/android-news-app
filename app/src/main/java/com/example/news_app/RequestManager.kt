package com.example.news_app

import android.content.Context
import android.widget.Toast
import com.example.news_app.Models.NewsApiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.lang.Exception

class RequestManager(
    val context: Context
) {
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
            api_key = context.getString(R.string.api_key2)
        )

        try {
            call.enqueue(object : Callback<NewsApiResponse> {
                override fun onResponse(
                    call: Call<NewsApiResponse>,
                    response: Response<NewsApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                    listener.onFetchData(response.body()!!.articles, response.message())
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
        sources: String?
    ) {
        val callNewsApi: CallNewsApi = retrofit.create(CallNewsApi::class.java)
        val call: Call<NewsApiResponse> = callNewsApi.callEverything(
            query = query,
            sources = sources,
            language = "en",
            api_key = context.getString(R.string.api_key2)
        )

        try {
            call.enqueue(object : Callback<NewsApiResponse> {
                override fun onResponse(
                    call: Call<NewsApiResponse>,
                    response: Response<NewsApiResponse>
                ) {
                    if (!response.isSuccessful) {
                        Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show()
                    }
                    listener.onFetchData(response.body()!!.articles, response.message())
                }

                override fun onFailure(call: Call<NewsApiResponse>, t: Throwable) {
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
    }
}