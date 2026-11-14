package com.oscar.orbimusic.presentation.viewmodel

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.oscar.orbimusic.data.audio.AudioPlayerService
import com.oscar.orbimusic.data.audio.toMediaItem
import com.oscar.orbimusic.data.local.database.entities.PlaySource
import com.oscar.orbimusic.domain.models.PlayerState
import com.oscar.orbimusic.domain.models.Playlist
import com.oscar.orbimusic.domain.models.PlaylistSelection
import com.oscar.orbimusic.domain.models.PlaylistSummary
import com.oscar.orbimusic.domain.models.Song
import com.oscar.orbimusic.domain.repository.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class MediaViewModel (private val songRepository: SongRepository,
                      private val context: Context): ViewModel() {
    private var controller: MediaController? = null;
    private var controllerFuture: ListenableFuture<MediaController>? = null;
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()
    private val _allSongs = MutableStateFlow<List<Song>>(emptyList());
    val allSongs: StateFlow<List<Song>> = _allSongs.asStateFlow();
    private val _currentPosition = MutableStateFlow(0L);
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow();
    private val _duration = MutableStateFlow(0L);
    val duration: StateFlow<Long> = _duration.asStateFlow();

    private val _isExpanded = MutableStateFlow<Boolean>(false);
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow();

    private val _nextSong = MutableStateFlow<Song?>(null)
    val nextSong: StateFlow<Song?> = _nextSong.asStateFlow()

    private val _shuffle =  MutableStateFlow<Boolean>(false);
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow();

    private val _repeat = MutableStateFlow<Boolean>(false);
    val repeat: StateFlow<Boolean> = _repeat.asStateFlow();


    //Los dos se siguen excluyendo entre si, pero ahora ademas se le pasan al
    //player: el shuffle y el repeat los resuelve el, no playNext() a mano.
    fun setRepeat(status: Boolean){
        _repeat.value = status;

        if(_shuffle.value){
            _shuffle.value = false
            controller?.shuffleModeEnabled = false
        }

        controller?.repeatMode =
            if (status) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }
    fun setShuffle(status: Boolean){
        _shuffle.value = status;

        if(_repeat.value){
            _repeat.value = false
        }

        controller?.shuffleModeEnabled = status
        controller?.repeatMode = if (status) Player.REPEAT_MODE_ALL else Player.REPEAT_MODE_OFF
    }


    fun setExpanded(status: Boolean){
        _isExpanded.value = status;
    };

    private data class PendingPlay(
        val songId: Long,
        val durationMs: Long,
        val shuffle: Boolean,
        val source: String
    )

    private var pendingPlay: PendingPlay? = null

    private fun openPlay(song: Song, source: String) {
        pendingPlay = PendingPlay(
            songId = song.id,
            durationMs = song.duration,
            shuffle = _shuffle.value,
            source = source
        )
    }

    private fun closePlay(msPlayed: Long) {
        val play = pendingPlay ?: return
        pendingPlay = null

        if (msPlayed <= 0) return

        viewModelScope.launch {
            songRepository.recordPlay(
                songId = play.songId,
                msPlayed = msPlayed,
                shuffle = play.shuffle,
                source = play.source
            )
        }
    }

    private fun closePlayAtCurrentPosition() {
        closePlay(controller?.currentPosition ?: 0L)
    }




    init {
        loadAllSongs()
        Log.d("OSCAR","${allSongs}");
        connectToAudioService()
        startUpdatingProgress()
        getNextSong()
    }

    private fun startUpdatingProgress() {
        viewModelScope.launch {
            while(true){
                controller?.let { player ->
                    _currentPosition.value = player.currentPosition;
                    //duration vale C.TIME_UNSET (negativo) hasta que el player
                    //prepara la pista; sin acotarlo el slider recibe basura
                    _duration.value = player.duration.coerceAtLeast(0L);
                }
                delay(500.milliseconds);
            }
        }
    }

    //Mismo contenido que el viejo setPlayerStateListener, solo cambia el
    //envoltorio: ahora son callbacks de Player en vez de un callback propio.
    private val playerListener = object : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncPlayerState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncPlayerState()
        }
    }

    private fun syncPlayerState() {
        val player = controller ?: return
        //Con shuffle activo currentMediaItemIndex sigue indexando la lista
        //original: el orden barajado es una traversia aparte. Por eso
        //queue[index] sigue siendo valido.
        val index = player.currentMediaItemIndex
        val isPlaying = player.isPlaying

        val queue = _playerState.value.queue
        val newSong = queue.getOrNull(index)
        val pending = pendingPlay

        if (newSong != null && pending != null && pending.songId != newSong.id) {
            closePlay(pending.durationMs)
            openPlay(newSong, pending.source)
        }

        _playerState.update { currentState ->
            currentState.copy(
                isPlaying = isPlaying,
                currentIndex = index,
                currentSong = if (currentState.queue.isNotEmpty() && index < currentState.queue.size) {
                    currentState.queue[index]
                } else currentState.currentSong
            )
        }
    }

    private fun connectToAudioService() {
        val token = SessionToken(
            context,
            ComponentName(context, AudioPlayerService::class.java)
        )
        controllerFuture = MediaController.Builder(context, token).buildAsync().also { future ->
            future.addListener({
                controller = future.get()
                controller?.addListener(playerListener)
                //Si el usuario ya habia tocado shuffle/repeat antes de que el
                //controller existiera, el player no se habria enterado
                controller?.shuffleModeEnabled = _shuffle.value
                controller?.repeatMode = when {
                    _repeat.value  -> Player.REPEAT_MODE_ONE
                    _shuffle.value -> Player.REPEAT_MODE_ALL
                    else           -> Player.REPEAT_MODE_OFF
                }
            }, MoreExecutors.directExecutor())
        }
    }
    public fun loadAllSongs(){
        viewModelScope.launch {
            try{
                songRepository.getAllSongs().collect { songs ->
                    _allSongs.value = songs;
                }
            }finally {
                null
            }
        }
    }

    public fun loadAllSongsByArtist(){
        viewModelScope.launch {
            try {
                songRepository.getOrderedByArtist().collect { songs ->
                    _allSongs.value = songs
                }
            }finally {
                null
            }
        }
    }

    public fun loadAllSongsByTitle(){
        viewModelScope.launch {
            try{
                songRepository.getOrderedByName().collect { songs ->
                    _allSongs.value = songs
                }

            }finally{
                null
            }
        }
    }



    // ---------- FAVORITOS ----------

    val favoriteIds: StateFlow<Set<Long>> = songRepository.getFavoriteIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    //La lista de favoritos, ya como canciones completas
    val favoriteSongs: StateFlow<List<Song>> = songRepository.getFavoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Marca o desmarca. No hay que leer el estado antes: el repositorio lo
    //resuelve en una sola operacion.
    fun toggleFavorite(songId: Long) {
        viewModelScope.launch {
            songRepository.toggleFavorite(songId)
        }
    }

    fun removeFavorite(songId: Long) {
        viewModelScope.launch {
            songRepository.removeFavorite(songId)
        }
    }


    // ---------- PLAYLISTS ----------

    //Cada playlist ya trae su conteo de canciones
    val playlists: StateFlow<List<Playlist>> = songRepository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Primer nivel: Favoritos y las playlists con su conteo, sin canciones
    val playlistSummaries: StateFlow<List<PlaylistSummary>> =
        songRepository.getPlaylistSummaries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    //Que playlist esta abierta en el segundo nivel. Null = primer nivel.
    private val _openPlaylist = MutableStateFlow<PlaylistSelection?>(null);
    val openPlaylist: StateFlow<PlaylistSelection?> = _openPlaylist.asStateFlow();
    private val _pendingPlaylist = MutableStateFlow<PlaylistSelection?>(null);
    val pendingPlaylist: StateFlow<PlaylistSelection?> = _pendingPlaylist.asStateFlow();

    private val _pendingCreatePlaylist = MutableStateFlow(false);
    val pendingCreatePlaylist: StateFlow<Boolean> = _pendingCreatePlaylist.asStateFlow();

    fun requestOpenPlaylist(selection: PlaylistSelection) {
        _pendingPlaylist.value = selection;
    }

    fun requestCreatePlaylist() {
        _pendingCreatePlaylist.value = true;
    }

    fun consumePendingPlaylist() {
        _pendingPlaylist.value = null;
    }

    fun consumePendingCreatePlaylist() {
        _pendingCreatePlaylist.value = false;
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val playlistSongs: StateFlow<List<Song>> = _openPlaylist
        .flatMapLatest { selection ->
            when (selection) {
                null -> flowOf(emptyList())
                //Favoritos vive en su propia tabla, no en playlistDetalle
                is PlaylistSelection.Favorites -> songRepository.getFavoriteSongs()
                is PlaylistSelection.Real -> songRepository.getPlaylistSongs(selection.id)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun openPlaylist(selection: PlaylistSelection) {
        _openPlaylist.value = selection;
    }

    fun closePlaylist() {
        _openPlaylist.value = null;
    }

    //En que playlists esta una cancion, para el selector de agregar
    fun playlistIdsContaining(songId: Long): Flow<Set<Long>> =
        songRepository.playlistIdsContaining(songId)

    //Devuelve el id de la playlist nueva por si la pantalla quiere abrirla
    fun createPlaylist(name: String, description: String = "", onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            onCreated(songRepository.createPlaylist(name, description))
        }
    }

    fun deletePlaylist(idPlaylist: Long) {
        viewModelScope.launch {
            songRepository.deletePlaylist(idPlaylist)
            //Si estaba abierta, se cierra: su id ya no existe
            if (_openPlaylist.value == PlaylistSelection.Real(idPlaylist)) {
                _openPlaylist.value = null
            }
        }
    }

    fun addSongToPlaylist(idPlaylist: Long, songId: Long) {
        viewModelScope.launch {
            songRepository.addSongToPlaylist(idPlaylist, songId)
        }
    }

    fun removeSongFromPlaylist(idPlaylist: Long, songId: Long) {
        viewModelScope.launch {
            songRepository.removeSongFromPlaylist(idPlaylist, songId)
        }
    }

    //Recibe la lista completa en su orden final, no un movimiento suelto
    fun reorderPlaylist(idPlaylist: Long, songIdsInOrder: List<Long>) {
        viewModelScope.launch {
            songRepository.reorderPlaylist(idPlaylist, songIdsInOrder)
        }
    }


    //Reproduce una lista cualquiera como cola.
    fun playSongs(songs: List<Song>, index: Int = 0, source: String = PlaySource.LIBRARY) {
        if (songs.isEmpty()) return
        val safeIndex = index.coerceIn(0, songs.lastIndex)
        closePlayAtCurrentPosition()

        _playerState.update { currentState ->
            currentState.copy(
                currentSong = songs[safeIndex],
                isPlaying = true,
                currentIndex = safeIndex,
                queue = songs
            )
        }

        _currentPosition.value = 0L

        setQueue(songs, safeIndex)

        openPlay(songs[safeIndex], source)
    }
    fun deleteSong(song: Song) {
        viewModelScope.launch {
            if (_playerState.value.currentSong?.path == song.path) {
                pauseSong()
            }
            songRepository.deleteSong(song)
        }
    }


    fun playSong(song: Song?, index: Int = 0, source: String = PlaySource.LIBRARY) {
        //Igual que en playSongs: la posicion se lee antes del cambio
        closePlayAtCurrentPosition()

        _playerState.update { currentState ->
            currentState.copy(
                currentSong = song,
                isPlaying = true,
                currentIndex = index,
                queue = _allSongs.value,
            )
        }

        _currentPosition.value = 0L

        setQueue(_allSongs.value, index)

        song?.let { openPlay(it, source) }
    }

    //Unico sitio donde se arma la cola del player. Antes esto vivia en
    //AudioPlayerService.setPlaylist y se llamaba en cada cambio de pista;
    //ahora solo se llama cuando la cola cambia de verdad.
    private fun setQueue(songs: List<Song>, startIndex: Int) {
        val player = controller ?: return
        player.setMediaItems(songs.map { it.toMediaItem() }, startIndex, 0L)
        player.prepare()
        player.play()
    }

    fun pauseSong() {
        _playerState.update { it.copy(isPlaying = false) }
        controller?.pause()
    }

    fun togglePlayPause() {
        _playerState.update { currentState ->
            currentState.copy(isPlaying = !currentState.isPlaying)
        }

        if (_playerState.value.isPlaying) {
            controller?.play()
        } else {
            controller?.pause()
        }
    }

    fun getNextSong() {
        viewModelScope.launch {
            _playerState.collect { state ->
                //nextMediaItemIndex respeta el orden barajado; antes era
                //currentIndex + 1, que mentia con el shuffle activo.
                //C.INDEX_UNSET (-1) significa que no hay siguiente.
                val nextIndexSong = controller?.nextMediaItemIndex ?: (state.currentIndex + 1)
                _nextSong.value = state.queue.getOrNull(nextIndexSong)
            }
        }
    }


    fun playNext() {
        if (_repeat.value) {
            controller?.seekTo(0L)
            return
        }
        controller?.seekToNextMediaItem()
    }

    //seekToPrevious ya trae la regla de los 3 segundos: si la posicion supera
    //maxSeekToPreviousPosition (3000 ms por defecto) vuelve al inicio de la
    //pista, si no salta a la anterior.
    fun playPrevious() {
        controller?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
        controller?.seekTo(position)
    }

    fun updatePosition(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
    }


    override fun onCleared() {
        super.onCleared()
        //Sin esto se pierde siempre la ultima reproduccion de cada sesion.
        //Va antes de soltar el controller porque lee su posicion.
        closePlayAtCurrentPosition()
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
    }

}