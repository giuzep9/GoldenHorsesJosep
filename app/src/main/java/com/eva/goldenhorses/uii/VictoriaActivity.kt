package com.eva.goldenhorses.uii

import android.content.Context
import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.goldenhorses.R
import com.eva.goldenhorses.SessionManager
import com.eva.goldenhorses.utils.aplicarIdioma
import com.eva.goldenhorses.utils.obtenerIdioma
import androidx.compose.ui.res.stringResource
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.graphics.Canvas
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.eva.goldenhorses.repository.JugadorRepository
import com.eva.goldenhorses.viewmodel.JugadorViewModel
import com.eva.goldenhorses.viewmodel.JugadorViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.TimeZone


class VictoriaActivity : ComponentActivity() {

    var currentCapturedBitmap: Bitmap? = null
    lateinit var createImageLauncher: ActivityResultLauncher<String>
    private val PERMISO_CALENDARIO = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val caballoPalo = intent.getStringExtra("jugador_palo") ?: "Oros"
        val caballoGanador = intent.getStringExtra("caballo_ganador") ?: ""

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val repository = JugadorRepository()
        val factory = JugadorViewModelFactory(repository)
        val jugadorViewModel = factory.create(JugadorViewModel::class.java)
        if (uid != null) jugadorViewModel.comprobarJugadorPorUid(uid)

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

        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isMusicMuted = sharedPreferences.getBoolean("isMusicMuted", false)

        if (!isMusicMuted) {
            val mediaPlayer = MediaPlayer.create(this, R.raw.victoria)
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        }

        setContent {
            val jugador by jugadorViewModel.jugador.collectAsState()
            jugador?.let {
                VictoriaScreen(caballoPalo = caballoPalo, nombreJugador = it.nombre)
            }
        }

        if (caballoPalo == caballoGanador) {
            if (
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
            ) {
                val calendarId = obtenerCalendarioValido(contentResolver)
                if (calendarId != null) {
                    jugadorViewModel.jugador.value?.let { jugador ->
                        insertarEventoCalendario(
                            calendarId = calendarId,
                            titulo = "Victoria en Golden Horses",
                            descripcion = "¡${jugador.nombre} ha ganado una partida apostando por $caballoPalo!"
                        )
                    }
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

        val tiempoResolucionMs = intent.getLongExtra("tiempo_resolucion", 0L)
        if (tiempoResolucionMs > 0L) mostrarNotificacionVictoria(tiempoResolucionMs)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    999
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

        if (requestCode == 999 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            val tiempoMs = intent.getLongExtra("tiempo_resolucion", 0L)
            if (tiempoMs > 0L) {
                mostrarNotificacionVictoria(tiempoMs)
            }
        }

        // Ya tenías esto para el calendario
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

    @SuppressLint("ServiceCast")
    private fun mostrarNotificacionVictoria(tiempoMs: Long) {
        val tiempoSegundos = tiempoMs / 1000

        val intent = Intent(this, DetalleVictoriaActivity::class.java).apply {
            putExtra("tiempo_resolucion", tiempoSegundos)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "victoria_channel"
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notificacion)
            .setContentTitle("\uD83C\uDFC6 ¡Victoria!")
            .setContentText("Has ganado en $tiempoSegundos segundos")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Victoria", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(1001, builder.build())
    }

    override fun attachBaseContext(newBase: Context) {
        val context = aplicarIdioma(newBase) // usa tu función LanguageUtils
        super.attachBaseContext(context)
    }
}

@Composable
fun VictoriaScreen(caballoPalo: String, nombreJugador: String) {
    val context = LocalContext.current
    val idioma = obtenerIdioma(context)

    val icono = when (caballoPalo) {
        "Oros" -> R.drawable.cab_oros
        "Copas" -> R.drawable.cab_copas
        "Espadas" -> R.drawable.cab_espadas
        "Bastos" -> R.drawable.cab_bastos
        else -> R.drawable.mazo
    }

    val fondoVictoria = if (idioma == "en") R.drawable.fondo_victory else R.drawable.fondo_victoria
    val botonVolverJugar = if (idioma == "en") R.drawable.boton_replay else R.drawable.volver_jugar
    val botonVolverInicio = if (idioma == "en") R.drawable.boton_home else R.drawable.volver_inicio
    val botonCaptura = if (idioma == "en") R.drawable.boton_screenshot else R.drawable.boton_captura

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo
        Image(
            painter = painterResource(id = fondoVictoria),
            contentDescription = "Fondo victoria",
            modifier = Modifier.fillMaxSize(),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        // Caja de contenido centrado con fondo blanco semitransparente
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .heightIn(max = 425.dp)
                .background(Color.White.copy(alpha = 0.8f))
                .align(Alignment.Center)
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = stringResource(id = R.string.winner), fontSize = 28.sp)

                Spacer(modifier = Modifier.height(24.dp))

                Image(
                    painter = painterResource(id = icono),
                    contentDescription = "Tu caballo ganador",
                    modifier = Modifier.size(120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = botonVolverJugar),
                    contentDescription = "Volver a Jugar",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .clickable {
                            val intent = Intent(context, PlayerSelectionActivity::class.java).apply {
                                putExtra("jugador_nombre", nombreJugador)
                            }
                            context.startActivity(intent)
                        }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Image(
                    painter = painterResource(id = botonVolverInicio),
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

            Spacer(modifier = Modifier.height(300.dp))
        }

        Image(
            painter = painterResource(id = botonCaptura),
            contentDescription = "Guardar captura",
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.7f)
                .offset(y = 310.dp)
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

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewVictoriaScreen() {
    VictoriaScreen(caballoPalo = "Oros", nombreJugador = "Eva")
}
