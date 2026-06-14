package com.example.whereami
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.tooling.preview.Preview



@Composable
fun AdvancedParametersScreen() {

    var stealthMode by remember { mutableStateOf(true) }
    var aiHunters by remember { mutableStateOf(false) }
    var voiceDistance by remember { mutableFloatStateOf(50f) }

    var selectedMultiplier by remember { mutableStateOf("2.0x") }
    var timeLimit by remember { mutableFloatStateOf(45f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        TopBar()

        Spacer(modifier = Modifier.height(12.dp))

        StepHeader()

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Advanced Parameters",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Fine-tune the hunt mechanics for your squad. These settings affect final score multipliers and gameplay difficulty.",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(20.dp))

        ParameterCard(
            title = "Stealth Mode",
            description = "Hide participant icons from the global map.",
            checked = stealthMode,
            onCheckedChange = { stealthMode = it }
        )

        VoiceChatCard(
            value = voiceDistance,
            onValueChange = { voiceDistance = it }
        )

        ScoreMultiplierCard(
            selectedMultiplier = selectedMultiplier,
            onMultiplierSelected = { selectedMultiplier = it }
        )

        ParameterCard(
            title = "AI Hunters",
            description = "Enable NPCs",
            checked = aiHunters,
            onCheckedChange = { aiHunters = it }
        )

        TimeLimitCard(
            timeLimit = timeLimit,
            onTimeLimitChange = { timeLimit = it }
        )

        ProTipCard()

        Spacer(modifier = Modifier.height(16.dp))

        BottomButtons()
    }
}

@Composable
fun TopBar() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0B63F6)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Game Setup",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = Color(0xFF0B63F6),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun StepHeader() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STEP 3 OF 3",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B63F6)
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Finalizing Configuration",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B63F6)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(Color(0xFF0B63F6), RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(Color(0xFF0B63F6), RoundedCornerShape(50))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(Color(0xFF0B63F6), RoundedCornerShape(50))
            )
        }
    }
}

@Composable
fun ParameterCard(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF0B63F6),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun VoiceChatCard(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color(0xFF0B63F6),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Chat Proximity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Radius where team communication is active.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${value.toInt()}m",
                    color = Color(0xFF0B63F6),
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..100f
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TIGHT", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text("WIDE", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ScoreMultiplierCard(
    selectedMultiplier: String,
    onMultiplierSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF0B63F6),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Score Multipliers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "Adjust points for difficulty scaling.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MultiplierButton(
                        value = "1.5x",
                        label = "TIMES BONUS",
                        selected = selectedMultiplier == "1.5x",
                        onClick = { onMultiplierSelected("1.5x") }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    MultiplierButton(
                        value = "2.0x",
                        label = "HARDCORE",
                        selected = selectedMultiplier == "2.0x",
                        onClick = { onMultiplierSelected("2.0x") }
                    )
                }
            }
        }
    }
}

@Composable
fun MultiplierButton(
    value: String,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF0B63F6) else Color(0xFFF1F4FA),
            contentColor = if (selected) Color.White else Color(0xFF0B63F6)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun TimeLimitCard(
    timeLimit: Float,
    onTimeLimitChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFF0B63F6),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Time Limit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        text = "${timeLimit.toInt()} Mins",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Slider(
                value = timeLimit,
                onValueChange = onTimeLimitChange,
                valueRange = 15f..120f,
                steps = 6
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("15 min", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                Text("120 min", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun ProTipCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF2FF))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = Color(0xFF0B63F6),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "Pro Tip",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B63F6)
                )

                Text(
                    text = "Activating 'Hardcore' mode disables HUD elements but triples your Global Ranking points for this session.",
                    fontSize = 12.sp,
                    color = Color(0xFF214B8F)
                )
            }
        }
    }
}

@Composable
fun BottomButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {println("Back clicked")},// pr voir ça marche bien
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFEDEDED),
                contentColor = Color.Black
            )
        ) {
            Text("Back")
        }

        Button(
            onClick = {println("Start Hunt clicked")},
            modifier = Modifier
                .weight(2f)
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0B63F6),
                contentColor = Color.White
            )
        ) {
            Text("Start Hunt")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdvancedParametersScreenPreview() {
    AdvancedParametersScreen()
}