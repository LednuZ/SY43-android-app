package com.example.whereami



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.whereami.ui.theme.WhereAmITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WhereAmITheme() {
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {

    val friends = remember {

        mutableStateListOf(

            Friend(
                "Lucie",
                "Dupont",
                "Offline",
                true
            ),

            Friend(
                "Marco",
                "Paulo",
                "Active 3 min ago",
                false
            ),

            Friend(
                "Maria",
                "Amari",
                "Online",
                false
            ),

            Friend(
                "Alex",
                "Martin",
                "Recently won a match",
                true
            )
        )
    }

    var searchText by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color(0xFFFDF6FC))
    ) {

        //la "barre nav"

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Game Setup",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Help,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "STEP 1 OF 3",
            color = Color.Blue,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "SELECT FRIENDS",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
            },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            label = {
                Text("Search friends")
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "SUGGESTED FRIENDS",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.weight(1f))

            TextButton(
                onClick = {

                    for (i in friends.indices) {

                        friends[i] =
                            friends[i].copy(
                                selected = true
                            )
                    }
                }
            ) {
                Text("Select All")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            itemsIndexed(friends) { index, friend ->

                ShowFriend(
                    friend = friend,
                    onSelectionChanged = {

                        friends[index] =
                            friends[index].copy(
                                selected = !friends[index].selected
                            )
                    }
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                Text(
                    text = "Invite with a link",
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Share this link to let anyone join your lobby instantly.",
                    color = Color.Gray,
                    fontSize = 12.sp
                    //A FAIRE : share marche pas
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Button(
                        onClick = {}
                    ) {

                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint=Color(0xFF6C63FF)
                        )

                        Spacer(
                            modifier = Modifier.width(4.dp)
                        )

                        Text("Share")
                    }

                    Button(
                        onClick = {}
                    ) {
                        Text("Copy Link")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Continue"
            )
        }
    }
}




@Composable
fun ShowFriend(
    friend: Friend,
    onSelectionChanged: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(50.dp)
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "${friend.name} ${friend.surname}",
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = friend.status,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onSelectionChanged
            ) {

                if (friend.selected) {

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.Blue
                    )

                } else {

                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}