/**
 * 2026 Mekarge OSS and Maintainers
 * Licensed under the MIT License. See LICENSE file in the project root
 * for full license information.
 */

package oss.mekarge.a3.nat.android

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun AppScreen(vm: AuthViewModel) {
    val scope = rememberCoroutineScope()
    val state by vm.state.collectAsState()

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("MekargeApp", style = MaterialTheme.typography.headlineLarge)
                Text(state.status, style = MaterialTheme.typography.bodySmall)

                Button(
                    onClick = { scope.launch { vm.login() } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.isLoggedIn) "Re-login" else "Login") }

                OutlinedButton(
                    onClick = { scope.launch { vm.callResource() } },
                    enabled = state.isLoggedIn,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Call /api/resource") }

                OutlinedButton(
                    onClick = { scope.launch { vm.logout() } },
                    enabled = state.isLoggedIn,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Logout") }

                state.lastResponse?.let { resp ->
                    Text("Last Response:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        resp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
