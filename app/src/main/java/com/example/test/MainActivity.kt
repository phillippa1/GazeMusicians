package com.example.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.test.ui.theme.TestTheme


// Problems- there is no title (is this a problem, i dont think do)
// -Song buttons are looking good try them on the other phone.
// - Maybe move the buttons down slightly
// - Might be better to do it on my own phone...?
// 



// Load Inter font
val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_regular, FontWeight.SemiBold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp),  // Makes it centered, need to think about screen size
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Title
                        Text(
                            text = "Gaze Guitar",
                            style = TextStyle(
                                fontFamily = InterFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 30.sp,
                                color = Color.Black
                            ),
                            modifier = Modifier.padding(bottom = 32.dp)  // Space below title
                        )
                        // Then add the buttons, calling them songlist
                        SongList()
                    }
                }
            }
        }
    }
}

@Composable
fun SongList() {
    val songs = listOf("Song 1", "Song 2", "Song 3", "Song 4", "Song 5")

    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        songs.forEach { songName ->
            SongButton( songName = songName, onClick = {
                // Need to do something when you click it
                // Now it just prints the songName
                println("Clicked on $songName")
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
        SongList()
    }
}
