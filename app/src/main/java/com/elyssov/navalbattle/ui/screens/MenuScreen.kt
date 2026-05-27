package com.elyssov.navalbattle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.GameViewModel
import com.elyssov.navalbattle.game.Screen
import com.elyssov.navalbattle.ui.theme.RadarAmber
import com.elyssov.navalbattle.ui.theme.SeaBackground
import com.elyssov.navalbattle.ui.theme.SeaSurface

@Composable
fun MenuScreen(vm: GameViewModel) {
    val lang by vm.lang.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(SeaBackground)) {
        // Splash art background
        Image(
            painter = painterResource(R.drawable.splash_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().alpha(0.55f)
        )
        // Dark gradient overlay for text readability
        Box(modifier = Modifier.fillMaxSize().background(SeaBackground.copy(alpha = 0.35f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language picker
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                LangChip("EN", lang == "en") { vm.setLang("en") }
                Spacer(Modifier.padding(horizontal = 4.dp))
                LangChip("RU", lang == "ru") { vm.setLang("ru") }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.menu_title),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(R.string.menu_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 320.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { vm.startNewGame() },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.menu_new_game)) }

                Button(
                    onClick = { vm.navigate(Screen.Settings) },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.menu_settings)) }

                Button(
                    onClick = { vm.navigate(Screen.Help) },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.menu_help)) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LangChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else SeaSurface
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val border = if (selected) RadarAmber else Color.Transparent
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(label, color = fg, fontWeight = FontWeight.Bold)
    }
}
