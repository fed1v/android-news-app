package com.example.news_app.Fragments

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.example.news_app.DatabaseHelper
import com.example.news_app.EncryptionHelper
import com.example.news_app.Models.NewsHeadlines
import com.example.news_app.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class NotesSecurityDialogFragment(var headlines: NewsHeadlines) : DialogFragment() {
    private lateinit var v: View
    private lateinit var btn_confirm: Button
    private lateinit var btn_touch_id: Button
    private lateinit var et_password: EditText

    private lateinit var databaseHelper: DatabaseHelper
    private var password: String? = null

    private var cancellationSignal: CancellationSignal? = null
    private val authenticationCallback: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                if (context != null) {
                    Toast.makeText(context, "Authentication success", Toast.LENGTH_SHORT).show()
                }
                openNotes()
                dismiss()
                super.onAuthenticationSucceeded(result)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.dialog_fragment_notes_security, container, false)
        databaseHelper = DatabaseHelper(requireContext(), headlines)
        getPasswordFromDatabase()
        initView()
        return v
    }

    private fun initView() {
        btn_confirm = v.findViewById(R.id.btn_confirm)
        btn_touch_id = v.findViewById(R.id.btn_touch_id)
        et_password = v.findViewById(R.id.et_password)
        btn_touch_id.setOnClickListener {
            if (checkBiometricSupport() && Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val biometricPrompt = BiometricPrompt.Builder(requireContext())
                    .setTitle("Notes authentication")
                    .setNegativeButton("Cancel", requireContext().mainExecutor) { _, _ -> }
                    .build()
                biometricPrompt.authenticate(
                    getCancellationSignal(),
                    requireContext().mainExecutor,
                    authenticationCallback
                )
            }
        }
        btn_confirm.setOnClickListener {
            if (signInSuccessful()) {
                Toast.makeText(requireContext(), "Successful", Toast.LENGTH_SHORT).show()
                dismiss()
                openNotes()
            } else {
                Toast.makeText(requireContext(), "Wrong password", Toast.LENGTH_SHORT).show()
            }
            et_password.text = null
        }
    }

    private fun openNotes() {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragment_container, NotesFragment(headlines))
            ?.addToBackStack(null)
            ?.commit()
    }

    private fun getPasswordFromDatabase() {
        databaseHelper.currentUserReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                password = snapshot.child("notesPassword").getValue(String::class.java)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun signInSuccessful(): Boolean {
        val passwordHash = EncryptionHelper.getSHA256(et_password.text.toString())
        if (password == null || password == passwordHash) {
            return true
        }
        return false
    }

    private fun checkBiometricSupport(): Boolean {
        try {
            val keyguardManager: KeyguardManager =
                requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            if (!keyguardManager.isKeyguardSecure) {
                Toast.makeText(
                    context,
                    "Fingerprint authentication is not enabled",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }

            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.USE_BIOMETRIC
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    context,
                    "Fingerprint authentication permission is not enabled",
                    Toast.LENGTH_SHORT
                ).show()

                return false
            }

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
            } else {
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun getCancellationSignal(): CancellationSignal {
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            Toast.makeText(context, "Authentication was cancelled", Toast.LENGTH_SHORT).show()
        }
        return cancellationSignal as CancellationSignal
    }
}