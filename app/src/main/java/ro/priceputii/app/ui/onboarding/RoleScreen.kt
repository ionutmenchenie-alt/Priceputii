package ro.priceputii.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ro.priceputii.app.data.Roles

@Composable
fun RoleScreen(onRoleChosen: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            "Priceputii",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            "Treburi prin gospodărie, făcute de oameni pricepuți din zona ta.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(40.dp))
        Text("Cum vrei să folosești aplicația?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(16.dp))
        RoleCard(
            title = "Sunt Priceput",
            body = "Caut lucrări în zona mea și mă ofer să le fac.",
            container = MaterialTheme.colorScheme.primary,
            content = MaterialTheme.colorScheme.onPrimary,
            onClick = { onRoleChosen(Roles.PRICEPUT) },
        )
        Spacer(Modifier.height(12.dp))
        RoleCard(
            title = "Am o lucrare",
            body = "Postez ce am de făcut și găsesc pe cineva priceput.",
            container = MaterialTheme.colorScheme.secondary,
            content = MaterialTheme.colorScheme.onSecondary,
            onClick = { onRoleChosen(Roles.CLIENT) },
        )
        Text(
            "Poți schimba oricând rolul din Profil.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun RoleCard(title: String, body: String, container: Color, content: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
