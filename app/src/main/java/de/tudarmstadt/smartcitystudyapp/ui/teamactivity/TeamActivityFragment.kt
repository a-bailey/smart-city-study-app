package de.tudarmstadt.smartcitystudyapp.ui.teamactivity

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.tudarmstadt.smartcitystudyapp.MainActivity
import de.tudarmstadt.smartcitystudyapp.R
import de.tudarmstadt.smartcitystudyapp.helper.SharedPref
import de.tudarmstadt.smartcitystudyapp.model.ActivityEntry
import de.tudarmstadt.smartcitystudyapp.model.User
import de.tudarmstadt.smartcitystudyapp.services.PointType
import de.tudarmstadt.smartcitystudyapp.services.UserService
import de.tudarmstadt.smartcitystudyapp.ui.activities.ActivitiesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TeamActivityFragment : Fragment() {

    @Inject
    lateinit var userService: UserService

    private var adapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_teamactivity, container, false)
        adapter = ArrayAdapter(
            this.requireContext(), android.R.layout.simple_list_item_1, emptyList<String>().toMutableList()
        )
        root.findViewById<ListView>(R.id.activities_scroll_view).adapter = adapter

        return root
    }

    private lateinit var ownTeam: Team
    private lateinit var teams: List<Team>

    override fun onResume() {
        super.onResume()

        ownTeam = Teams.ownTeam.copy()
        teams = Teams.teams.toMutableList()

        updateTeamActivities()
    }

    fun updateTeamActivities() {
        CoroutineScope(Dispatchers.IO).launch {
            var currentUser: User? = null
            currentUser = userService.getCurrentUser()

            withContext(Dispatchers.Main) {
                val diff =  Calendar.getInstance().time.time - currentUser!!.starttime
                val seconds = diff / 1000
                val minutes = seconds / 60
                val hours = minutes / 60

                val incrementSteps = Math.round(hours.toFloat() / 4)

                teams = teams.map {
                    /*for(i in 1..incrementSteps) {
                        it.points = it.points + rand(0, 600)
                    }*/
                    it.points = it.points + (incrementSteps * PointType.TEAM.points)
                    it
                }

                /*for(i in 1..incrementSteps) {
                    ownTeam.points =  ownTeam.points + rand(0, 600)
                }*/
                ownTeam.points =  ownTeam.points + (incrementSteps * PointType.TEAM.points) + (currentUser!!.reports * PointType.TEAM.points)

                adapter?.clear()
                adapter?.add(
                    "Dein ${ownTeam.name} hat ${ownTeam.points.toString()} Punkte gemacht!"
                )
                adapter?.addAll(
                    teams.map {
                        "${it.name} aus ${it.location} hat bereits ${it.points.toString()} Punkte gemacht!"
                    }
                )
            }
        }
    }

    fun rand(start: Int, end: Int): Int {
        require(start <= end) { "Illegal Argument" }
        val random = SecureRandom()
        random.setSeed(random.generateSeed(20))

        return random.nextInt(end - start + 1) + start
    }
}