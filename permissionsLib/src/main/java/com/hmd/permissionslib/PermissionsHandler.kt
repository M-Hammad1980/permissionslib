import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.speakercleaner.volume.booster.loud.music.volumeBooster.utils.DefaultDialog

class PermissionsHandler(private val activity: ComponentActivity) {

    private var permissions: List<String> = listOf()
    private var currentPermissionIndex = 0
    private var deniedPermissions: MutableList<String> = mutableListOf()
    private var onPermissionResult: ((Boolean) -> Unit)? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var explainRequestReason: ((List<String>, PermissionRequestScope) -> Unit)? = null
    private var forwardToSettings: ((List<String>, PermissionRequestScope) -> Unit)? = null

    init {
        permissionLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            handlePermissionResult(isGranted)
        }
    }

    fun permissions(permissions: List<String>): PermissionsHandler {
        this.permissions = permissions
        return this
    }

    fun onExplainRequestReason(explainRequestReason: (List<String>, PermissionRequestScope) -> Unit): PermissionsHandler {
        this.explainRequestReason = explainRequestReason
        return this
    }

    fun onForwardToSettings(forwardToSettings: (List<String>, PermissionRequestScope) -> Unit): PermissionsHandler {
        this.forwardToSettings = forwardToSettings
        return this
    }

    fun request(onResult: (Boolean) -> Unit) {
        this.onPermissionResult = onResult
        currentPermissionIndex = 0
        deniedPermissions.clear()
        requestNextPermission()
    }

    private fun requestNextPermission() {
        if (currentPermissionIndex < permissions.size) {
            val permission = permissions[currentPermissionIndex]
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    Log.d("permission**", "requestNextPermission: shouldShowRequestPermissionRationale")
                    explainRequestReason?.invoke(listOf(permission), PermissionRequestScope())
                } else {
                    Log.d("permission**", "requestNextPermission: launch")
                    permissionLauncher.launch(permission)
                }
            } else {
                // Permission already granted, proceed to the next one
                currentPermissionIndex++
                requestNextPermission()
            }
        } else {
            // All permissions requested, check if any were denied
            if (deniedPermissions.isNotEmpty()) {
                // Some permissions were denied, show forward to settings dialog
                forwardToSettings?.invoke(deniedPermissions, PermissionRequestScope())
            } else {
                // All permissions granted
                onPermissionResult?.invoke(true)
            }
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        val permission = permissions[currentPermissionIndex]
        if (isGranted) {
            // Permission granted, proceed to the next one
            currentPermissionIndex++
            requestNextPermission()
        } else {
            // Permission denied
            deniedPermissions.add(permission)
            if (currentPermissionIndex < permissions.size) {
                // Request next permission
                currentPermissionIndex++
                requestNextPermission()
            } else {
                // All permissions requested, check if any were denied
                if (deniedPermissions.isNotEmpty()) {
                    // Some permissions were denied, show forward to settings dialog
                    forwardToSettings?.invoke(deniedPermissions, PermissionRequestScope())
                } else {
                    // All permissions granted
                    onPermissionResult?.invoke(true)
                }
            }
        }
    }

    inner class PermissionRequestScope {
        fun showRequestReasonDialog(context:Context, deniedList: List<String>, message: String, positiveText: String, negativeText: String) {
            DefaultDialog(context,deniedList,message,positiveText,negativeText){
            }.show()
            /*AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(positiveText) { _, _ ->
                    permissionLauncher.launch(deniedList[0])
                }
                .setNegativeButton(negativeText) { _, _ ->
                    onPermissionResult?.invoke(false)
                }
                .show()*/
        }

        fun showForwardToSettingsDialog(context:Context, deniedList: List<String>, message: String, positiveText: String, negativeText: String) {
            DefaultDialog(context,deniedList,message,positiveText,negativeText){
                onPermissionResult?.invoke(false)
            }.show()
//            AlertDialog.Builder(activity)
//                .setMessage(message)
//                .setPositiveButton(positiveText) { _, _ ->
//                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                        data = android.net.Uri.fromParts("package", activity.packageName, null)
//                    }
//                    activity.startActivity(intent)
//                }
//                .setNegativeButton(negativeText) { _, _ ->
//                    onPermissionResult?.invoke(false)
//                }
//                .show()
        }
    }
}
