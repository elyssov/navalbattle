package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Screen
import com.elyssov.navalbattle.game.Texts
import com.elyssov.navalbattle.ui.theme.HitRed
import com.elyssov.navalbattle.ui.theme.NukeOrange
import com.elyssov.navalbattle.ui.theme.RadarAmber
import com.elyssov.navalbattle.ui.theme.RadarGreen
import com.elyssov.navalbattle.ui.theme.SeaSurface
import kotlinx.coroutines.delay

@Composable
fun NukeRitualScreen(vm: GameViewModel) {
    val lang by vm.lang.collectAsState()
    val ceremony = if (lang == "ru") Texts.nukeCeremonyRu else Texts.nukeCeremonyEn
    val launchLine = if (lang == "ru") Texts.nukeLaunchLineRu else Texts.nukeLaunchLineEn

    var act by remember { mutableIntStateOf(0) }
    val correctCode = remember { Texts.generateLaunchCode() }
    var entered by remember { mutableStateOf("") }
    var aborted by remember { mutableStateOf(false) }
    var launched by remember { mutableStateOf(false) }
    var secondsLeft by remember { mutableIntStateOf(45) }

    LaunchedEffect(act) {
        if (act < 3) return@LaunchedEffect
        while (secondsLeft > 0 && !aborted && !launched) {
            delay(1000)
            secondsLeft -= 1
        }
        if (secondsLeft <= 0 && !launched) aborted = true
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).systemBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (lang == "ru") "Σ☢ ПРИМЕНЕНИЕ СПЕЦИАЛЬНОГО БОЕПРИПАСА" else "Σ☢ NUCLEAR WEAPON RELEASE",
            color = NukeOrange,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SeaSurface)
                .border(2.dp, NukeOrange, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            if (aborted) {
                AbortedBlock(lang)
            } else if (launched) {
                LaunchedBlock(launchLine)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (lang == "ru") "АКТ ${act + 1}/4" else "ACT ${act + 1}/4",
                        color = RadarAmber
                    )
                    Text(
                        ceremony[act],
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (act == 3) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (lang == "ru") "ПУСКОВОЙ КОД (сверьте с опечатанным пакетом):"
                            else "LAUNCH CODE (verify with sealed packet):",
                            color = RadarAmber
                        )
                        Text(
                            correctCode,
                            color = RadarGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = entered,
                            onValueChange = { if (it.length <= 6) entered = it.filter { c -> c.isDigit() } },
                            label = { Text(if (lang == "ru") "Введите код" else "Enter code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true
                        )
                        Text(
                            if (lang == "ru") "Автоблокировка через: $secondsLeft сек."
                            else "Auto-lockout in: $secondsLeft sec.",
                            color = if (secondsLeft <= 10) HitRed else RadarAmber,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = { vm.navigate(Screen.Battle) },
                modifier = Modifier.weight(1f)
            ) { Text(if (lang == "ru") "ОТМЕНА" else "ABORT") }
            if (act < 3) {
                Button(
                    onClick = { act += 1 },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) { Text(if (lang == "ru") "ДАЛЕЕ" else "NEXT") }
            } else if (!launched && !aborted) {
                Button(
                    onClick = {
                        if (entered == correctCode) {
                            launched = true
                        } else {
                            aborted = true
                        }
                    },
                    enabled = entered.length == 6,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(
                        if (lang == "ru") "ПУСК" else "LAUNCH",
                        color = NukeOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = { vm.beginNuclearTargeting() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    enabled = launched
                ) {
                    Text(
                        if (lang == "ru") "ЦЕЛЬ →" else "TARGET →",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AbortedBlock(lang: String) {
    Column {
        Text(
            if (lang == "ru") "КОД НЕ ПОДТВЕРЖДЁН." else "CODE NOT CONFIRMED.",
            color = HitRed,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (lang == "ru")
                "Система безопасности — блокировка. Торпеда деактивирована. Пуск отменён."
            else
                "Safety system engaged. Torpedo deactivated. Launch aborted.",
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LaunchedBlock(launchLine: String) {
    Column {
        Text(launchLine, color = RadarGreen, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Σ☢", color = NukeOrange, fontSize = 64.sp, fontWeight = FontWeight.Bold)
    }
}

