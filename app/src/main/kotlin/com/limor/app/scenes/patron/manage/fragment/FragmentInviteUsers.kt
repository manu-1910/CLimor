package com.limor.app.scenes.patron.manage.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.limor.app.R
import com.limor.app.databinding.FragmentInviteUsersBinding
import com.limor.app.di.Injectable
import com.limor.app.scenes.patron.manage.adapters.ContactsListAdapter
import com.limor.app.scenes.patron.manage.adapters.InviteLimorUsersAdapter
import com.limor.app.scenes.patron.manage.viewmodels.ManagePatronViewModel
import com.limor.app.scenes.utils.CommonsKt
import javax.inject.Inject

class FragmentInviteUsers : Fragment(), Injectable {

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

    private lateinit var binding: FragmentInviteUsersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInviteUsersBinding.inflate(layoutInflater, container, false)
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
        binding.toolbar.btnNotification.setImageDrawable(getDrawable(requireContext(),
            R.drawable.ic_phone_book))
        limorUsersAdapter = InviteLimorUsersAdapter(
            ArrayList(),
           onSelected = {
               //invite
               model.inviteInternalUsers(it.id)
               updateCountText(CommonsKt.user?.availableInvitations?:0)
           }
        )
        performSearch("")
        limorUsersAdapter?.setUsers(ArrayList())
        binding.userSearchResultsRecyclerView.adapter = limorUsersAdapter

        Log.d("INVITE count ->","${CommonsKt.user?.availableInvitations}")
        updateCountText(CommonsKt.user?.availableInvitations?:0)
    }


    private fun updateCountText(count: Int) {
        binding.inviteCountDescription.text =
            resources.getString(R.string.invites_count_description, count)
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
            Log.d(" Users ", "-> $it")
                binding.userSearchResultsLayout.visibility = View.VISIBLE
            limorUsersAdapter?.setUsers(it)
        })
    }

    private fun setClickListeners() {
        binding.toolbar.btnBack.setOnClickListener {
            model.clearUserSearchResults()
            findNavController().navigateUp()
        }
        binding.toolbar.btnNotification.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentInviteUsers_to_fragment_invite_friends)
        }
    }

    private fun performSearch(searchItem: String) {
        searchString = searchItem
        if (searchString.length > 3) {
            model.search(searchItem)
        } else{
            hideLimorUsersLayout()
        }
    }



    private fun hideLimorUsersLayout(){
        binding.userSearchResultsLayout.visibility = View.GONE
        limorUsersAdapter?.setUsers(ArrayList())
    }



}