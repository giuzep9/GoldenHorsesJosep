package com.eva.goldenhorses.uii

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.fondo2), // Usa tu propia imagen de fondo
            contentDescription = "Fondo Login",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            Image(
                painter = painterResource(id = R.drawable.identificate),
                contentDescription = "Identifícate",
                modifier = Modifier
                    .height(400.dp)
                    .padding(bottom = 5.dp)
            )

            // Campo de entrada de nombre
            BasicTextField(
                value = username,
                onValueChange = { username = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 24.sp),
                modifier = Modifier
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(250.dp))

            // Botón con imagen
            Image(
                painter = painterResource(id = R.drawable.boton_inicio), // Imagen del botón
                contentDescription = "Botón Iniciar",
                modifier = Modifier
                    .width(220.dp)
                    .height(150.dp)
                    .clickable {
                        if (username.isNotBlank()) {
                            val intent = Intent(context, HomeActivity::class.java)
                            intent.putExtra("nombre_usuario", username)
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(
                                context,
                                "Por favor, introduce tu nombre",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            )

        }
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen()
}

