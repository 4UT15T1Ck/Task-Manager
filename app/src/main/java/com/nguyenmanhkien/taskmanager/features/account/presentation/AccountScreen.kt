package com.nguyenmanhkien.taskmanager.features.account.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.nguyenmanhkien.taskmanager.features.account.presentation.components.AuthButton
import org.koin.androidx.compose.koinViewModel

// TODO: fix this ugly screen
@Composable
fun AuthScreen(
    navController: NavController,
    viewModel: AccountViewModel = koinViewModel()
) {
    val context = LocalContext.current

    val state = viewModel.state.value

//    Scaffold { innerPadding ->

//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding),
//            contentAlignment = Alignment.Center
//        ) {
//            if (state.loading) {
//                CircularProgressIndicator()
//            } else {
//                AuthButton(
//                    text = "Login",
//                    onClick = {
//                        viewModel.onEvent(AccountEvent.LoginEvent(context))
//                    }
//                )
//            }
//
//            if (state.success) {
//                // TODO: navigate to setting
//            }
//
//            state.error?.let {
//                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
//            }
//        }
//    }

}
