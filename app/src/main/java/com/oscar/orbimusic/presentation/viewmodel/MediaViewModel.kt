package com.oscar.orbimusic.presentation.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oscar.orbimusic.data.audio.AudioPlayerService
import com.oscar.orbimusic.data.audio.AudioServiceConnection
import com.oscar.orbimusic.data.local.database.PlaySource
import com.oscar.orbimusic.domain.model.PlayerState
import com.oscar.orbimusic.domain.model.Playlist
import com.oscar.orbimusic.domain.model.PlaylistSelection
import com.oscar.orbimusic.domain.model.PlaylistSummary
import com.oscar.orbimusic.domain.model.Song
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
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class MediaViewModel (private val songRepository: SongRepository,
                      private val context: Context): ViewModel() {


    private var audioService: AudioPlayerService? = null;
    private lateinit var serviceConnection: AudioServiceConnection;
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


    fun setRepeat(status: Boolean){
        _repeat.value = status;

        if(_shuffle.value){
            _shuffle.value = false
        }
    }
    fun setShuffle(status: Boolean){
        _shuffle.value = status;

        if(_repeat.value){
            _repeat.value = false
        }
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
        closePlay(audioService?.getCurrentPosition() ?: 0L)
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
                audioService?.let { service ->
                    _currentPosition.value = service.getCurrentPosition();
                    _duration.value = service.getDuration();
                }
                delay(500.milliseconds);
            }
        }
    }

    private fun connectToAudioService() {
        serviceConnection = AudioServiceConnection(
            callback = { service ->
                audioService = service
                service.setPlayerStateListener { isPlaying, index ->
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
            }
        )
        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
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

        audioService?.setPlaylist(songs, safeIndex)
        audioService?.play()

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

        audioService?.setPlaylist(_allSongs.value, index)
        audioService?.play()

        song?.let { openPlay(it, source) }
    }

    fun pauseSong() {
        _playerState.update { it.copy(isPlaying = false) }
        audioService?.pause()
    }

    fun togglePlayPause() {
        _playerState.update { currentState ->
            currentState.copy(isPlaying = !currentState.isPlaying)
        }

        if (_playerState.value.isPlaying) {
            audioService?.play()
        } else {
            audioService?.pause()
        }
    }

    fun getNextSong() {
        viewModelScope.launch {
            _playerState.collect { state ->
                val nextIndexSong = state.currentIndex + 1;
                _nextSong.value = if(nextIndexSong < state.queue.size){
                    state.queue[nextIndexSong];
                }else null
            }
        }
    }

    fun playNext() {
        val currentState = _playerState.value
        val current = currentState.currentIndex;
        var nextIndex = currentState.currentIndex + 1

        if(_repeat.value){
            playSong(currentState.queue[current], current)
            return;
        }

        if(_shuffle.value){
            nextIndex = Random.nextInt(0, currentState.queue.size);
            if(nextIndex == current){
                nextIndex = Random.nextInt(0, currentState.queue.size);
            }
        }

        if (nextIndex < currentState.queue.size) {
            playSong(currentState.queue[nextIndex], nextIndex)
        }

    }

    fun playNextNoShuffle(){
        val currentState = _playerState.value
        val current = currentState.currentIndex;
        val nextIndex = currentState.currentIndex + 1

        if (nextIndex < currentState.queue.size) {
            playSong(currentState.queue[nextIndex], nextIndex)
        }
    }



    fun playPrevious() {
        val currentState = _playerState.value

        if (currentState.currentPosition > 3000) {
            _playerState.update { it.copy(currentPosition = 0) }
            audioService?.seekTo(0)
        } else {
            val prevIndex = currentState.currentIndex - 1
            if (prevIndex >= 0) {
                playSong(currentState.queue[prevIndex], prevIndex)
            }
        }
    }

    fun seekTo(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
        audioService?.seekTo(position)
    }

    fun updatePosition(position: Long) {
        _playerState.update { it.copy(currentPosition = position) }
    }


    override fun onCleared() {
        super.onCleared()
        //Sin esto se pierde siempre la ultima reproduccion de cada sesion
        closePlayAtCurrentPosition()
        context.unbindService(serviceConnection)
    }

}