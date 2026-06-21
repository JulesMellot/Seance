package com.seance.tv.di

import com.seance.tv.di.ServerManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerUrlInterceptor @Inject constructor(
    private val serverManager: ServerManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val serverUrl = runBlocking { serverManager.serverUrl.first() }
            ?: return chain.proceed(chain.request())

        return try {
            val uri = URI(serverUrl)
            val newUrl = chain.request().url.newBuilder()
                .scheme(uri.scheme)
                .host(uri.host)
                .port(if (uri.port == -1) if (uri.scheme == "https") 443 else 80 else uri.port)
                .build()
            chain.proceed(chain.request().newBuilder().url(newUrl).build())
        } catch (e: Exception) {
            chain.proceed(chain.request())
        }
    }
}
