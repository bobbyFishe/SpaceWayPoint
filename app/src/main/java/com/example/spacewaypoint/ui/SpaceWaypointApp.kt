import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spacewaypoint.data.GameDifficulty
import com.example.spacewaypoint.ui.AppScreen
import com.example.spacewaypoint.ui.ComplexityBlog
import com.example.spacewaypoint.ui.StartScreen
import com.example.spacewaypoint.ui.TrainingViewModel
import com.example.spacewaypoint.ui.TrainingViewModelFactory


enum class TrainingAppScreen{
    Start,
    Complexity,
    Game,
    End
}

@Composable
fun SpaceWaypointApp(
    onExit: () -> Unit,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = TrainingAppScreen.Start.name,
        modifier = modifier
    ) {
        composable(route = TrainingAppScreen.Start.name) {
            StartScreen(
                onNextButtonClicked = {
                    navController.navigate(TrainingAppScreen.Complexity.name)
                }
            )
        }
        composable(route = TrainingAppScreen.Complexity.name) {
            ComplexityBlog(
                onDifficultySelected = { difficulty->
                    navController.navigate("${TrainingAppScreen.Game.name}/${difficulty.name}")
                }
            )
        }
        composable(
            route = "${TrainingAppScreen.Game.name}/{difficulty}",
            arguments = listOf(navArgument("difficulty") {type = NavType.StringType})
            ) { backStackEntry ->
                val difficultyName = backStackEntry.arguments?.getString("difficulty")
                val difficulty = GameDifficulty.valueOf(difficultyName ?: GameDifficulty.EASY.name)
                val viewModel: TrainingViewModel = viewModel(
                    factory = TrainingViewModelFactory(difficulty)
                )
                BackHandler {
                    navController.popBackStack(
                        route = TrainingAppScreen.Complexity.name,
                        inclusive = false
                    )
                }
                AppScreen(
                    viewModel = viewModel,
                    onExit = onExit,
                    navController = navController
                )
        }
    }
}