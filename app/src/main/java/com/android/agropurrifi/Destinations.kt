package com.android.agropurrifi

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Destinations (val ruta : String, val label : String, @DrawableRes val iconoId : Int){
    object phScreen : Destinations("ph", "PH del agua", R.drawable.ph_agua2)
    object turbidezScreen : Destinations("turbidez", "Turbidez del agua", R.drawable.turb_agua)

    companion object{
        val items = listOf(phScreen, turbidezScreen)

    }

}