package com.project.noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        replaceFragment(HomeFragment.newInstance(), true)

    }

    private fun replaceFragment(fragment: Fragment, isTransition: Boolean) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        if (isTransition) {
            fragmentTransaction.setCustomAnimations(
                android.R.anim.slide_out_right,
                android.R.anim.slide_in_left
            )
        }
        fragmentTransaction.replace(R.id.frame_layout, fragment)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
    }

}