package com.limor.app.scenes.patron.manage.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.FragmentInviteFriendsBinding
import com.limor.app.scenes.patron.manage.adapters.ContactsListAdapter
import com.limor.app.uimodels.ContactUIModel

class FragmentInviteFriends : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {

    @SuppressLint("InlinedApi")
    private val PROJECTION: Array<out String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        else
            ContactsContract.Contacts.DISPLAY_NAME,
        ContactsContract.Data.PHOTO_URI
    )

    @SuppressLint("InlinedApi")
    private val FROM_COLUMNS: Array<String> = arrayOf(
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)) {
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        } else {
            ContactsContract.Contacts.DISPLAY_NAME
        },
        ContactsContract.Data.PHOTO_URI
    )

    @SuppressLint("InlinedApi")
    private val SELECTION: String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
        else
            "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?"
    private var searchString: String = ""
    private val selectionArgs = arrayOf<String>(searchString)

    private val TO_IDS: IntArray = intArrayOf(R.id.contact_name_text_view, R.id.iv_user)
    var contactId: Long = 0
    var contactKey: String? = null
    var contactUri: Uri? = null
    // private var cursorAdapter: SimpleCursorAdapter? = null
    private var contactsAdapter: ContactsListAdapter? = null

    private lateinit var binding: FragmentInviteFriendsBinding
    private var mode: FriendSelectionMode = FriendSelectionMode.LIMOR_USERS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInviteFriendsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialiseViews()
        setClickListeners()
        setTextWatchers()
    }

    private fun initialiseViews() {
        binding.toolbar.title.text = getString(R.string.invite_friends)
        binding.toolbar.btnNotification.setImageDrawable(resources.getDrawable(R.drawable.ic_phone_book))

        /*cursorAdapter = SimpleCursorAdapter(
            requireContext(),
            R.layout.item_contact_list,
            null,
            FROM_COLUMNS, TO_IDS,
            0
        )*/
        contactsAdapter = ContactsListAdapter(
            ArrayList()
        )
        //binding.contactsListView.adapter = cursorAdapter
        binding.contactsListView.adapter = contactsAdapter
    }

    private fun setTextWatchers(){
        binding.searchBar.apply {
            setOnQueryTextListener(
                onQueryTextChange = {
                    performSearch(it)
                },
                onQueryTextSubmit = {
                    performSearch(it)
                },
                onQueryTextBlank = {
                    performSearch("")
                }
            )
        }
    }

    private fun setClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            if(mode == FriendSelectionMode.CONTACTS){
                showLimorUsers()
            } else{
                findNavController().navigateUp()
            }
        }
        binding.toolbar.btnNotification.setOnClickListener {
            if (!hasPermissions(requireContext(), *PERMISSIONS)) {
                try {
                    requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else{
                loaderManager.initLoader(0, null, this)
                showContactsLayout()
            }
        }
    }

    private fun performSearch(searchItem: String){
        searchString = searchItem
        loaderManager.restartLoader(0, null, this)
    }

    private fun showContactsLayout(){
        mode = FriendSelectionMode.CONTACTS
        binding.contactsListLayout.visibility = View.VISIBLE
        binding.userSearchResultsLayout.visibility = View.GONE
        binding.toolbar.btnNotification.visibility = View.GONE
    }

    private fun showLimorUsers(){
        mode = FriendSelectionMode.LIMOR_USERS
        binding.contactsListLayout.visibility = View.GONE
        binding.userSearchResultsLayout.visibility = View.VISIBLE
        binding.toolbar.btnNotification.visibility = View.VISIBLE
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                loaderManager.initLoader(0, null, this)
                showContactsLayout()
            }
        }

    private fun hasPermissions(context: Context, vararg permissions: String): Boolean =
        permissions.all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    companion object {
        private const val PERMISSION_ALL = 1
        private val PERMISSIONS = arrayOf(
            Manifest.permission.READ_CONTACTS
        )
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        selectionArgs[0] = "%$searchString%"
        return requireContext().let {
            return CursorLoader(
                it,
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                if(searchString.trim().isEmpty()) null else SELECTION,
                if(searchString.trim().isEmpty()) null else selectionArgs,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
                else
                    "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        data?.moveToFirst()
        var result = ArrayList<ContactUIModel>()
        while (data?.isAfterLast == false) {
            val contact = ContactUIModel(data.getInt(0), data.getString(2), data.getString(3), false)
            result.add(contact)
            data?.moveToNext()
        }
        contactsAdapter?.setContacts(result)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        contactsAdapter?.setContacts(ArrayList())
        //cursorAdapter?.swapCursor(null)
    }

    enum class FriendSelectionMode{
        LIMOR_USERS,
        CONTACTS
    }

}