package ui.birds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.suresh.http.Image
import dev.suresh.http.MediaApiClient
import dev.suresh.http.Video
import dev.suresh.log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BirdUiState(
    val images: List<Image> = emptyList(),
    val videos: List<Video> = emptyList()
) {

  val categories by lazy { images.map { it.category }.distinct() }
}

class BirdsViewModel : ViewModel() {
  init {
    log.info { "Creating BirdsViewModel" }
  }

  private val client = MediaApiClient()

  private val _uiState = MutableStateFlow(BirdUiState())

  val uiState = _uiState.asStateFlow()

  fun update() {
    log.info { "Retrieving the images and vide info..." }
    viewModelScope.launch {
      val images = client.images()
      val videos = client.videos()
      log.info { "Got ${images.size} images and ${videos.size} videos" }
      _uiState.update { state -> state.copy(images = images, videos = videos) }
    }
  }

  override fun onCleared() {
    log.info { "Closing the client..." }
    super.onCleared()
    client.close()
  }
}
