package id.tentuin.student.core.network

import id.tentuin.student.BuildConfig
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.data.model.RefreshRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val sessionDataStore: SessionDataStore,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code != 401) return null
        // Avoid infinite loops — if the retry also returns 401, give up
        if (response.request.header("X-Retry-Auth") != null) return null

        val refreshToken = runBlocking { sessionDataStore.refreshToken.first() } ?: return null

        return runBlocking {
            try {
                val api = Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApi::class.java)

                val newTokens = api.refreshToken(body = RefreshRequest(refreshToken))
                sessionDataStore.updateAccessToken(newTokens.accessToken, newTokens.refreshToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .header("X-Retry-Auth", "true")
                    .build()
            } catch (e: Exception) {
                sessionDataStore.clearSession()
                null
            }
        }
    }
}
