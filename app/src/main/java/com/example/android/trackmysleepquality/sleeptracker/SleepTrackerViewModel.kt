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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
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

    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()

    val navigateToSleepNight: LiveData<SleepNight>
        get() = _navigateToSleepQuality

    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    // Snackbar
    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    val startButtonVisible = Transformations.map(tonight) {
        null == it
    }

    val stopButtonVisible = Transformations.map(tonight) {
        null != it
    }

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    init {
        initializeTonight()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * suspended function should return Deferred
     */
    private suspend fun getTonightFromDatabase(): SleepNight? {
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night 
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
     * The following functions expose database interaction
     * for clickListener
     */
    private fun initializeTonight() {
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
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

            // This alone trigger observable for navigation
            _navigateToSleepQuality.value = oldNight
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
        // Show a snackbar message, because it's friendly.
        _showSnackbarEvent.value = true
    }

    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value  = false
    }
}

