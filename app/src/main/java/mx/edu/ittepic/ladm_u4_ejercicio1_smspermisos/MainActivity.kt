package mx.edu.ittepic.ladm_u4_ejercicio1_smspermisos

import android.content.pm.PackageManager
import android.database.sqlite.SQLiteException
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val siPermiso = 1
    val siPermisoReceiver = 2
    val siPermisoLectura = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.RECEIVE_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.RECEIVE_SMS),siPermisoReceiver)
        }

        if(ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_SMS),siPermisoLectura)
        } else {
            leerSMSEntrada()
        }

        button.setOnClickListener {
            if(ActivityCompat.checkSelfPermission(this,
                   android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,
                         arrayOf(android.Manifest.permission.SEND_SMS), siPermiso)
            } else {
                envioSMS()
            }
        }

        textView.setOnClickListener {
            try {
                val cursor = BaseDatos(this, "entrantes", null,1)
                    .readableDatabase
                    .rawQuery("SELECT * FROM ENTRANTES",null)

                var ultimo = ""
                if(cursor.moveToFirst()){
                    do{
                        ultimo = "ULTIMO MENSAJE RECIBIDO\nCELULAR ORIGEN: "+
                                cursor.getString(0)+
                                "\nMENSAJE SMS: "+cursor.getString(1)
                    }while (cursor.moveToNext())
                } else {
                    ultimo = "SIN MENSAJES AUN, TABLA VACIA"
                }
                textView.setText(ultimo)
            } catch (err:SQLiteException){
                Toast.makeText(this, err.message,Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
        grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if(requestCode == siPermiso){
                envioSMS()
            }
            if(requestCode == siPermisoReceiver){
                mensajeRecibir()
            }
            if(requestCode == siPermisoLectura){
                leerSMSEntrada()
            }

    }

    private fun leerSMSEntrada() {
        var cursor = contentResolver.query(
            Uri.parse("content://sms/"),
            null,null,null,null
        )

        var resultado = ""
        if(cursor.moveToFirst()){
            var posColumnacelularOrigen = cursor.getColumnIndex("address")
            var posColumnaMensaje = cursor.getColumnIndex("body")
            val posColumnaFecha = cursor.getColumnIndex("date")
            do{

                val fechamensaje = cursor.getString(posColumnaFecha)
                resultado += "ORIGEN: "+cursor.getString(posColumnacelularOrigen)+
                        "\nMENSAJE: "+cursor.getString(posColumnaMensaje)+
                        "\nFECHA: "+Date(fechamensaje.toLong())+
                        "\n-------------\n"

            }while (cursor.moveToNext())
        } else {
            resultado = "NO HAY SMS EN BANDE DE ENTRADA"
        }
        textView2.setText(resultado)
    }

    private fun mensajeRecibir() {
        AlertDialog.Builder(this)
            .setMessage("SE OTORGO RECIBIR")
            .show()
    }


    private fun envioSMS() {
        SmsManager.getDefault().sendTextMessage(editText.text.toString(),null,
        editText2.text.toString(), null,null)
        Toast.makeText(this,"SE ENVIO EL SMS", Toast.LENGTH_LONG)
            .show()
    }
}

/*
 The message format is passed in the
 Telephony.Sms.Intents.SMS_RECEIVED_ACTION
 as the format String extra, and will be
 either "3gpp" for GSM/UMTS/LTE messages
 in 3GPP format or "3gpp2"
 for CDMA/LTE messages in 3GPP2 format.
 */
