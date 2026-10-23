package ro.priceputii.app.ui.components

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.Timestamp
import ro.priceputii.app.data.rankFor
import ro.priceputii.app.ui.theme.BrandOrangeSoft
import java.text.SimpleDateFormat
import java.util.Locale

fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.dial(phone: String) {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}

fun Context.sms(phone: String) {
    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone")))
}

fun formatLei(price: Long): String = "%,d lei".format(Locale("ro", "RO"), price)

/** "acum 5 min", "acum 3 h", "ieri", or a date. */
fun timeAgo(timestamp: Timestamp?, now: Long = System.currentTimeMillis()): String {
    if (timestamp == null) return "acum"
    val millis = timestamp.toDate().time
    val minutes = (now - millis) / 60_000
    return when {
        minutes < 1 -> "acum"
        minutes < 60 -> "acum $minutes min"
        minutes < 24 * 60 -> "acum ${minutes / 60} h"
        minutes < 48 * 60 -> "ieri"
        else -> SimpleDateFormat("d MMM", Locale("ro", "RO")).format(timestamp.toDate())
    }
}

fun clockTime(timestamp: Timestamp?): String =
    timestamp?.let { SimpleDateFormat("HH:mm", Locale("ro", "RO")).format(it.toDate()) }.orEmpty()

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/** Spinner sized to sit inside a filled button while it works. */
@Composable
fun ButtonProgress() {
    CircularProgressIndicator(
        modifier = Modifier.size(20.dp),
        color = LocalContentColor.current,
        strokeWidth = 2.dp,
    )
}

@Composable
fun EmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

/** Small pill with the Priceput's rank, shown next to names. */
@Composable
fun RankBadge(starsTotal: Long, ratingCount: Long) {
    Surface(color = BrandOrangeSoft, shape = MaterialTheme.shapes.small) {
        Text(
            rankFor(starsTotal, ratingCount).label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}
