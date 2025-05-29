package org.kabiri.android.usbterminal.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.kabiri.android.usbterminal.model.UserSettingPreferences

private const val TEST_DATA_STORE_NAME = "test_data_store"
@RunWith(AndroidJUnit4::class)
internal class UserSettingRepositoryAndroidTest {

    private val testCoroutineDispatcher: TestDispatcher = StandardTestDispatcher()
    private val testCoroutineScope = TestScope(testCoroutineDispatcher + Job())
    private val testContext = ApplicationProvider.getApplicationContext<Context>()

    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATA_STORE_NAME) }
        )

    private val repository: UserSettingRepository = UserSettingRepository(testDataStore)

    @Test
    fun testFetchInitialPreferences() {

        // arrange
        val expected = UserSettingPreferences()
        var actual: UserSettingPreferences? = null

        // act
        testCoroutineScope.runTest {
            actual = repository.fetchInitialPreferences()
        }

        // assert
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testWriteBaudRate() {

        // arrange
        val customBaudRate = 123
        val expected = UserSettingPreferences(
            baudRate = customBaudRate
        )
        var actual: UserSettingPreferences? = null

        // act
        testCoroutineScope.runTest {
            repository.setBaudRate(customBaudRate)
            actual = repository.preferenceFlow.first()
        }

        // assert
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun testClearResetsValuesToDefaults() {

        // arrange
        val customBaudRate = 123
        val expected = UserSettingPreferences()
        var actual: UserSettingPreferences? = null

        // act
        testCoroutineScope.runTest {
            repository.setBaudRate(customBaudRate)
            repository.clear()
            actual = repository.preferenceFlow.first()
        }

        // assert
        assertThat(actual).isEqualTo(expected)
    }
}