package com.museerodin.companion

import android.app.Application
import com.museerodin.companion.content.ContentAssetDataSource
import com.museerodin.companion.content.ContentRepository
import com.museerodin.companion.narration.AndroidTextToSpeechEngine
import com.museerodin.companion.narration.NarrationController
import com.museerodin.companion.narration.NoOpNarrationEngine
import com.museerodin.companion.user.UserDatabase
import com.museerodin.companion.user.UserRepository

class MuseeRodinApplication : Application() {
    lateinit var contentRepository: ContentRepository
        private set
    lateinit var userRepository: UserRepository
        private set
    lateinit var narrationController: NarrationController
        private set

    override fun onCreate() {
        super.onCreate()
        contentRepository = ContentAssetDataSource(assets).load()
        userRepository = UserRepository(UserDatabase.create(this).userDao())
        val engine = if (isInstrumentationRun()) NoOpNarrationEngine() else AndroidTextToSpeechEngine(this)
        narrationController = NarrationController(engine)
    }

    override fun onTerminate() {
        narrationController.shutdown()
        super.onTerminate()
    }

    private fun isInstrumentationRun(): Boolean {
        return try {
            Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }
}
