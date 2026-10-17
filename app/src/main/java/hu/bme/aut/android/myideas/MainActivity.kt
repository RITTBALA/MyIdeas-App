package hu.bme.aut.android.myideas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.graphics.toColorInt
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import hu.bme.aut.android.myideas.data.local.AppDatabase
import hu.bme.aut.android.myideas.data.repository.IdeaRepository
import hu.bme.aut.android.myideas.ui.screens.IdeaBoardScreen
import hu.bme.aut.android.myideas.ui.theme.MyIdeasTheme
import hu.bme.aut.android.myideas.ui.viewmodel.IdeaBoardViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { IdeaRepository(database.ideaDao(), database.projectDao()) }
    private val viewModel: IdeaBoardViewModel by viewModels {
        IdeaBoardViewModel.Factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install System Splash Screen (Android 12+ / androidx.core.splashscreen)
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Set dark window background for cold start
        val darkBgColor = "#0D1117".toColorInt()
        window.decorView.setBackgroundColor(darkBgColor)

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        insetsController.isAppearanceLightNavigationBars = false

        enableEdgeToEdge()

        setContent {
            MyIdeasTheme {
                IdeaBoardScreen(viewModel = viewModel)
            }
        }
    }
}
