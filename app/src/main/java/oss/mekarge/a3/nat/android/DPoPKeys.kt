/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import java.util.Base64

object DPoPKeys {
    private const val ALIAS = "app_dpop_ec_p256"

    fun ensureKeypair() {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (ks.containsAlias(ALIAS)) return

        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(false)
            .build()
        kpg.initialize(spec)
        kpg.generateKeyPair()
    }

    private fun getPrivateKey(): PrivateKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        return entry.privateKey
    }

    private fun getPublicKey(): ECPublicKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val entry = ks.getEntry(ALIAS, null) as KeyStore.PrivateKeyEntry
        return entry.certificate.publicKey as ECPublicKey
    }

    fun publicJwk(): Map<String, String> {
        val pub = getPublicKey()
        val w = pub.w
        val x = toFixed32(w.affineX.toByteArray())
        val y = toFixed32(w.affineY.toByteArray())
        return mapOf(
            "kty" to "EC",
            "crv" to "P-256",
            "x" to b64url(x),
            "y" to b64url(y)
        )
    }

    fun signSigningInput(signingInput: ByteArray): ByteArray {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(getPrivateKey())
        sig.update(signingInput)
        val der = sig.sign()
        return Ecdsa.derToJose(der, 32)
    }

    private fun toFixed32(bytes: ByteArray): ByteArray {
        val b = if (bytes.size > 32) bytes.copyOfRange(bytes.size - 32, bytes.size) else bytes
        if (b.size == 32) return b
        val out = ByteArray(32)
        System.arraycopy(b, 0, out, 32 - b.size, b.size)
        return out
    }

    private fun b64url(data: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(data)
}
