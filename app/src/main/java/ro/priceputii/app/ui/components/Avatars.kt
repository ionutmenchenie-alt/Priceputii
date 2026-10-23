package ro.priceputii.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ro.priceputii.app.R

/** The avatars people pick at sign-up; the key is what's stored in Firestore. */
object Avatars {
    val all = listOf(
        "a1" to R.drawable.avatar_1,
        "a2" to R.drawable.avatar_2,
        "a3" to R.drawable.avatar_3,
        "a4" to R.drawable.avatar_4,
        "a5" to R.drawable.avatar_5,
    )

    fun image(key: String): Int? = all.firstOrNull { it.first == key }?.second
}

/** Round avatar; falls back to the name's first letter for people who haven't picked one. */
@Composable
fun Avatar(key: String, name: String, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val image = Avatars.image(key)
    if (image != null) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape),
        )
    } else {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier.size(size)) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
fun AvatarPicker(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Avatars.all.forEach { (key, image) ->
            val isSelected = key == selected
            Image(
                painter = painterResource(image),
                contentDescription = "Avatar ${key.drop(1)}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .border(
                        if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.secondary)
                        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        CircleShape,
                    )
                    .clickable(role = Role.RadioButton) { onSelect(key) },
            )
        }
    }
}
