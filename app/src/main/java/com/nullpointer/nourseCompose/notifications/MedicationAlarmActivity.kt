package com.nullpointer.nourseCompose.notifications

import android.os.Bundle
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.nullpointer.nourseCompose.R

class MedicationAlarmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); val id = intent.getLongExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, -1); val name = intent.getStringExtra("reminder_name").orEmpty(); val dosage = intent.getStringExtra("reminder_dosage").orEmpty(); val photo = intent.getStringExtra("reminder_photo"); setContent { val context = LocalContext.current; val bitmap = photo?.let { runCatching { contentResolver.openInputStream(Uri.parse(it))?.use(BitmapFactory::decodeStream) }.getOrNull() }; MaterialTheme { Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text(getString(R.string.title_alarm_now), style = MaterialTheme.typography.headlineMedium); Text(name, style = MaterialTheme.typography.headlineSmall); if (dosage.isNotBlank()) Text(dosage); bitmap?.let { Image(it.asImageBitmap(), null, Modifier.height(150.dp), contentScale = ContentScale.Crop) }; Button(onClick = { NotificationManagerCompat.from(context).cancel(id.toInt()); finish() }) { Text(getString(R.string.action_taken_alarm)) }; Button(onClick = { sendBroadcast(Intent(this@MedicationAlarmActivity, MedicationReminderReceiver::class.java).setAction(MedicationReminderScheduler.ACTION_SNOOZE).putExtra(MedicationReminderScheduler.EXTRA_REMINDER_ID, id)); finish() }) { Text(getString(R.string.action_snooze_alarm)) } } } } }
}
