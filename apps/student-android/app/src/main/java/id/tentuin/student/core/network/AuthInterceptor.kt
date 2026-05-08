package id.tentuin.student.core.network

import id.tentuin.student.BuildConfig
import id.tentuin.student.core.datastore.SessionDataStore
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
        val accessToken = runBlocking { sessionDataStore.accessToken.first() }
        val request = chain.request().newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
            .addHeader("Content-Type", "application/json")
            .apply {
                if (accessToken != null) {
                    addHeader("Authorization", "Bearer $accessToken")
                } else {
                    addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
                }
            }
            .build()
        return chain.proceed(request)
    }
}
