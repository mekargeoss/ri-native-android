/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    private val vm by lazy { AuthViewModel(applicationContext, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.data?.let { vm.handleDeepLink(it) }

        setContent { AppScreen(vm) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let { vm.handleDeepLink(it) }
    }

}
