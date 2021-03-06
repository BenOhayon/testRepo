package com.benohayon.meallennium.framework.managers

import android.content.Context
import com.benohayon.meallennium.framework.models.USER_EMAIL_KEY
import com.benohayon.meallennium.framework.models.USER_FIRST_NAME_KEY
import com.benohayon.meallennium.framework.models.USER_LOGIN_METHOD_KEY
import com.benohayon.meallennium.framework.utils.MeallenniumSharedPreferences

object UserManager {

    fun storeName(context: Context, firstName: String?) {
        MeallenniumSharedPreferences(context)
                .storeString(USER_FIRST_NAME_KEY, firstName)
    }

    fun getName(context: Context, defaultFirstName: String? = "User Name"): String? {
        return MeallenniumSharedPreferences(context)
                .getString(USER_FIRST_NAME_KEY, defaultFirstName)
    }

    fun storeEmail(context: Context, email: String?) {
        MeallenniumSharedPreferences(context)
                .storeString(USER_EMAIL_KEY, email)
    }

    fun getEmail(context: Context, defaultEmail: String? = "user@email.com"): String? {
        return MeallenniumSharedPreferences(context)
                .getString(USER_EMAIL_KEY, defaultEmail)
    }

    fun storeLoginMethod(context: Context, loginMethod: FirebaseManager.LoginMethod?) {
        MeallenniumSharedPreferences(context)
                .storeLoginMethod(USER_LOGIN_METHOD_KEY, loginMethod)
    }

    fun getLoginMethod(context: Context): FirebaseManager.LoginMethod? {
        return MeallenniumSharedPreferences(context)
                .getLoginMethod(USER_LOGIN_METHOD_KEY)
    }

    fun clearValues(context: Context) {
        MeallenniumSharedPreferences(context)
                .clearValues()
    }

}