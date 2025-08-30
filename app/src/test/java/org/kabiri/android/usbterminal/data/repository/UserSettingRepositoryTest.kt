package org.kabiri.android.usbterminal.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.kabiri.android.usbterminal.model.UserSettingPreferences

private const val TEST_DATA_STORE_NAME = "test_data_store.preferences_pb"

@OptIn(ExperimentalCoroutinesApi::class)
internal class UserSettingRepositoryTest {
    private val dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher()

    @get:Rule
    val tempDir = TemporaryFolder()

    private lateinit var testDataStore: DataStore<Preferences>

    private lateinit var repository: UserSettingRepository

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        tempDir.create()
        testDataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(dispatcher),
                produceFile = { tempDir.newFile(TEST_DATA_STORE_NAME) },
            )
        repository = UserSettingRepository(testDataStore)
    }

    @After
    fun cleanUp() {
        tempDir.delete()
    }

    @Test
    fun testFetchInitialPreferences() =
        runTest {
            // arrange
            val expected = UserSettingPreferences()

            // act
            val actual = repository.fetchInitialPreferences()

            // assert
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun testWriteBaudRate() =
        runTest {
            // arrange
            val customBaudRate = 123
            val expected =
                UserSettingPreferences(
                    baudRate = customBaudRate,
                )

            // act
            repository.setBaudRate(customBaudRate)

            // assert
            val actual = repository.preferenceFlow.first()
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun testSetAutoScrollUpdatesPreferences() =
        runTest {
            // arrange
            val expected = false

            // act
            repository.setAutoScroll(false)

            // assert
            val actual = repository.preferenceFlow.first().autoScroll
            assertThat(actual).isEqualTo(expected)
        }

    @Test
    fun testClearResetsValuesToDefaults() =
        runTest {
            // arrange
            val customBaudRate = 123
            val expected = UserSettingPreferences()

            // act
            repository.setBaudRate(customBaudRate)
            repository.clear()

            // assert
            val actual = repository.preferenceFlow.first()
            assertThat(actual).isEqualTo(expected)
        }
}
