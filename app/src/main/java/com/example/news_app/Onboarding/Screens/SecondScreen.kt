package com.example.news_app.Onboarding.Screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.example.news_app.R

class SecondScreen : Fragment() {
    private lateinit var v: View
    private lateinit var viewPager: ViewPager2
    private lateinit var next: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_second_screen, container, false)

        next = v.findViewById(R.id.next)

        viewPager = requireActivity().findViewById(R.id.viewPager)

        next.setOnClickListener {
            viewPager.currentItem = 2
        }

        return v
    }

}