package id.tentuin.university.core.network

import id.tentuin.university.BuildConfig
import id.tentuin.university.core.datastore.SessionDataStore
import id.tentuin.university.data.model.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore,
    private val apiProvider:      Provider<UniversityApi>,
) : Interceptor {

    private val refreshMutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = runBlocking { sessionDataStore.accessToken.first() }
        val response = chain.proceed(originalRequest.withAuth(token))

        val isAuthEndpoint = originalRequest.url.encodedPath.contains("/auth/v1/")
        if (response.code != 401 || token == null || isAuthEndpoint) return response

        response.close()
        val refreshed = runBlocking { tryRefresh(staleToken = token) }
        if (!refreshed) return chain.proceed(originalRequest.withAuth(null))

        val newToken = runBlocking { sessionDataStore.accessToken.first() }
        return chain.proceed(originalRequest.withAuth(newToken))
    }

    private fun Request.withAuth(token: String?): Request =
        newBuilder()
            .header("apikey",        BuildConfig.SUPABASE_ANON_KEY)
            .header("Content-Type",  "application/json")
            .header("Authorization", "Bearer ${token ?: BuildConfig.SUPABASE_ANON_KEY}")
            .build()

    private suspend fun tryRefresh(staleToken: String): Boolean = refreshMutex.withLock {
        val current = sessionDataStore.accessToken.first()
        if (current != null && current != staleToken) return@withLock true

        val refreshToken = sessionDataStore.refreshToken.first() ?: return@withLock false
        try {
            val res = apiProvider.get().refreshToken(body = RefreshRequest(refreshToken))
            sessionDataStore.updateTokens(res.accessToken, res.refreshToken)
            true
        } catch (e: Exception) {
            sessionDataStore.clearSession()
            false
        }
    }
}
