/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object Config {
    const val CLOUD_PROJECT_NUMBER: Long = 0L
    val ATTESTATION_SERVER: HttpUrl = "https://10.0.2.2:8000".toHttpUrl()
    const val DEEP_LINK_SCHEME: String = "app"
    const val DEEP_LINK_HOST: String = "finish"
}
