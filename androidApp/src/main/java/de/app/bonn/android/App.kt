package de.app.bonn.android

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class App : Application() {
    override fun onCreate() {
        super.onCreate()

//        if (FirebaseApp.getApps(this.applicationContext).isEmpty()) {
//            FirebaseApp.initializeApp(this.applicationContext)
//            Log.d("Firebase", "Firebase initialized")
//        }
    }
}
