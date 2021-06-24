package com.limor.app.scenes.main.fragments.discover.discover

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limor.app.scenes.main.fragments.discover.common.mock.MockCast
import com.limor.app.scenes.main.fragments.discover.common.mock.MockPerson
import com.limor.app.uimodels.CategoryUIModel
import com.limor.app.usecases.GetCategoriesUseCase
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

class DiscoverViewModel @Inject constructor(
    val getCategoriesUseCase: GetCategoriesUseCase
) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryUIModel>>()
    val categories: LiveData<List<CategoryUIModel>> = _categories

    private val _suggestedPeople = MutableLiveData<List<MockPerson>>()
    val suggestedPeople: LiveData<List<MockPerson>> = _suggestedPeople

    private val _featuredCasts = MutableLiveData<List<MockCast>>()
    val featuredCasts: LiveData<List<MockCast>> = _featuredCasts

    private val _topCasts = MutableLiveData<List<MockCast>>()
    val topCasts: LiveData<List<MockCast>> = _topCasts

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

    private val mockCastsList
        get() = mutableListOf<MockCast>()
            .apply {
                repeat(21) {
                    add(
                        MockCast(
                            location = generateRandomLocation(),
                            playProgress = generatePlayProgress(),
                            duration = generateRandomDuration(),
                            owner = mockPersonList.random(),
                            date = generateRandomDateInPast(),
                            name = generateRandomName(),
                            imageUrl = generateRandomImageUrl()
                        )
                    )
                }
            }

    init {
        loadCategories()
        generateSuggestedPeople()
        generateFeaturedCasts()
        generateTopCasts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategoriesUseCase.execute()
                .onSuccess {
                    _categories.value = it
                }
                .onFailure {
                    Timber.e("Error while getting categories: $it")
                }
        }
    }

    @Deprecated("As soon as API is available this has to be removed")
    private fun generateSuggestedPeople() {
        _suggestedPeople.value = mockPersonList
    }

    @Deprecated("As soon as API is available this has to be removed")
    private fun generateFeaturedCasts() {
        _featuredCasts.value = mockCastsList
    }

    @Deprecated("As soon as API is available this has to be removed")
    private fun generateTopCasts() {
        _topCasts.value = mockCastsList
    }

    private fun generateRandomDateInPast(): LocalDateTime {
        return LocalDateTime.now().minusDays((0..5).random().toLong())
    }

    private fun generateRandomDuration(): Duration {
        return Duration.ofSeconds((30..500).random().toLong())
    }

    private fun generatePlayProgress(): Int {
        return (0..100).random()
    }

    private fun generateRandomLocation(): String {
        return listOf("Warsaw", "Dublin", "London", "Paris", "Moscow", "Chicago").random()
    }

    private fun generateRandomName(): String {
        return listOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
            "Proin fermentum leo vel orci porta non pulvinar neque laoreet",
            "Non tellus orci ac auctor",
            "Faucibus nisl tincidunt",
            "Amet tellus cras adipiscing enim. Condimentum mattis pellentesque id nibh tortor id aliquet lectus proin. Posuere ac ut consequat semper viverra nam libero.",
            "Sollicitudin nibh sit amet",
            "Neque laoreet",
            "Turpis nunc eget lorem",
            "Varius duis at consectetur",
        ).random()
    }

    private fun generateRandomImageUrl(): String {
        return listOf(
            "https://assets.unenvironment.org/s3fs-public/styles/topics_content_promo/public/2021-05/alberta-2297204_1920.jpg?itok=aim5GFuY",
            "https://static.scientificamerican.com/sciam/cache/file/4E0744CD-793A-4EF8-B550B54F7F2C4406_source.jpg",
            "https://www.eea.europa.eu/themes/biodiversity/state-of-nature-in-the-eu/state-of-nature-2020-subtopic/image_large",
            "https://www.happybrainscience.com/wp-content/uploads/2017/07/derwent-morning-Cropped.jpg",
            "https://images.indianexpress.com/2017/04/nature-tree_759.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Books_HD_%288314929977%29.jpg/800px-Books_HD_%288314929977%29.jpg",
            "https://media.npr.org/assets/img/2020/09/18/gettyimages-1170941183-c1e33a9a2b274d5381f14d18f71db5ee0342aec3-s800-c85.jpg",
            "https://ychef.files.bbci.co.uk/976x549/p03gg1lc.jpg",
            "https://e3.365dm.com/20/12/768x432/skynews-books-bookshop-generic_5200042.jpg",
        ).random()
    }
}