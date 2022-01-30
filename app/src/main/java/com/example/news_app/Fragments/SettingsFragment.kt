package com.example.news_app.Fragments

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.example.news_app.*
import com.example.news_app.R
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class SettingsFragment : Fragment() {
    private lateinit var pendingIntent: PendingIntent
    private lateinit var v: View
    private lateinit var text_user_name: TextView
    private lateinit var text_user_email: TextView

    private lateinit var button_logout: Button
    private lateinit var button_language: Button
    private lateinit var button_country: Button
    private lateinit var button_category: Button
    private lateinit var button_select_time: Button
    private lateinit var button_set_alarm: Button
    private lateinit var button_cancel_alarm: Button

    private lateinit var btn_change_password: Button

    private var googleSignInAccount: GoogleSignInAccount? = null
    private var googleSignInClient: GoogleSignInClient? = null

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var usersReference: DatabaseReference
    private lateinit var currentUserReference: DatabaseReference
    private lateinit var userBookmarksReference: DatabaseReference
    private lateinit var userStatsReference: DatabaseReference
    private var user: FirebaseUser? = null

    private val countriesMap = NewsOptionsHelper.countries
    private var country_num = 0

    private val languagesMap = NewsOptionsHelper.languages
    private var language_num = 0

    private val categories = arrayOf(
        "Any",
        "Business",
        "Entertainment",
        "General",
        "Health",
        "Science",
        "Sports",
        "Technology"
    )
    private var category_num = 2

    private lateinit var user_language: String
    private lateinit var user_country: String
    private lateinit var user_category: String

    private var userPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var picker: MaterialTimePicker

    companion object {
        lateinit var calendar: Calendar
        lateinit var switch_notes_security: SwitchCompat
    }

    private var alarmManager: AlarmManager? = null

    private var password: String? = null
    private var firstCheck = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = LayoutInflater.from(context).inflate(R.layout.fragment_settings, container, false)

        if (!InternetConnection.isConnected()) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
            return v
        }

        initDatabase()
        getUserSettings()
        getPasswordFromDatabase()

        initView()

        country_num = countriesMap.keys.indexOf(user_country)
        language_num = languagesMap.keys.indexOf(user_language)
        category_num = categories.indexOf(user_category)

        return v
    }

    private fun getUserSettings() {
        userPreferences = context?.getSharedPreferences("User settings", MODE_PRIVATE)
        user_country = userPreferences?.getString("User country", "us") ?: "us"
        user_language = userPreferences?.getString("User language", "en") ?: "en"
        user_category = userPreferences?.getString("User category", "general") ?: "general"
    }

    private fun initDatabase() {
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        if (user != null) {
            firebaseDatabase = FirebaseDatabase.getInstance()
            usersReference = firebaseDatabase.getReference("users")
            currentUserReference = usersReference.child(user!!.uid)
            userBookmarksReference = currentUserReference.child("bookmarks")
            userStatsReference = currentUserReference.child("stats")
        }
    }

    private fun getPasswordFromDatabase() {
        currentUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                password = snapshot.child("notesPassword").getValue(String::class.java)
                switch_notes_security.isChecked = (!password.isNullOrBlank())
                btn_change_password.isClickable = (!password.isNullOrBlank())
                firstCheck = false
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun initView() {
        text_user_email = v.findViewById(R.id.text_user_email)
        text_user_name = v.findViewById(R.id.text_user_name)
        bottomNavigationView = requireActivity().findViewById(R.id.bottom_nav)

        googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())
        if (googleSignInAccount != null) {
            text_user_email.text = googleSignInAccount!!.email
            text_user_name.text = googleSignInAccount!!.displayName
        } else {
            text_user_email.text = Firebase.auth.currentUser?.email ?: "-"
            text_user_name.text = Firebase.auth.currentUser?.displayName ?: "-"
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        button_logout = v.findViewById(R.id.btn_logout)
        button_category = v.findViewById(R.id.btn_category)
        button_language = v.findViewById(R.id.btn_language)
        button_country = v.findViewById(R.id.btn_country)
        button_select_time = v.findViewById(R.id.btn_select_time)
        button_set_alarm = v.findViewById(R.id.btn_set_alarm)
        button_cancel_alarm = v.findViewById(R.id.btn_cancel_alarm)

        switch_notes_security = v.findViewById(R.id.switch_notes_security)
        btn_change_password = v.findViewById(R.id.btn_change_password)

        button_logout.setOnClickListener {
            logout()
        }

        button_category.setOnClickListener {
            openCategorySettings()
        }

        button_language.setOnClickListener {
            openLanguageSettings()
        }

        button_country.setOnClickListener {
            openCountrySettings()
        }

        button_select_time.setOnClickListener {
            selectTime()
        }

        button_set_alarm.setOnClickListener {
            setAlarm()
        }

        button_cancel_alarm.setOnClickListener {
            cancelAlarm()  // TODO
        }

        btn_change_password.setOnClickListener {
            if (!password.isNullOrBlank()) {
                val dialog = ChangePasswordDialogFragment()
                dialog.show(requireActivity().supportFragmentManager, "Change Password")
            } else {
                Toast.makeText(context, "Nothing to change", Toast.LENGTH_SHORT).show()
            }
        }


        switch_notes_security.setOnCheckedChangeListener { button, isChecked ->
            if (!firstCheck) {
                if (isChecked) {
                    if (password.isNullOrBlank()) {
                        val dialog = SetPasswordDialogFragment()
                        dialog.show(requireActivity().supportFragmentManager, "Set Password")
                    }
                } else {
                    if (!password.isNullOrBlank()) {
                        val dialog = PasswordDialogFragment()
                        dialog.show(requireActivity().supportFragmentManager, "Password")
                    }
                }
            }
        }


        requireActivity().onBackPressedDispatcher.addCallback {
            bottomNavigationView.selectedItemId = R.id.newsFragment
            activity?.supportFragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, NewsFragment())
                ?.commit()
        }
    }

    private fun setAlarm() {
        AlarmReceiver.setAlarm(requireContext())
    }

    private fun cancelAlarm() {
        val intent = Intent(activity, AlarmReceiver::class.java)
        pendingIntent = PendingIntent.getBroadcast(activity, 0, intent, 0)
        if (alarmManager == null) {
            activity?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
        alarmManager?.cancel(pendingIntent)
        Toast.makeText(context, "Alarm canceled successfully", Toast.LENGTH_SHORT).show()
    }

    private fun selectTime() {
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Select notifications time")
            .build()

        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val minute = picker.minute
            println("$hour:$minute")
            setCalendar()
        }

        if (activity != null) {
            picker.show(requireActivity().supportFragmentManager, "NewsApp")
        }
    }

    private fun setCalendar() {
        calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, picker.hour)
        calendar.set(Calendar.MINUTE, picker.minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val editor =
            requireContext().getSharedPreferences("Notifications time", MODE_PRIVATE).edit()
        editor.putLong("Time", calendar.timeInMillis)
        editor.apply()
    }

    private fun openCountrySettings() {
        var country_n = country_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default country")
            .setSingleChoiceItems(countriesMap.keys.toTypedArray(), country_num) { dialog, which ->
                country_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                country_num = country_n
                changeCountry()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changeCountry() {
        val country = countriesMap.keys.toTypedArray()[country_num]
        val countryMap = mapOf("country" to country)
        currentUserReference.updateChildren(countryMap)
        editor = userPreferences?.edit()
        editor?.putString("User country", country)
        editor?.apply()
    }

    private fun openLanguageSettings() {
        var language_n = language_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default language")
            .setSingleChoiceItems(languagesMap.keys.toTypedArray(), language_num) { dialog, which ->
                language_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                language_num = language_n
                changeLanguage()
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun changeLanguage() {
        val language = languagesMap.keys.toTypedArray()[language_num]
        val languageMap = mapOf("language" to language)
        currentUserReference.updateChildren(languageMap)
        editor = userPreferences?.edit()
        editor?.putString("User language", language)
        editor?.apply()
    }

    private fun openCategorySettings() {
        var category_n = category_num
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Default category")
            .setSingleChoiceItems(categories, category_num) { dialog, which ->
                category_n = which
            }
            .setPositiveButton("Ok") { dialog, which ->
                category_num = category_n
                changeCategory()
            }
            .setNegativeButton("Cancel", null)
            .show()

    }

    private fun changeCategory() {
        val category = categories[category_num]
        val categoryMap = mapOf("category" to category)
        currentUserReference.updateChildren(categoryMap)
        editor = userPreferences?.edit()
        editor?.putString("User category", category)
        editor?.apply()
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        googleSignInClient?.signOut()
        Firebase.auth.signOut()
        val intent = Intent(context, LoginActivity::class.java)
        editor = userPreferences?.edit()
        editor?.clear()
        editor?.commit()
        startActivity(intent)
    }
}