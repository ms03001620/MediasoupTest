package com.example.mediasouptest

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.mediasoup.droid.MediasoupClient
import permissions.dispatcher.*
import permissions.dispatcher.ktx.PermissionsRequester
import permissions.dispatcher.ktx.constructPermissionsRequest


class SplashScreenActivity : AppCompatActivity() {
    //https://github.com/permissions-dispatcher/PermissionsDispatcher/tree/master/ktx
    private lateinit var permissionsRequest : PermissionsRequester

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        initVersion()
        permissionsRequest = makePermissionsRequester()
        permissionsRequest.launch()
    }

    private fun makePermissionsRequester(): PermissionsRequester {
        val permissions = mutableListOf<String>(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        return constructPermissionsRequest(
            permissions = permissions.toTypedArray(),
            onShowRationale = ::showRationaleForCamera,
            onPermissionDenied = ::onCameraDenied,
            onNeverAskAgain = ::onCameraNeverAskAgain,
            requiresPermission = ::enterMain
        )
    }

    private fun initVersion() {
        val tvVer = findViewById<View>(R.id.text_ver) as TextView
        tvVer.text = MediasoupClient.version()
    }

    override fun onResume() {
        Log.d("SplashScreenActivity", "onResume")
        super.onResume()
    }

    fun enterMain() {
        findViewById<View>(R.id.mediasoup).postDelayed({
            //startActivity(Intent(this@SplashScreenActivity, TestMeActivity::class.java))
            startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            finish()
        }, 1500)
    }

/*
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }
*/

    fun showRationaleForCamera(request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setTitle("Permission")
            .setMessage("Voip call camera mic, Please !")
            .setNegativeButton("Exit", DialogInterface.OnClickListener { dialog, which ->
                request.cancel()
            })
            .setPositiveButton("Setting", DialogInterface.OnClickListener { dialog, which ->
                request.proceed()
            })
            .create()
            .show()
    }

    fun onCameraDenied() {
        Toast.makeText(this, "No Permission to enter", Toast.LENGTH_LONG).show()
        finish()
    }

    private val REQ_SETTING = 10010

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SETTING) {
            permissionsRequest.launch()
        }
    }

    fun onCameraNeverAskAgain() {
        Snackbar.make(findViewById(android.R.id.content), "Permission has denied. Go Settings?", Snackbar.LENGTH_INDEFINITE)
            .setAction("SETTINGS") {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, REQ_SETTING)
            }
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.WHITE)
            .show()
    }

}