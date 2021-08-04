package de.tudarmstadt.smartcitystudyapp.ui.incidents

import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import de.tudarmstadt.smartcitystudyapp.MainActivity
import de.tudarmstadt.smartcitystudyapp.R
import de.tudarmstadt.smartcitystudyapp.model.SOURCE_OTHER


@AndroidEntryPoint
class SubmitFragment : Fragment() {

    private val submitViewModel: SubmitViewModel by viewModels()
    private val REQUEST_IMAGE_CAPTURE = 1
    private val button_active_color = R.color.main_blue
    private val button_disabled_color = R.color.grey
    private var br: BroadcastReceiver? = null
    private var filter: IntentFilter? = null
    private var fusedLocationProvider: FusedLocationProviderClient? = null
    private val locationRequest: LocationRequest =  LocationRequest.create().apply {
        interval = 30
        fastestInterval = 10
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        maxWaitTime= 60
    }
    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_submitincidents, container, false)
        val suggestion: String = arguments?.getString("suggestion") ?: ""
        submitViewModel.source = arguments?.getString("source") ?: SOURCE_OTHER
        val galleryButton = root.findViewById<Button>(R.id.incidents_button_gallery)
        val cameraButton = root.findViewById<Button>(R.id.incidents_button_camera)
        val sendPhotoSwitch = root.findViewById<SwitchCompat>(R.id.switch_send_photo)
        val submitButton =  root.findViewById<Button>(R.id.incidents_button_submit)
        val locationSwitch = root.findViewById<SwitchCompat>(R.id.switch_send_location)
        filter = IntentFilter(getString(R.string.broadcast_network_status)).apply{
            addAction(R.string.broadcast_network_status.toString())
        }

        root.findViewById<EditText>(R.id.report_text).setText(suggestion)

        submitButton.setOnClickListener { submitViewModel.sendReport(root) }

        sendPhotoSwitch.setOnClickListener {
            when (sendPhotoSwitch.isChecked) {
                true -> {
                    galleryButton.visibility = View.VISIBLE
                    cameraButton.visibility = View.VISIBLE
                }
                false -> {
                    galleryButton.visibility = View.INVISIBLE
                    cameraButton.visibility = View.INVISIBLE
                }
            }
        }

        locationSwitch.setOnClickListener {
            when (locationSwitch.isChecked) {
                true -> {
                    requestLocationPermission()
                }
            }
        }

        galleryButton.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            startActivityForResult(Intent.createChooser(galleryIntent, "Select File"), 0)
        }

        cameraButton.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {

            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // listen for network connectivity changes and set the background color of the submit button
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i("NETWORK", "received")
                if (intent != null) {
                    if (intent.hasExtra("status")) {
                        if (getString(R.string.broadcast_network_status).equals(intent.action)) {
                            when (MainActivity.networkAvailable) {
                                true -> {
                                    setButtonColor(view, button_active_color)
                                }
                                false -> {
                                    setButtonColor(view, button_disabled_color)
                                }
                            }
                        }
                    }
                }
            }
        }

        // hacky approach to get the network status when the view is created
        /* ToDo: is there another possibility to get the network status on creation?
        *  Because the network manager broadcasts the status only on network status changes
        */
        if(MainActivity.networkAvailable) {
            setButtonColor(view, button_active_color)
        } else {
            setButtonColor(view, button_disabled_color)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(super.requireContext()).registerReceiver(br!!, filter!!)
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(super.requireContext()).unregisterReceiver(br!!)
    }

    @Suppress("DEPRECATION")
    fun setButtonColor(view: View, color: Int) {
        var button =  view.findViewById<Button>(R.id.incidents_button_submit)
        var buttonDrawable: Drawable? = button.getBackground()
        buttonDrawable = DrawableCompat.wrap(buttonDrawable!!)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableCompat.setTint(buttonDrawable, ResourcesCompat.getColor(resources, color, null))
            button.background = buttonDrawable
        } else {
            button.setBackgroundColor(resources.getColor(color));
            button.invalidate();
        }
    }

    private fun requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locationList = locationResult.locations
            if (locationList.isNotEmpty()) {
                //The last location in the list is the newest
                val location = locationList.last()
                Toast.makeText(
                    context,
                    "Got Location: " + location.toString(),
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocationProvider?.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper()
                        )
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(context, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }
}
