package com.example.news_app.Onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(
    var list: List<Fragment>,
    var fragmentManager: FragmentManager,
    var lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {


    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment = list[position]
}