/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    //TODO 01. Create a viewModelJob and overwrite onCleared() for cancelling coroutine
    private var viewModelJob: Job = Job()

    /*
     * By default, all coroutines started in uiScope will launch in [Dispatchers.Main] which is
     * the main thread on Android. This is a sensible default because most coroutines started by
     * a [ViewModel] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight?>()

    private val nights = database.getAllNights()

    init {
        initializeTonight()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            database.getTonight()
        }
    }

    private suspend fun insert(night: SleepNight): Unit {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    /**
     * Executes when the START button is Clicked
     */
    fun onStartTracking() {
        uiScope.launch {
            insert(SleepNight())
            // Transaction finished??
            tonight.value = getTonightFromDatabase()
        }
    }

    /**
     * Executes when the STOP button is Clicked
     */
    fun onStopTracking() {
        uiScope.launch {
            // In Kotlin, return@label syntax is used for specifying
            // which function among several nested ones this statement
            // returns from. In this case, we are specifying to return
            // from launch(), not the lambda
            val oldNight: SleepNight = tonight.value ?: return@launch

            // Update the night in the database to add the end time
            oldNight.endTimeMilli = System.currentTimeMillis()

            update(oldNight)
        }
    }

    /**
     * Executes when the CLEAR button is clicked
     */
    fun onClear() {
        uiScope.launch {
            // Clear the database table
            clear()

            tonight.value = null
        }
    }


    //TODO 02. Define a scope for the coroutines to run in

    //TODO 03. Create tonight live data var and use a coroutine to initialize it from the database
    //TODO 04. Get all nights from the database
    //TODO 05. Add local functions fro insert(), update(), and clear()
    //TODO 06. Implement click handlers for Start, Stop, and Clear buttons using coroutines to do the
    // database work
    //TODO 07. Transfers nights into nightString using formatNights()
}

