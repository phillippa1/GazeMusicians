package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test.ui.theme.TestTheme


// ToDo- Songs need to be moved down
// ToDo- A switch for changing the gaze interaction methods
// Documentation- there is a calibration function that starts the user calibration


// Load Inter font
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_regular, FontWeight.SemiBold)
)

// Mapping the songs to numbers:
val songMap = mapOf(
    "Song 1" to 1,
    "Song 2" to 2,
    "Song 3" to 3,
    "Song 4" to 4,
    "Song 5" to 5
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "songList") {
                    composable("songList") {
                        SongListScreen(navController = navController)
                    }
                    // Route to each song
                    composable("song/{songNumber}") { backStackEntry ->
                        val songNumber = backStackEntry.arguments?.getString("songNumber")?.toIntOrNull() ?: 1
                        MusicPage(navController = navController, songNumber = songNumber)
                    }
                }  // Added: Closing brace for NavHost
            }  // Added: Closing brace for TestTheme
        }  // Added: Closing brace for setContent
    }
}

@Composable
fun SongListScreen(navController: NavController) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),  // Makes it centered, need to think about screen size
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SongList(navController = navController)
        }
    }
}

@Composable
fun SongList(navController: NavController) {
    val songs = listOf("Song 1", "Song 2", "Song 3", "Song 4", "Song 5")

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        songs.forEach { songName ->
            SongButton(songName = songName, onClick = {
                val songNumber = songMap[songName] ?: 1
                navController.navigate("song/$songNumber")
            })
        }
    }
}

// Button for each song
@Composable
fun SongButton(songName: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .width(307.dp)
            .height(98.dp),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, Color.Black),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = songName,
            style = TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                lineHeight = 36.sp
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        SongList(rememberNavController())
    }
}
