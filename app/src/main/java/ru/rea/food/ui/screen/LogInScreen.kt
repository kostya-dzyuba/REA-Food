package ru.rea.food.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.rea.food.*
import ru.rea.food.R
import ru.rea.food.ui.AccountButtons
import ru.rea.food.ui.TextField
import ru.rea.food.ui.topbar.TopAccountBar

@Composable
fun LogInScreen(nav: NavController, onLogIn: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(dimensionResource(id = R.dimen.content_padding)),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopAccountBar(text = stringResource(R.string.skip))
        val paddingModifier = Modifier.padding(start = 16.dp)
        Text(
            text = stringResource(R.string.login_your),
            modifier = paddingModifier,
            style = MaterialTheme.typography.h5
        )
        Text(
            text = stringResource(R.string.greeting),
            modifier = paddingModifier,
        )
        Spacer(modifier = Modifier.height(16.dp))
        var email by remember { mutableStateOf("") }
        var emailError by remember { mutableStateOf("") }
        TextField(
            text = email,
            onTextChange = { email = it },
            label = stringResource(R.string.email),
            title = stringResource(R.string.address),
            error = emailError,
            keyboardType = KeyboardType.Email
        )
        var password by remember { mutableStateOf("") }
        var passwordError by remember { mutableStateOf("") }
        TextField(
            text = password,
            onTextChange = { password = it },
            label = stringResource(R.string.enter_password),
            title = stringResource(R.string.password),
            error = passwordError,
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val deviceId = HardwareId.get()
            val context = LocalContext.current
            AccountButtons(
                onCreate = { nav.navigate("create") },
                onLogin = {
                    REAFoodService.instance.logIn(
                        Account(
                            email = email,
                            password = password,
                            deviceId = deviceId
                        )
                    ).enqueue(object : Callback<AccountResponse> {
                        override fun onResponse(
                            call: Call<AccountResponse>,
                            response: Response<AccountResponse>
                        ) {
                            val body = response.body()!!
                            val token = body.token
                            if (token == "") {
                                emailError = body.errors.email
                                passwordError = body.errors.password
                            } else {
                                onLogIn(token)
                                nav.navigate("restaurants")
                            }
                        }

                        override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                            Toast.makeText(context, t.message, Toast.LENGTH_SHORT).show()
                        }
                    })
                },
                text = stringResource(R.string.login_my),
                google = true,
            )
        }
    }
}