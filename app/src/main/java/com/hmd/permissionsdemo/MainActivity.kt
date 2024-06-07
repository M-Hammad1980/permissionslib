package com.hmd.permissionsdemo

import PermissionsHandler
import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hmd.permissionsdemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val permissionsHandler = PermissionsHandler(this)
        binding.btnRequest.setOnClickListener {
            val permissions = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                listOf(
                    Manifest.permission.READ_MEDIA_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
            else{
                listOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                )
            }

            permissionsHandler.permissions(permissions).onExplainRequestReason { deniedList, scope  ->
                Log.d("permission**", "requestPermissionApi33Notification: $deniedList")
                scope.showRequestReasonDialog(this,
                    deniedList,
                    "Core fundamental are based on these permissions",
                    "Ok",
                    "Cancel"
                )
            }.onForwardToSettings { deniedList, scope  ->
                Log.d("permission**", "requestPermissionApi33Notification: $deniedList")
                scope.showForwardToSettingsDialog(this,
                    deniedList, "These permissions are denied:",
                    "Ok",
                    "Cancel"
                )
            }.request { allGranted ->
                if (allGranted) {
//                    click?.invoke(true)
                } else {
//                    click?.invoke(false)
                    // Show setting dialog
                }
            }

        }
    }
}