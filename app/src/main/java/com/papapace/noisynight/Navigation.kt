package com.papapace.noisynight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.papapace.noisynight.ui.main.InfoScreen
import com.papapace.noisynight.ui.main.MainScreen

@Composable
fun MainNavigation(service: NoiseAudioService) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    modifier = Modifier.fillMaxSize().background(Color.Black),
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            service = service,
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<Info> {
          InfoScreen(
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize()
          )
        }
      },
  )
}
