package com.example.panicboton

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.gms.location.*
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private val REQUIRED_PERMISSION_GPS =
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION)

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private var hayAlgo: Boolean = false
    private var hayAlgo2: Boolean = false
    private var hayAlgo3: Boolean = false
    private var hayAlgo4: Boolean = false

    private lateinit var etMensaje: EditText
    private lateinit var etNombreContacto1: EditText
    private lateinit var etNombreContacto2: EditText
    private lateinit var etNombreContacto3: EditText
    private lateinit var etNombreContacto4: EditText
    private lateinit var etNumeroContacto1: EditText
    private lateinit var etNumeroContacto2: EditText
    private lateinit var etNumeroContacto3: EditText
    private lateinit var etNumeroContacto4: EditText
    private lateinit var swInitCamara: SwitchCompat

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var ubicacion: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initObjects()
    }


    override fun onBackPressed() {
        val lyAjustes = findViewById<LinearLayout>(R.id.lyAjustes)
        if (lyAjustes.isVisible == true){
            lyAjustes.isVisible = false
            val btSos = findViewById<LinearLayout>(R.id.btSos)
            btSos.isClickable = true
        }
        else{
            super.onBackPressed()
        }
    }

    private fun initObjects(){
        etMensaje = findViewById(R.id.etMensaje)
        etNombreContacto1 = findViewById(R.id.etNombreContacto1)
        etNombreContacto2 = findViewById(R.id.etNombreContacto2)
        etNombreContacto3 = findViewById(R.id.etNombreContacto3)
        etNombreContacto4 = findViewById(R.id.etNombreContacto4)
        etNumeroContacto4 = findViewById(R.id.etNumeroContacto4)
        etNumeroContacto1 = findViewById(R.id.etNumeroContacto1)
        etNumeroContacto2 = findViewById(R.id.etNumeroContacto2)
        etNumeroContacto3 = findViewById(R.id.etNumeroContacto3)
        swInitCamara = findViewById(R.id.swIniciarGrabacion)

        initPreferences()
        recoveryPreferences()
        setFbIcon()
        try {
            initPermissions()
        }catch (e: Error){
            Toast.makeText(baseContext,"Activa la ubicación para un mejor funcionamiento", Toast.LENGTH_LONG).show()
        }
    }

    private fun initPermissions(){
        requestAllPermissions()
        verifyPermissionSMS()
        verifyGPS()
        //manageLocation()
        //requestNewLocationData()
    }

    private fun requestAllPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            verifyGPS()
        }
        else verifyGPS()
    }

    private fun verifyPermissionSMS() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }
    }

    private fun verifyGPS(){
        if (allPermissionsGrantedGPS()){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
           // manageLocation()
        }
        else {
            requestPermissionsGPS()
        }
    }

    private fun requestPermissionsGPS(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    private fun allPermissionsGrantedGPS() = REQUIRED_PERMISSION_GPS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationActive():Boolean{
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermission():Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)==
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==
                PackageManager.PERMISSION_GRANTED
    }

    private fun manageLocation(){
        if (checkPermission()) {
            if (isLocationActive()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) ==
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        requestNewLocationData()
                    }
                }
            }
            else {
                ubicacion = null
                Toast.makeText(baseContext, "Active la ubicación para un mejor servicio", Toast.LENGTH_SHORT).show()
            }
        }
        else requestPermissionsGPS()
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = com.google.android.gms.location.LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallBack, Looper.myLooper())
    }

    private val mLocationCallBack = object: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            ubicacion = "Latitud: " + mLastLocation.latitude.toString()
            ubicacion+= " "
            ubicacion += "Longitud: " +mLastLocation.longitude.toString()
            //updateAddress(mLastLocation)  Funcion para incluir la direccion escrita de la ubicacion, Error, pendiente de checar
            sendMessage()
            if (swInitCamara.isChecked){
                iniciarVideo()
            }
        }
    }

    private fun updateAddress(location: Location){
        var geocoder: Geocoder
        var addressList: ArrayList<Address>
        geocoder = Geocoder(this, Locale.getDefault())
        addressList = geocoder.getFromLocation(location.latitude, location.longitude,1) as ArrayList<Address>
        ubicacion += " " + addressList.get(0).getAddressLine(0).toString()
    }

    fun abrirAjustes(v: View){
        val lyAjustes = findViewById<LinearLayout>(R.id.lyAjustes)
        lyAjustes.isVisible = true
        setFbIcon()
        val btSos = findViewById<LinearLayout>(R.id.btSos)
        btSos.isClickable = false
    }

    fun cerrarAjustes(v: View){
        cerrar()
    }

    private fun cerrar(){
        val lyAjustes = findViewById<LinearLayout>(R.id.lyAjustes)
        lyAjustes.isVisible = false
        val btSos = findViewById<LinearLayout>(R.id.btSos)
        btSos.isClickable = true
    }

    fun guardarAjustes(v: View){
        savePreferences()
        Toast.makeText(baseContext, "Configuración guardada :)", Toast.LENGTH_SHORT).show()
        cerrar()
    }

    private fun initPreferences(){
        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE)
        editor = sharedPreferences.edit()
    }

    private fun savePreferences(){
        editor.clear()
        editor.apply{
            if (etMensaje.text.isNotEmpty()){
                putString("mensaje", etMensaje.text.toString())
            }
            putString("nombreContacto1", etNombreContacto1.text.toString())
            putString("numeroContacto1", etNumeroContacto1.text.toString())
            putString("nombreContacto2", etNombreContacto2.text.toString())
            putString("numeroContacto2", etNumeroContacto2.text.toString())
            putString("nombreContacto3", etNombreContacto3.text.toString())
            putString("numeroContacto3", etNumeroContacto3.text.toString())
            putString("nombreContacto4", etNombreContacto4.text.toString())
            putString("numeroContacto4", etNumeroContacto4.text.toString())
            putBoolean("hayAlgo", hayAlgo)
            putBoolean("hayAlgo2", hayAlgo2)
            putBoolean("hayAlgo3", hayAlgo3)
            putBoolean("hayAlgo4", hayAlgo4)
            putBoolean("initVideo", swInitCamara.isChecked)
        }.apply()
    }

    private fun recoveryPreferences(){
        hayAlgo = sharedPreferences.getBoolean("hayAlgo", false)
        hayAlgo2 = sharedPreferences.getBoolean("hayAlgo2", false)
        hayAlgo3 = sharedPreferences.getBoolean("hayAlgo3", false)
        hayAlgo3 = sharedPreferences.getBoolean("hayAlgo4", false)
        etMensaje.setText(sharedPreferences.getString("mensaje", "¡¡Ayuda!! Algo ha pasado y me siento en peligro"))
        etNombreContacto1.setText(sharedPreferences.getString("nombreContacto1", ""))
        etNumeroContacto1.setText(sharedPreferences.getString("numeroContacto1", ""))
        etNombreContacto2.setText(sharedPreferences.getString("nombreContacto2", ""))
        etNumeroContacto2.setText(sharedPreferences.getString("numeroContacto2", ""))
        etNombreContacto3.setText(sharedPreferences.getString("nombreContacto3", ""))
        etNumeroContacto3.setText(sharedPreferences.getString("numeroContacto3", ""))
        etNombreContacto4.setText(sharedPreferences.getString("nombreContacto4", ""))
        etNumeroContacto4.setText(sharedPreferences.getString("numeroContacto4", ""))
        swInitCamara.isChecked = sharedPreferences.getBoolean("initVideo", true)
    }

    fun sosActivated(v: View){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(baseContext, "Tiene que proporcionar permiso para enviar mensajes", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
        if (etNumeroContacto1.text.isEmpty() && etNumeroContacto2.text.isEmpty() && etNumeroContacto3.text.isEmpty() && etNumeroContacto4.text.isEmpty()){
            val lyAjustes = findViewById<LinearLayout>(R.id.lyAjustes)
            lyAjustes.isVisible = true
            val btSos = findViewById<LinearLayout>(R.id.btSos)
            btSos.isClickable = false
            Toast.makeText(baseContext, "Agregue al menos un contacto para poder enviar el mensaje", Toast.LENGTH_LONG).show()
        }
        else{
            if (isLocationActive()){
                enviarConLocation()
            }
            else{
                enviarSinLocation()
            }
        }
    }

    private fun enviarConLocation(){
        try {
            verifyGPS()
            manageLocation()
            //requestNewLocationData()
        }catch(e: Error){
            sendMessage()
            if (swInitCamara.isChecked){
                iniciarVideo()
            }
        }
    }

    private fun enviarSinLocation(){
        ubicacion = null
        sendMessage()
        if (swInitCamara.isChecked){
            iniciarVideo()
        }
        Toast.makeText(baseContext, "Mensaje enviado sin ubicación", Toast.LENGTH_SHORT).show()
        Toast.makeText(baseContext, "Active la ubicación para un mejor servicio", Toast.LENGTH_SHORT).show()
    }

    /*private fun setLocation(){
        try {
            verifyGPS()
            manageLocation()
            requestNewLocationData()
            requestNewLocationData()
        }catch(e: Error){
            ubicacion = null
            Toast.makeText(this, "Mensaje enviado sin ubicacion :/", Toast.LENGTH_SHORT).show()
        }finally {
            sendMessage()
            if (swInitCamara.isChecked){
            iniciarVideo()
            }
        }
    }*/

    private fun sendMessage(){
        val smsManager: SmsManager = SmsManager.getDefault()
        etMensaje = findViewById(R.id.etMensaje)
        if (etMensaje.text.isEmpty()){
            etMensaje.setText("¡¡AYUDA!! Algo ha pasado y me siento en peligro")
            if (ubicacion == null){
                if (etNumeroContacto1.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto1.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto2.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto2.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto3.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto3.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto4.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto4.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
            }
            else{
                if (etNumeroContacto1.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto1.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto2.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto2.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto3.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto3.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto4.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto4.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
            }
        }
        else{
            if (ubicacion == null){
                if (etNumeroContacto1.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto1.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto2.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto2.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto3.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto3.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
                if (etNumeroContacto4.text.isNotEmpty()) {
                    smsManager.sendTextMessage(
                        etNumeroContacto4.text.toString(), null, etMensaje.text.toString() + ", Por favor comunicate conmigo en cuanto antes",
                        null, null)
                }
            }
            else{
                if (etNumeroContacto1.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto1.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto2.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto2.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto3.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto3.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
                if (etNumeroContacto4.text.isNotEmpty()) {
                    smsManager.sendTextMessage(etNumeroContacto4.text.toString(), null, etMensaje.text.toString() + ", mi ubicacion actual es " + "("
                            + ubicacion + ")", null, null)
                }
            }
        }
    }

    private fun iniciarVideo(){
        var i= Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
        i.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 24)
        startActivity(i)
    }

    fun seleccionarContacto1(v: View){
        if (hayAlgo == false && etNumeroContacto1.text.isEmpty()){
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            startActivityForResult(intent, 2)
        }
        else{
            etNombreContacto1.setText("")
            etNumeroContacto1.setText("")
            hayAlgo = false
            setFbIcon()
        }
    }

    fun seleccionarContacto2(v: View){
        if (hayAlgo2 == false && etNumeroContacto2.text.isEmpty()){
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            startActivityForResult(intent, 3)
        }
        else{
            etNombreContacto2.setText("")
            etNumeroContacto2.setText("")
            hayAlgo2 = false
            setFbIcon()
        }
    }

    fun seleccionarContacto3(v: View){
        if (hayAlgo3 == false && etNumeroContacto3.text.isEmpty()){
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            startActivityForResult(intent, 4)
        }
        else{
            etNombreContacto3.setText("")
            etNumeroContacto3.setText("")
            hayAlgo3 = false
            setFbIcon()
        }
    }

    fun seleccionarContacto4(v: View){
        if (hayAlgo4 == false && etNumeroContacto4.text.isEmpty()){
            var intent = Intent(Intent.ACTION_PICK)
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE)
            startActivityForResult(intent, 5)
        }
        else{
            etNombreContacto4.setText("")
            etNumeroContacto4.setText("")
            hayAlgo4 = false
            setFbIcon()
        }
    }
    private fun setFbIcon(){
        val ivContacto1 = findViewById<ImageView>(R.id.ivContacto1)
        val ivContacto2 = findViewById<ImageView>(R.id.ivContacto2)
        val ivContacto3 = findViewById<ImageView>(R.id.ivContacto3)
        val ivContacto4 = findViewById<ImageView>(R.id.ivContacto4)
        val fbContacto1 = findViewById<Button>(R.id.fbContacto1)
        val fbContacto2 = findViewById<Button>(R.id.fbContacto2)
        val fbContacto3 = findViewById<Button>(R.id.fbContacto3)
        val fbContacto4 = findViewById<Button>(R.id.fbContacto4)
        if (hayAlgo == true || etNumeroContacto1.text.isNotEmpty()){
            ivContacto1.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            fbContacto1.setBackgroundResource(R.drawable.trash_icon)
        }
        else{
            ivContacto1.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_strong))
            fbContacto1.setBackgroundResource(R.drawable.plus)
        }
        if (hayAlgo2 == true || etNumeroContacto2.text.isNotEmpty()){
            ivContacto2.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            fbContacto2.setBackgroundResource(R.drawable.trash_icon)
        }
        else{
            ivContacto2.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_strong))
            fbContacto2.setBackgroundResource(R.drawable.plus)
        }
        if (hayAlgo3 == true || etNumeroContacto3.text.isNotEmpty()){
            ivContacto3.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            fbContacto3.setBackgroundResource(R.drawable.trash_icon)
        }
        else{
            ivContacto3.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_strong))
            fbContacto3.setBackgroundResource(R.drawable.plus)
        }
        if (hayAlgo4 == true || etNumeroContacto4.text.isNotEmpty()){
            ivContacto4.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            fbContacto4.setBackgroundResource(R.drawable.trash_icon)
        }
        else{
            ivContacto4.setBackgroundColor(ContextCompat.getColor(this, R.color.gray_strong))
            fbContacto4.setBackgroundResource(R.drawable.plus)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode ==2 && resultCode == RESULT_OK){
            var uri: Uri? = data?.data
            var cursor: Cursor? = getContentResolver().query(uri!!, null,null,null,null)
            if (cursor != null && cursor.moveToFirst()){
                var indiceName: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var indiceNumero: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var nombre: String = cursor.getString(indiceName)
                var numero: String = cursor.getString(indiceNumero)
                numero.replace("(", "").replace(")","").replace(" ", "")


                etNombreContacto1.setText(nombre)
                etNumeroContacto1.setText(numero)
                hayAlgo = true
                setFbIcon()
            }
        }

        if (requestCode ==3 && resultCode == RESULT_OK){
            var uri: Uri? = data?.data
            var cursor: Cursor? = getContentResolver().query(uri!!, null,null,null,null)
            if (cursor != null && cursor.moveToFirst()){
                var indiceName: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var indiceNumero: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var nombre: String = cursor.getString(indiceName)
                var numero: String = cursor.getString(indiceNumero)
                numero.replace("(", "").replace(")","").replace(" ", "")


                etNombreContacto2.setText(nombre)
                etNumeroContacto2.setText(numero)
                hayAlgo2 = true
                setFbIcon()
            }
        }
        if (requestCode ==4 && resultCode == RESULT_OK){
            var uri: Uri? = data?.data
            var cursor: Cursor? = getContentResolver().query(uri!!, null,null,null,null)
            if (cursor != null && cursor.moveToFirst()){
                var indiceName: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var indiceNumero: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var nombre: String = cursor.getString(indiceName)
                var numero: String = cursor.getString(indiceNumero)
                numero.replace("(", "").replace(")","").replace(" ", "")


                etNombreContacto3.setText(nombre)
                etNumeroContacto3.setText(numero)
                hayAlgo3 = true
                setFbIcon()
            }
        }
        if (requestCode ==5 && resultCode == RESULT_OK){
            var uri: Uri? = data?.data
            var cursor: Cursor? = getContentResolver().query(uri!!, null,null,null,null)
            if (cursor != null && cursor.moveToFirst()){
                var indiceName: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var indiceNumero: Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var nombre: String = cursor.getString(indiceName)
                var numero: String = cursor.getString(indiceNumero)
                numero.replace("(", "").replace(")","").replace(" ", "")


                etNombreContacto4.setText(nombre)
                etNumeroContacto4.setText(numero)
                hayAlgo4 = true
                setFbIcon()
            }
        }
    }
}

//Erick Gerardo Zuniga Flores   Universidad Politecnica de Pachuca.