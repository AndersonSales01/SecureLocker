package com.example.securelocker.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.io.File

object EncryptionHelper {

    private val prefName = "com.example.securelocker.pref"
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun getSharedPref(context: Context): SharedPreferences {
        val keyEncryptedScheme = EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
        val valueEncryptedScheme = EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM

        return EncryptedSharedPreferences.create(
            prefName,
            masterKeyAlias,
            context,
            keyEncryptedScheme,
            valueEncryptedScheme
        )
    }

    fun EncryptedFile(file: File, context: Context): EncryptedFile {
        val fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB

        return EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            fileEncryptionScheme
        ).build()
    }
}