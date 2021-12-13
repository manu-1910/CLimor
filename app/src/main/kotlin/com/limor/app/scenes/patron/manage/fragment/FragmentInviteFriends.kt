package com.limor.app.scenes.patron.manage.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.FragmentInviteFriendsBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.patron.manage.adapters.ContactsListAdapter
import com.limor.app.scenes.patron.manage.adapters.InviteLimorUsersAdapter
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import com.limor.app.scenes.utils.CommonsKt
import com.limor.app.uimodels.ContactUIModel
import org.jetbrains.anko.design.snackbar
import javax.inject.Inject

class FragmentInviteFriends : Fragment(), Injectable, LoaderManager.LoaderCallbacks<Cursor> {

    private var selectedContacts = ArrayList<ContactUIModel>()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }

    @SuppressLint("InlinedApi")
    private val PROJECTION: Array<out String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Data.DISPLAY_NAME_PRIMARY,
        ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    @SuppressLint("InlinedApi")
    private val SELECTION: String =
        "${ContactsContract.Contacts.HAS_PHONE_NUMBER} = ? and ${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ? and ${ContactsContract.RawContacts.ACCOUNT_TYPE} = ?"
    private var searchString: String = ""
    private val selectionArgs = arrayOf<String>("1",searchString,"com.google")

    private var contactsAdapter: ContactsListAdapter? = null
    private var limorUsersAdapter: InviteLimorUsersAdapter? = null

    private lateinit var binding: FragmentInviteFriendsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        binding.toolbar.btnNotification.visibility = View.GONE

        contactsAdapter = ContactsListAdapter(
            ArrayList(),
            onSelected = { contacts ->
                selectedContacts = contacts as ArrayList<ContactUIModel>
                updateInviteButton(contacts.size)
            }
        )
        binding.contactsListView.adapter = contactsAdapter

        updateCountText(CommonsKt.user?.availableInvitations?:0)

        if (!hasPermissions(requireContext(), *PERMISSIONS)) {
            try {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else {
            LoaderManager.getInstance(this).initLoader(0, null, this)
            showContactsLayout()
        }
    }

    private fun updateInviteButton(count: Int) {
        if (count > 0) {
            binding.inviteButton.text = resources.getString(R.string.invite_people, count)
            binding.inviteButton.isEnabled = true
        } else {
            binding.inviteButton.text = resources.getString(R.string.invite)
            binding.inviteButton.isEnabled = false
        }
    }

    private fun updateCountText(count: Int) {
        if (count > 0) {
            binding.inviteCountDescription.visibility = View.VISIBLE
            binding.inviteCountDescription.text =
                resources.getString(R.string.invites_count_description, count)
        } else {
            binding.inviteCountDescription.visibility = View.GONE
            binding.inviteCountDescription.text =
                resources.getString(R.string.invites_count_description, count)
        }
    }

    private fun setTextWatchers() {
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
            model.clearUserSearchResults()
            findNavController().navigateUp()
        }
        binding.toolbar.btnNotification.visibility = View.GONE

        binding.inviteButton.setOnClickListener {
            val numbers = selectedContacts.mapNotNull { it.phoneNumber }
            if(numbers.size>CommonsKt.user?.availableInvitations?:0){
                binding.root.snackbar("You can only invite ${CommonsKt.user?.availableInvitations} people")
            }else{
                CommonsKt.user?.availableInvitations = numbers.size
                model.inviteExternal(numbers)
            }

        }
    }

    private fun performSearch(searchItem: String) {
        searchString = searchItem
        LoaderManager.getInstance(this).restartLoader(0, null, this)
    }

    private fun showContactsLayout() {
        binding.contactsListLayout.visibility = View.VISIBLE
        binding.toolbar.btnNotification.visibility = View.GONE
        performSearch(searchString)
    }



    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                LoaderManager.getInstance(this).initLoader(0, null, this)
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
        selectionArgs[1] = "%$searchString%"
        return requireContext().let {
            return CursorLoader(
                it,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION,
                if (searchString.trim().isEmpty()) null else SELECTION,
                if (searchString.trim().isEmpty()) null else selectionArgs,
                "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"
            )
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        data?.moveToFirst()
        var result = ArrayList<ContactUIModel>()
        while (data?.isAfterLast == false) {
           val phoneNumber =
                data.getString(4)
            Log.d("CONTACT_DATA", phoneNumber)
            val contact = ContactUIModel(data.getInt(0),
                data.getString(2),
                data.getString(3),
                false,
                phoneNumber)
            result.add(contact)
            data.moveToNext()
        }
        contactsAdapter?.setContacts(result)
        //binding.contactsListLayout.visibility = View.VISIBLE
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        contactsAdapter?.setContacts(ArrayList())
    }


}