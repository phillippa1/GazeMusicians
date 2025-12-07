package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column  // NEW: Added for vertical layout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button  // NEW: Added for buttons
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.test.ui.theme.TestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Wrapped in Column for vertical stacking, added title and buttons
                    Column(modifier = Modifier.padding(innerPadding)) {
                        // NEW: Title at the top
                        Text(text = "Gaze and Guitar", modifier = Modifier.padding(bottom = 16.dp))  // Adjust padding/style as needed

                        // Existing greeting
                        Greeting(name = "Pippa")

                        // Need to add buttons for each song
                        // Also going to need a switch to switch which interaction technique I am using (not needing to be functional for now
                        // Next steps: center buttons and make clickable
                        Button(onClick = { /* Goes to music- need to find music */ }) {
                            Text("Song 1")
                        }
                        Button(onClick = { /* music */ }) {
                            Text("Song 2")
                        }

                        Button(onClick = { /* music */ }) {
                            Text("Song 2")
                        }

                        Button(onClick = { /* music */ }) {
                            Text("Song 2")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        Greeting("Pippa")
    }
}
