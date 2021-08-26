package de.tudarmstadt.smartcitystudyapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.tudarmstadt.smartcitystudyapp.MainActivity
import de.tudarmstadt.smartcitystudyapp.R
import de.tudarmstadt.smartcitystudyapp.model.User
import de.tudarmstadt.smartcitystudyapp.services.UserService
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    @Inject
    lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()

        view?.findViewById<Button>(R.id.mach_mehr_punkte)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_profile_to_nav_incidents)
        }

        showUserInfo()
    }

    fun showUserInfo() {

        CoroutineScope(Dispatchers.IO).launch {
            var currentUser: User? = null
            currentUser = userService.getCurrentUser()

            withContext(Dispatchers.Main) {
                view?.findViewById<TextView>(R.id.vp_name)?.text = currentUser!!.userName
                view?.findViewById<TextView>(R.id.vp_ort)?.text = currentUser.wohnort
                view?.findViewById<TextView>(R.id.points)?.setText(currentUser.points.toString())
            }
        }
    }
}