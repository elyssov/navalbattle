package com.elyssov.navalbattle.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import com.elyssov.navalbattle.R
import com.elyssov.navalbattle.game.ShipType

@Composable
fun rememberShipSprites(): Map<ShipType, ImageBitmap> {
    val patrol = ImageBitmap.imageResource(R.drawable.patrol)
    val mrk = ImageBitmap.imageResource(R.drawable.mrk)
    val dstr = ImageBitmap.imageResource(R.drawable.dstr)
    val crsr = ImageBitmap.imageResource(R.drawable.crsr)
    val hwcrsr = ImageBitmap.imageResource(R.drawable.hwcrsr)
    val carrier = ImageBitmap.imageResource(R.drawable.carrier)
    val submarine = ImageBitmap.imageResource(R.drawable.submarine)
    return remember(patrol, mrk, dstr, crsr, hwcrsr, carrier, submarine) {
        mapOf(
            ShipType.MissileBoat to patrol,
            ShipType.Mrk to mrk,
            ShipType.Destroyer to dstr,
            ShipType.Cruiser to crsr,
            ShipType.Tarkr to hwcrsr,
            ShipType.Carrier to carrier,
            ShipType.Submarine to submarine
        )
    }
}
