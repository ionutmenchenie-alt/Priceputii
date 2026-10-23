package ro.priceputii.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ro.priceputii.app.ui.PriceputiiApp
import ro.priceputii.app.ui.theme.PriceputiiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PriceputiiTheme {
                PriceputiiApp()
            }
        }
    }
}
