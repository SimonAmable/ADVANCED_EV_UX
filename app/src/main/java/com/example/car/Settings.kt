package com.example.car

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextClock
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class Settings : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the current local time
        val calendar = Calendar.getInstance()
        val timeZone: TimeZone = calendar.timeZone

        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val minute: Int = calendar.get(Calendar.MINUTE)

        // Display the local time
        val currentTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        Log.d("Local Time", "Current local time: $currentTime")

        // Set the local time to TextClock
        val textClock: TextClock = view.findViewById(R.id.textClock)
        textClock.text = currentTime

        // Assuming you have a button in your Settings fragment with ID button1
        val button1: Button = view.findViewById(R.id.button1)

        button1.setOnClickListener {
            replaceFragment(Bluetooth())
        }

        val button2: Button = view.findViewById(R.id.button2)

        button2.setOnClickListener {
            replaceFragment(Charge())
        }
    }

    // Add this function to your Settings fragment or in a utility class
    fun replaceFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
