package id.tentuin.agent.core.network

import id.tentuin.agent.BuildConfig
import id.tentuin.agent.core.datastore.SessionDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionDataStore.accessToken.first() }
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer ${token ?: BuildConfig.SUPABASE_ANON_KEY}")
            .build()
        return chain.proceed(request)
    }
}
