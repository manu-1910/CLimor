package com.limor.app.scenes.patron.manage.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
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
import com.limor.app.uimodels.ContactUIModel
import javax.inject.Inject

class FragmentInviteFriends : Fragment(), Injectable, LoaderManager.LoaderCallbacks<Cursor> {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val model: ManagePatronViewModel by activityViewModels { viewModelFactory }

    @SuppressLint("InlinedApi")
    private val PROJECTION: Array<out String> = arrayOf(
        ContactsContract.Contacts._ID,
        ContactsContract.Contacts.LOOKUP_KEY,
        ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
        ContactsContract.Data.PHOTO_URI
    )

    @SuppressLint("InlinedApi")
    private val SELECTION: String =
        "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} LIKE ?"
    private var searchString: String = ""
    private val selectionArgs = arrayOf<String>(searchString)

    private var contactsAdapter: ContactsListAdapter? = null
    private var limorUsersAdapter: InviteLimorUsersAdapter? = null

    private lateinit var binding: FragmentInviteFriendsBinding
    private var mode: InviteMode = InviteMode.LIMOR_USERS

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
        subscribeViewModels()
    }

    private fun initialiseViews() {
        binding.toolbar.title.text = getString(R.string.invite_friends)
        binding.toolbar.btnNotification.setImageDrawable(resources.getDrawable(R.drawable.ic_phone_book))

        contactsAdapter = ContactsListAdapter(
            ArrayList(),
            onSelected = { count ->
                updateInviteButton(count)
            }
        )
        binding.contactsListView.adapter = contactsAdapter

        limorUsersAdapter = InviteLimorUsersAdapter(
            ArrayList(),
            onSelected = { count ->
                updateCountText(count)
            }
        )
        binding.userSearchResultsRecyclerView.adapter = limorUsersAdapter

        updateCountText(5)
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

    private fun subscribeViewModels() {
        model.searchResult.observe(viewLifecycleOwner, {
            if(binding.userSearchResultsLayout.visibility == View.GONE){
                binding.userSearchResultsLayout.visibility = View.VISIBLE
            }
            limorUsersAdapter?.setUsers(it)
        })
    }

    private fun setClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            if (mode == InviteMode.CONTACTS) {
                showLimorUsers()
            } else {
                model.clearUserSearchResults()
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
            } else {
                loaderManager.initLoader(0, null, this)
                showContactsLayout()
            }
        }
    }

    private fun performSearch(searchItem: String) {
        searchString = searchItem
        when (mode) {
            InviteMode.CONTACTS -> {
                loaderManager.restartLoader(0, null, this)
            }
            else -> {
                if (searchString.length > 3) {
                    model.search(searchItem)
                } else{
                    hideLimorUsersLayout()
                }
            }
        }
    }

    private fun showContactsLayout() {
        mode = InviteMode.CONTACTS
        binding.contactsListLayout.visibility = View.VISIBLE
        binding.userSearchResultsLayout.visibility = View.GONE
        binding.toolbar.btnNotification.visibility = View.GONE
        performSearch(searchString)
    }

    private fun showLimorUsers() {
        mode = InviteMode.LIMOR_USERS
        binding.contactsListLayout.visibility = View.GONE
        binding.userSearchResultsLayout.visibility = View.VISIBLE
        binding.toolbar.btnNotification.visibility = View.VISIBLE
        performSearch(searchString)
    }

    private fun hideContactsLayout(){
        binding.contactsListLayout.visibility = View.GONE
        contactsAdapter?.setContacts(ArrayList())
    }

    private fun hideLimorUsersLayout(){
        binding.userSearchResultsLayout.visibility = View.GONE
        limorUsersAdapter?.setUsers(ArrayList())
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
            val contact =
                ContactUIModel(data.getInt(0), data.getString(2), data.getString(3), false)
            result.add(contact)
            data.moveToNext()
        }
        contactsAdapter?.setContacts(result)
        //binding.contactsListLayout.visibility = View.VISIBLE
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        hideContactsLayout()
    }

    enum class InviteMode {
        LIMOR_USERS,
        CONTACTS
    }

}