package dev.lucasangelo.voxpopuli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.lucasangelo.voxpopuli.ui.VoxPopuliTheme
import dev.lucasangelo.voxpopuli.ui.theme.App

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoxPopuliTheme {
                App()
            }
        }
    }
}