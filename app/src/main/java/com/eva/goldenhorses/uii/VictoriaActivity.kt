package com.eva.goldenhorses.uii

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import android.app.Activity
import android.content.ContentResolver
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import java.util.TimeZone


class VictoriaActivity : ComponentActivity() {

    var currentCapturedBitmap: Bitmap? = null
    lateinit var createImageLauncher: ActivityResultLauncher<String>
    private val PERMISO_CALENDARIO = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nombreJugador = intent.getStringExtra("jugador_nombre") ?: "Jugador"
        val caballoPalo = intent.getStringExtra("jugador_palo") ?: "Oros"
        val caballoGanador = intent.getStringExtra("caballo_ganador") ?: ""

        createImageLauncher =
            registerForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
                uri?.let {
                    currentCapturedBitmap?.let { bitmap ->
                        contentResolver.openOutputStream(uri)?.use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }
                    }
                }
            }

        setContent {
            VictoriaScreen(caballoPalo = caballoPalo, nombreJugador = nombreJugador)
        }

        if (caballoPalo == caballoGanador) {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_CALENDAR
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val calendarId = obtenerCalendarioValido(contentResolver)
                if (calendarId != null) {
                    insertarEventoCalendario(
                        calendarId = calendarId,
                        titulo = "Victoria en Golden Horses",
                        descripcion = "¡$nombreJugador ha ganado una partida apostando por $caballoPalo!"
                    )
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.READ_CALENDAR,
                        android.Manifest.permission.WRITE_CALENDAR
                    ),
                    PERMISO_CALENDARIO
                )
            }
        }
    }

    fun captureScreen(): Bitmap {
        val view = this.window.decorView.rootView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun insertarEventoCalendario(calendarId: Long, titulo: String, descripcion: String) {
        val eventsUri = CalendarContract.Events.CONTENT_URI

        Thread {
            try {
                val startMillis = System.currentTimeMillis()
                val endMillis = startMillis + (60 * 60 * 1000) // 1 hora

                val values = android.content.ContentValues().apply {
                    put(CalendarContract.Events.DTSTART, startMillis)
                    put(CalendarContract.Events.DTEND, endMillis)
                    put(CalendarContract.Events.TITLE, titulo)
                    put(CalendarContract.Events.DESCRIPTION, descripcion)
                    put(CalendarContract.Events.CALENDAR_ID, calendarId)
                    put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                }

                val result = contentResolver.insert(eventsUri, values)
                android.util.Log.d("CALENDAR_EVENT", "Evento insertado: $result")
            } catch (e: Exception) {
                android.util.Log.e("CALENDAR_EVENT", "Error insertando evento: ${e.message}")
            }
        }.start()
    }


    fun obtenerCalendarioValido(contentResolver: ContentResolver): Long? {
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.IS_PRIMARY
        )

        val cursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val nombre = it.getString(1)
                val owner = it.getString(3)
                val esPrimario = it.getInt(4)

                android.util.Log.d(
                    "CALENDAR_LIST",
                    "ID: $id, Nombre: $nombre, Owner: $owner, Primario: $esPrimario"
                )

                if (esPrimario == 1 || nombre.contains("gmail", ignoreCase = true)) {
                    return id
                }
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISO_CALENDARIO && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val nombreJugador = intent.getStringExtra("jugador_nombre") ?: "Jugador"
            val caballoPalo = intent.getStringExtra("jugador_palo") ?: "Oros"
            val caballoGanador = intent.getStringExtra("caballo_ganador") ?: ""

            if (caballoPalo == caballoGanador) {
                val calendarId = obtenerCalendarioValido(contentResolver)
                if (calendarId != null) {
                    insertarEventoCalendario(
                        calendarId = calendarId,
                        titulo = "Victoria en Golden Horses",
                        descripcion = "¡$nombreJugador ha ganado una partida apostando por $caballoPalo!"
                    )
                }
            }
        }
    }


    @Composable
    fun VictoriaScreen(caballoPalo: String, nombreJugador: String) {
        val context = LocalContext.current

        val icono = when (caballoPalo) {
            "Oros" -> R.drawable.cab_oros
            "Copas" -> R.drawable.cab_copas
            "Espadas" -> R.drawable.cab_espadas
            "Bastos" -> R.drawable.cab_bastos
            else -> R.drawable.mazo
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fondo
            Image(
                painter = painterResource(id = R.drawable.fondo_victoria),
                contentDescription = "Fondo victoria",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            // Caja de contenido centrado con fondo blanco semitransparente
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(max = 450.dp)
                    .background(Color.White.copy(alpha = 0.8f))
                    .align(Alignment.Center)
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("¡Has ganado!", fontSize = 28.sp)

                    Spacer(modifier = Modifier.height(24.dp))

                    Image(
                        painter = painterResource(id = icono),
                        contentDescription = "Tu caballo ganador",
                        modifier = Modifier.size(120.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Image(
                        painter = painterResource(id = R.drawable.volver_jugar),
                        contentDescription = "Volver a Jugar",
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .clickable {
                                val intent =
                                    Intent(context, PlayerSelectionActivity::class.java).apply {
                                        putExtra("jugador_nombre", nombreJugador)
                                    }
                                context.startActivity(intent)
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Image(
                        painter = painterResource(id = R.drawable.volver_inicio),
                        contentDescription = "Volver a Inicio",
                        modifier = Modifier
                            .fillMaxWidth(0.55f)
                            .clickable {
                                SessionManager.guardarJugador(context, nombreJugador)
                                val intent = Intent(context, HomeActivity::class.java)
                                context.startActivity(intent)
                            }
                    )

                }
            }

            Spacer(modifier = Modifier.height(300.dp))

            Image(
                painter = painterResource(id = R.drawable.boton_captura),
                contentDescription = "Guardar captura",
                modifier = Modifier
                    .align(Alignment.Center) // centrado en pantalla
                    .offset(y = 300.dp)
                    .fillMaxWidth(0.55f)
                    .clickable {
                        val activity = context as? VictoriaActivity
                        activity?.let {
                            it.currentCapturedBitmap = it.captureScreen()
                            it.createImageLauncher.launch("victoria_${System.currentTimeMillis()}.png")
                        }
                    }
            )
        }
    }
}


/*@Preview(showBackground = true)
@Composable
fun PreviewVictoriaScreen() {
    VictoriaScreen(caballoPalo = "Oros", nombreJugador = "JugadorDemo")
}
*/