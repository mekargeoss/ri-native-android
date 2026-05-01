/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

data class UiState(
    val isLoggedIn: Boolean = false,
    val status: String = "Ready",
    val lastResponse: String? = null,
    val sessionToken: String? = null,
)

class AuthViewModel(private val ctx: Context, private val activity: MainActivity) {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    suspend fun login() {
        _state.value =
            _state.value.copy(status = "Preparing keys & getting attestation challenge...")

        DPoPKeys.ensureKeypair()
        val jwk = DPoPKeys.publicJwk()

        val challenge = withContext(Dispatchers.IO) { Api.attestationChallenge(jwk) }
        val nonce = challenge.nonce
        val evidences = mutableMapOf<String, Any>("nonce" to nonce)

        try {
            val token = PlayIntegrityHelper.requestIntegrityToken(ctx, nonce)
            if (token != null) evidences["integrityToken"] = token
        } catch (_: Exception) {

        }

        _state.value = _state.value.copy(status = "Asking server to verify...")
        try {
            val bootstrap = withContext(Dispatchers.IO) {
                Api.bootstrap(jkt = challenge.jkt, evidences = evidences)
            }
            _state.value =
                _state.value.copy(status = "Verification OK. Opening Mekarge A3 login ...")
            openLoginInBrowser(bootstrap.loginStartUrl)
        } catch (e: Exception) {
            _state.value = _state.value.copy(status = "Verification error: ${e.message}")
        }
    }

    fun handleDeepLink(uri: Uri) {
        if (uri.scheme != Config.DEEP_LINK_SCHEME || uri.host != Config.DEEP_LINK_HOST) return
        val code = uri.getQueryParameter("code") ?: run {
            _state.value = _state.value.copy(status = "Deep-link missing code")
            return
        }
        scope.launch { finishLogin(code) }
    }

    private suspend fun finishLogin(handoffCode: String) {
        _state.value = _state.value.copy(status = "Finalizing login with DPoP...")
        try {
            val result = withContext(Dispatchers.IO) {
                Api.finish(appHandoffCode = handoffCode)
            }
            _state.value = _state.value.copy(
                status = "Logged in. Expires at: ${result.expiresAt}",
                isLoggedIn = true,
                sessionToken = result.sessionToken
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(status = "Finalization error: ${e.message}")
        }
    }

    suspend fun callResource() {
        val token = _state.value.sessionToken ?: return
        try {
            val resp = withContext(Dispatchers.IO) { Api.getResource(sessionToken = token) }
            _state.value = _state.value.copy(lastResponse = resp, status = "Resource OK")
        } catch (e: Exception) {
            _state.value = _state.value.copy(status = "Resource error: ${e.message}")
        }
    }

    suspend fun logout() {
        val token = _state.value.sessionToken ?: return
        try {
            withContext(Dispatchers.IO) { Api.logout(sessionToken = token) }
            _state.value = UiState(status = "Logged out")
        } catch (e: Exception) {
            _state.value = _state.value.copy(status = "Logout error: ${e.message}")
        }
    }

    private fun openLoginInBrowser(url: String) {
        CustomTabsIntent.Builder().build().launchUrl(activity, url.toUri())

    }
}
