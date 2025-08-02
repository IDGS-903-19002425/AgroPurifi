package com.android.agropurrifi

import androidx.annotation.DrawableRes

sealed class Destinations (val ruta : String, val label : String, @DrawableRes val iconoId : Int? = null){
    object dashboardScreen : Destinations("dashboard", "Dashboard", null)
    object phScreen : Destinations("ph", "pH", R.drawable.ph_agua2)
    object turbidezScreen : Destinations("turbidez", "Turbidez", R.drawable.turb_agua)
    object filtrosScreen : Destinations("filtros", "Filtros", null)


    companion object{
        val items = listOf(dashboardScreen, phScreen, turbidezScreen, filtrosScreen)
    }
}