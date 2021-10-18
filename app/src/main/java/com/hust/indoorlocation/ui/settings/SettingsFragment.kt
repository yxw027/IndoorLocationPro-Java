package com.hust.indoorlocation.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.hust.indoorlocation.R
import com.hust.indoorlocation.tools.ext.showSnackMsg
import com.hust.indoorlocation.tools.util.CacheDataUtil
import com.hust.indoorlocation.tools.util.LogUtil
import java.io.File
import android.provider.DocumentsContract





class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var context: SettingsActivity? = null


    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
        fun getInstance() = SettingsFragment()

    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
        setHasOptionsMenu(true)
        context = activity as SettingsActivity

        LogUtil.d("SettingsFragment on create")
        preferenceScreen
        preferenceManager
        setDefaultText()
        setDefaultFiles()
        findPreference<Preference>("clearCache")?.setOnPreferenceClickListener {
            CacheDataUtil.clearAllCache((context as SettingsActivity))
            context?.showSnackMsg(getString(R.string.clear_cache_successfully))
            setDefaultText()
            false
        }

        findPreference<Preference>("clearFiles")?.setOnPreferenceClickListener {
            CacheDataUtil.clearAllFiles((context as SettingsActivity))
            context?.showSnackMsg(getString(R.string.clear_cache_successfully))
            setDefaultFiles()
            false
        }

        findPreference<Preference>("openFiles")?.setOnPreferenceClickListener {
            //调用系统文件管理器打开指定路径目录
            LogUtil.d("调用系统文件管理器打开指定路径目录")
//            val intent = Intent(Intent.ACTION_GET_CONTENT)
//            intent.setDataAndType(Uri.parse("/android/data/com.hust.indoorLocation/files/Documents/"),"*")
//            intent.addCategory (Intent.CATEGORY_OPENABLE)

//            requireActivity().startActivityForResult(intent,1)
//            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT)
//            intent.addCategory(Intent.CATEGORY_OPENABLE)
//            intent.type="*/*"
//            startActivityForResult(intent,100)

//            val dir=File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.toURI())
//            openAssignFolder(dir.path)

            intoFileManager()
            false
        }
    }
    private fun intoFileManager() {
        val path = "%2fandroid%2fdata%2fcom.hust.indoorLocation%2ffiles%2fDocuments%2f"
        val uri =
            Uri.parse("content://com.android.externalstorage.documents/document/primary:$path")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*" //想要展示的文件类型

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
        startActivityForResult(intent, 0)

    }
    private fun openAssignFolder(path: String) {
        val file = File(path)
        if (null == file || !file.exists()) {
            LogUtil.d("指定路径目录null:$path")
            return
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setDataAndType(Uri.fromFile(file), "*/*")
        try {
            startActivity(intent)
            //            startActivity(Intent.createChooser(intent,"选择浏览工具"));
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun setDefaultText() {
        try {
            findPreference<Preference>("clearCache")?.summary  = CacheDataUtil.getTotalCacheSize(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setDefaultFiles() {
        try {
            findPreference<Preference>("clearFiles")?.summary  = CacheDataUtil.getTotalFilesSize(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key ?: return
    }

}