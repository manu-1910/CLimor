package com.limor.app.scenes.main.fragments.discover.suggestedpeople

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DiscoverSuggestedPeopleViewModel @Inject constructor(
    // some repo to get suggested people
): ViewModel() {

    private val _suggestedPeople = MutableLiveData<List<MockPerson>>()
    val suggestedPeople: LiveData<List<MockPerson>> = _suggestedPeople

    private val mockPersonList = listOf(
        MockPerson(
            name = "Test Person",
            nickName = "@testPerson",
            imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-demo-photo-0c.jpg",
            isFollowed = false
        ),
        MockPerson(
            name = "Test Person2",
            nickName = "@testPerson2",
            imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/04.jpg",
            isFollowed = true
        ),
        MockPerson(
            name = "Test Person3",
            nickName = "@testPerson3",
            imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/08.jpg",
            isFollowed = true
        ),
        MockPerson(
            name = "Test Person4",
            nickName = "@testPerson4",
            imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/05.jpg",
            isFollowed = true
        ),
        MockPerson(
            name = "Test Person5",
            nickName = "@testPerson5",
            imageUrl = "https://d1r8m46oob3o9u.cloudfront.net/images/home-page-examples/06.jpg",
            isFollowed = false
        ),
    )

    init {
        viewModelScope.launch {
            delay(1000)
            _suggestedPeople.value = mockPersonList
        }
    }

}