package id.tentuin.student.ui.screen.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import id.tentuin.student.core.datastore.SessionDataStore
import id.tentuin.student.ui.MainActivity
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OAuthCallbackActivity : ComponentActivity() {

    @Inject
    lateinit var sessionDataStore: SessionDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: run { finish(); return }

        // Supabase implicit flow: tokens in URI fragment (#access_token=...&refresh_token=...)
        val fragment = uri.fragment ?: ""
        val params   = fragment.split("&").associate { part ->
            val idx = part.indexOf('=')
            if (idx >= 0) part.substring(0, idx) to part.substring(idx + 1) else part to ""
        }

        val accessToken  = params["access_token"]
        val refreshToken = params["refresh_token"]
        val userId       = params["user_id"] ?: extractUserIdFromToken(accessToken)

        if (accessToken != null && refreshToken != null && userId != null) {
            lifecycleScope.launch {
                sessionDataStore.saveSession(accessToken, refreshToken, userId)
                navigateToMain()
            }
        } else {
            // PKCE flow: code in query param
            val code = uri.getQueryParameter("code")
            if (code != null) {
                // For PKCE, exchange the code via API — not implemented in this activity;
                // the app handles this through the standard Supabase implicit flow.
                navigateToMain()
            } else {
                finish()
            }
        }
    }

    private fun extractUserIdFromToken(token: String?): String? {
        if (token == null) return null
        return try {
            val payload = token.split(".").getOrNull(1) ?: return null
            val decoded = android.util.Base64.decode(
                payload.padEnd((payload.length + 3) / 4 * 4, '='),
                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP,
            )
            val json    = String(decoded)
            val subStart = json.indexOf("\"sub\":\"") + 7
            val subEnd   = json.indexOf("\"", subStart)
            if (subStart > 6 && subEnd > subStart) json.substring(subStart, subEnd) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun navigateToMain() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
