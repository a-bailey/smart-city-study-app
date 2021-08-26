package de.tudarmstadt.smartcitystudyapp.ui.incidents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.FragmentScoped
import de.tudarmstadt.smartcitystudyapp.R
import de.tudarmstadt.smartcitystudyapp.model.User
import de.tudarmstadt.smartcitystudyapp.services.PointType
import de.tudarmstadt.smartcitystudyapp.services.UserService
import de.tudarmstadt.smartcitystudyapp.ui.teamactivity.Teams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ThankYouFragment : Fragment() {

    @Inject
    lateinit var userService: UserService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_thankyou, container, false)
        root.findViewById<Button>(R.id.thankyou_button_close).setOnClickListener {
            root.findNavController().navigate(R.id.action_global_home)
        }
        return root
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.IO).launch {
            var currentUser: User? = null
            currentUser = userService.getCurrentUser()

            withContext(Dispatchers.Main) {
                val diff =  Calendar.getInstance().time.time - currentUser!!.starttime
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60

                val incrementSteps = Math.round(hours.toFloat() / 4)

                val teamPoints =  Teams.ownTeam.points + (incrementSteps * PointType.TEAM.points) + (currentUser!!.reports * PointType.TEAM.points)
                view?.findViewById<TextView>(R.id.score_team)?.text = teamPoints.toString()

            }
        }
    }
}