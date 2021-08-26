package de.tudarmstadt.smartcitystudyapp.ui.teamactivity

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import de.tudarmstadt.smartcitystudyapp.MainActivity
import de.tudarmstadt.smartcitystudyapp.R
import de.tudarmstadt.smartcitystudyapp.helper.SharedPref

@AndroidEntryPoint
class TeamActivityFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_teamactivity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        view?.findViewById<Button>(R.id.ereignisse)?.setOnClickListener {
            findNavController().navigate(R.id.action_nav_reports_to_nav_incidents)
        }
    }
}