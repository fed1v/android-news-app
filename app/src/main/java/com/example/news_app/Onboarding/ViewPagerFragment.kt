package com.example.news_app.Onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.example.news_app.Onboarding.Screens.FirstScreen
import com.example.news_app.Onboarding.Screens.SecondScreen
import com.example.news_app.Onboarding.Screens.ThirdScreen
import com.example.news_app.R
import kotlinx.android.synthetic.main.fragment_view_pager.view.*

class ViewPagerFragment : Fragment() {
    private lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_view_pager, container, false)
        val fragmentList = listOf<Fragment>(
            FirstScreen(),
            SecondScreen(),
            ThirdScreen()
        )
        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )
        v.viewPager.adapter = adapter
        return v
    }
}