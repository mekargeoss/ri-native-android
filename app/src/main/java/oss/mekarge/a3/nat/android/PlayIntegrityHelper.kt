/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import android.content.Context
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object PlayIntegrityHelper {
    suspend fun requestIntegrityToken(ctx: Context, nonce: String): String? {
        val mgr = IntegrityManagerFactory.create(ctx)
        val request = IntegrityTokenRequest.builder()
            .setNonce(nonce)
            .setCloudProjectNumber(Config.CLOUD_PROJECT_NUMBER)
            .build()

        return suspendCancellableCoroutine { cont ->
            mgr.requestIntegrityToken(request)
                .addOnSuccessListener { resp -> cont.resume(resp.token()) }
                .addOnFailureListener { error ->
                    cont.resume(null)
                }
        }
    }
}
