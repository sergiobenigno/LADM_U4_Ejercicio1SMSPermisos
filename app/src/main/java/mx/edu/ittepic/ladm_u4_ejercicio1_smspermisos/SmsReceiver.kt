package mx.edu.ittepic.ladm_u4_ejercicio1_smspermisos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Build
import android.telephony.SmsMessage
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

/*
   RECEIVER = evento u oyente de android que permite
              la lectura de eventos del
              sistema operativo.
 */

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val extras = intent.extras

        if(extras != null) {
            var sms = extras.get("pdus") as Array<Any>

            for(indice in sms.indices){
                var formato = extras.getString("format")

                var smsMensaje = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    SmsMessage.createFromPdu(sms[indice] as ByteArray,formato)
                } else {
                    SmsMessage.createFromPdu(sms[indice] as ByteArray)
                }

                var celularOrigen = smsMensaje.originatingAddress
                var contenidoSMS = smsMensaje.messageBody.toString()

                //GUARDAR SOBRE TABLA SQLITE

                try {
                    var baseDatos = BaseDatos(context, "entrantes", null, 1)
                    var insertar = baseDatos.writableDatabase
                    var SQL = "INSERT INTO ENTRANTES VALUES('${celularOrigen}','${contenidoSMS}')"
                    insertar.execSQL(SQL)
                    baseDatos.close()
                }catch (err: SQLiteException){
                    Toast.makeText(context, err.message,Toast.LENGTH_LONG)
                        .show()
                }

                Toast.makeText(context, "ENTRO CONTENIDO ${contenidoSMS}",Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

}