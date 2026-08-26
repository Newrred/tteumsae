package com.tteumsae.app

import android.content.Context
import androidx.room.Room
import com.tteumsae.app.data.local.RoomDatabaseTransactionRunner
import com.tteumsae.app.data.local.SavedPlacePreferencesMigration
import com.tteumsae.app.data.local.SharedPreferencesSavedPlaceMigrationPreferences
import com.tteumsae.app.data.local.TteumsaeDatabase
import com.tteumsae.app.data.saved.RoomSavedPlacesRepository
import com.tteumsae.app.data.saved.SavedPlacesRepository

class AppContainer(context: Context) {
    val database: TteumsaeDatabase = Room.databaseBuilder(
        context.applicationContext,
        TteumsaeDatabase::class.java,
        TteumsaeDatabase.NAME,
    ).build()

    private val transactionRunner = RoomDatabaseTransactionRunner(database)

    val savedPlacesRepository: SavedPlacesRepository = RoomSavedPlacesRepository(
        dao = database.savedPlaceDao(),
        transactionRunner = transactionRunner,
    )

    val savedPlacePreferencesMigration = SavedPlacePreferencesMigration(
        preferences = SharedPreferencesSavedPlaceMigrationPreferences(context.applicationContext),
        dao = database.savedPlaceDao(),
        transactionRunner = transactionRunner,
    )
}
