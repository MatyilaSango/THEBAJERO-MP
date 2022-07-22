/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thebajero.mp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author Sango
 */
public class FXMLPlayerController implements Initializable {
    
    @FXML
    private ImageView prevButton, stopButton, playButton, pauseButton, nextButton, repeatButton, shufflebutton, addMedia, volumePic, songImageDet, audioVideoSwitch;
    
    @FXML
    private Text songNameDet,songArtistDet, songAlbumDet, songDurationDet, timeProgressBar, currentSongTime;
    
    @FXML
    private AnchorPane repeatPane, shufflePane, musicPane, albumPane, playlistPane, MusicAlbumPlaylistPane, videoPane, audioVideoPane;
    
    @FXML
    private Slider volumeSlider, timer;
    
    @FXML
    private ProgressBar mediaBar;
    
    @FXML
    private ListView<String> playList, musicList, albumList;
    
    @FXML
    private TextField searcher;
    
    @FXML 
    MediaView mediaView;
    
    List<File> mediaFiles = null;
    List<File> newPlayList = null;
    Media media = null;
    MediaPlayer mediaPlayer = null;
    int current = 0;
    double currentTime = 0;
    boolean repeat = false;
    boolean shuffle = false;
    boolean isPlaying = false;
    String videoFormats = "avi wmv wmp wm asf mpg mpeg mpe m1v m2v mpv2 mp2v ts tp tpr trp vob ifo ogm ogv mp4 m4v m4p m4b 3gp 3gpp 3g2 3gp2 mkv rm ram rmvb rpm flv swf mov qt nsv dpg m2ts m2t mts dvr-ms k3g skm evo nsr amv divx webm wtv f4v mxf gif";
    String audioFormats = "wav wma mpa mp2 m1a m2a mp3 ogg m4a aac mka ra flac ape mpc mod ac3 eac3 dts dtshd wv tak cda dsf tta aiff aif opus amr m4a";
    String configFile = "C:\\Users\\Sango\\Documents\\NetBeansProjects\\THEBAJERO MP\\src\\thebajero\\mp\\Configurations.config";
    Thread threadHandleTimeBar = new Thread();
    Thread threadPlayMedia = new Thread();
    Thread treadPlaySelectedPlaylist = new Thread();
    boolean handleTime = false;
    
    /**
     * Load medias from default music folder
     * @throws java.io.FileNotFoundException
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void loadMusic() throws FileNotFoundException, IOException, InterruptedException{
        String username = System.getProperty("user.name");
        File folder = new File("C:\\Users\\"+username+"\\Music\\");
        File[] files = folder.listFiles();
        this.toMusic(files);

        Scanner sc = new Scanner(new File(configFile));
        String configString = new String();
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            switch(line.split(":")[0]){    
                case "mute":
                    continue;
                case "shuffle":
                    shuffle = (Integer.parseInt(line.split(":")[1].replaceAll(";", "").trim()) == 0);
                    this.clickShuffleButton();
                    continue;
                case "repeat":
                    repeat = (Integer.parseInt(line.split(":")[1].replaceAll(";", "").trim()) == 0);
                    this.clickRepeatButton();
                    continue;
                case "volume":
                    volumeSlider.setValue(Double.parseDouble(line.split(":")[1].replaceAll(";", "").trim())*100);
            }
            
        }
        
    }
    
    /**
     * Load medias from chosen directory.
     */
    public void myPrefMusic(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage) addMedia.getScene().getWindow();
        File folder = directoryChooser.showDialog(stage);

        if(folder != null){
            File[] files = folder.listFiles();
            this.toMusic(files);
        }
    }
    
    /**
     * Load medias
     * @param files
     */
    public void toMusic(File[] files){
        mediaFiles = new ArrayList<>();
        for (File file : files) {
            try{
                this.loadFiles(file);
            }catch(OutOfMemoryError e){
                break;
            }
        }
        ObservableList<String> obl = FXCollections.observableArrayList("");
        ObservableList<String> albumObl = FXCollections.observableArrayList("");
        for(File f : mediaFiles){ 
            obl.add(f.getName().replaceAll(f.getName().substring(this.findPeriod(f.getName()), f.getName().length()), ""));
            File p = new File(f.getParent());
            if(!albumObl.contains(p.getName())){
                albumObl.add(p.getName());
            }
        }
        obl.remove(0);
        albumObl.remove(0);

        albumList.setItems(albumObl);
        musicList.setItems(obl);
    }
    
    /**
     * Load files from subdirectory folders
     * @param file
     */
    public void loadFiles(File file){
        if (file.isFile() && 
                (audioFormats.contains(file.getName().substring(this.findPeriod(file.getName())+1, file.getName().length())) || 
                videoFormats.contains(file.getName().substring(this.findPeriod(file.getName())+1, file.getName().length()))) ) {
            try{
                mediaFiles.add(file);
            }catch(MediaException e){
            }
        }
        else if(file.isDirectory()){
            File[] files = file.listFiles(); 
            for (File subfile : files) {
                this.loadFiles(subfile);
            }
        }
    }
    /**
     * Find the format of the media file
     * @param songName
     * @return the format
     */
    public int findPeriod(String songName){
        int i = 0;
        for(i=songName.length()-1; i>0; i--){
            if(songName.charAt(i) == '.'){
                break;
            }
        }
        return i;
    }
    
    /**
     * Play music
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void playMedia() throws InterruptedException{

        if(mediaFiles == null){
            songNameDet.setText("No songs available!");
        }
        else{
            if(media != null){
                stopMedia();
            }
            
            threadPlayMedia.stop();
            threadPlayMedia = new Thread(() -> {
                try{
                    media = new Media(mediaFiles.get(current).toURI().toString());
                    mediaPlayer = new MediaPlayer(media); 
                    this.changeVolume();
                    mediaPlayer.play();
                    displayVideo();
                }catch(MediaException e){
                    songNameDet.setText("Meadia not currently supported!");
                    songArtistDet.setText("");
                    songAlbumDet.setText("");
                    songDurationDet.setText("");
                }catch (IOException ex) {
                    Logger.getLogger(FXMLPlayerController.class.getName()).log(Level.SEVERE, null, ex);
                }

                musicList.getSelectionModel().select(current);
                mediaBar.setProgress(0);
            });
            threadPlayMedia.start();
             
            if(handleTime==false){
                handleTimeBar(musicList);
            }
        }
    }
    
    /**
     * Play/Pause the media when space is clicked.  
     */
    @FXML
    public void onSpaceEntered(){
        Scanner sc = new Scanner(System.in);
        String key = sc.nextLine();
        if(key.equals("\\s")){
            this.pauseMedia();
        }
    }
    
    /**
     * Handle time bar  
     * @param lis
     * @throws java.lang.InterruptedException
     */
    public void handleTimeBar(ListView<String> lis) throws InterruptedException{
        threadHandleTimeBar.stop();
        threadHandleTimeBar = new Thread(() -> {     
        try{
            while(mediaPlayer.getCurrentTime().toMillis() < mediaPlayer.getMedia().getDuration().toMillis()){
                mediaBar.setProgress(mediaPlayer.getCurrentTime().toMillis() / media.getDuration().toMillis());
                timer.setValue(mediaPlayer.getCurrentTime().toMillis() / media.getDuration().toMillis()*100);
                currentSongTime.setText((int)media.getDuration().toMinutes()+":"+ (int)(media.getDuration().toSeconds()%60));
                timeProgressBar.setText((int)mediaPlayer.getCurrentTime().toMinutes()+":"+ (int)(mediaPlayer.getCurrentTime().toSeconds()%60));
                
                songImageDet.setImage((Image) media.getMetadata().get("image"));
                songNameDet.setText("Title: "+((media.getMetadata().get("title")!=null) ? media.getMetadata().get("title") : lis.getSelectionModel().getSelectedItem()));
                songArtistDet.setText("Artist: "+media.getMetadata().get("artist"));
                songAlbumDet.setText("Album: "+media.getMetadata().get("album"));
                songDurationDet.setText("Duration: "+(int)media.getDuration().toMinutes()+"m: "+ (int)(media.getDuration().toSeconds()%60)+"s || "+media.getMetadata().get("encoding_info"));
                handleTime=true;
                try {
                    Thread.sleep((long)1000);
                    }
                catch (InterruptedException ex) {
                        Logger.getLogger(FXMLPlayerController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                
                }
                mediaBar.setProgress(0);
                timer.setValue(0);
                timeProgressBar.setText("--:--");
                currentSongTime.setText("--:--");
                
                handleTime=false;

                if(repeat){
                    this.playMedia();
                }
                if(shuffle){
                    this.nextMedia();
                }
                if(!repeat || !shuffle){
                    this.nextMedia();
                } 

            }catch(NullPointerException | InterruptedException ex) {
                    
            }
        });
        threadHandleTimeBar.start();
    }
    
     /**
     * Pause music
     */
    @FXML
    public void pauseMedia(){
        if(mediaPlayer != null){
            if(isPlaying == true){
                mediaPlayer.pause();
                isPlaying = false;
            }
            else{
                mediaPlayer.play();
                isPlaying = true;
            }
        }
    }
    
    /**
     * Stop music 
     */
    @FXML
    public void stopMedia(){
        if(mediaPlayer != null){
            mediaPlayer.stop();
        }
    }
    
    /**
     * Next song
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void nextMedia() throws InterruptedException{
        if(repeat == false){
            if(mediaFiles != null){
                if(shuffle){
                    Random rand = new Random();
                    current = rand.nextInt(mediaFiles.size());
                }else{current++;}
                
                if(current > mediaFiles.size()-1){
                    current=0;
                }
                this.stopMedia();
                this.playMedia();
            }
        }
    }
    
    /**
     * Previous song
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void prevMedia() throws InterruptedException{
        if(repeat == false){
            if(mediaFiles != null){
                if(shuffle){
                    Random rand = new Random();
                    current = rand.nextInt(mediaFiles.size());
                }
                current--;
                if(current < 0){
                    current=mediaFiles.size()-1;
                }
                this.stopMedia();
                this.playMedia();
            }
        }
    }
    
    /**
     * Set volume to mute on or off;
     * @throws java.io.IOException
     */
    public void muteOnOff() throws IOException{
        String shuffleString;
        if (media != null){
            if(mediaPlayer.isMute()){
                mediaPlayer.setMute(false);
                volumePic.setImage(new Image("/icons/icons8_medium_volume_50px.png"));
                shuffleString = "0";
            }
            else{
                mediaPlayer.setMute(true);
                volumePic.setImage(new Image("/icons/icons8_mute_50px.png"));
                shuffleString = "1";
            }
            this.saveToConfig("mute: "+shuffleString+";");
        }
    }
    
    /**
     * Turn on/off repeat
     * @throws java.io.IOException
     */
    @FXML
    public void clickRepeatButton() throws IOException{
        String shuffleString;
        if(repeat){
            repeat = false;
            repeatPane.setStyle("-fx-border-color: #3D4E64;");
            shuffleString = "0";
        }
        else{
            repeat = true;
            repeatPane.setStyle("-fx-border-color: #C657F5;");
            shuffleString = "1";
        }
        this.saveToConfig("repeat: "+shuffleString+";");
    }
    
    /**
     * Turn on/off shuffle
     * @throws java.io.IOException
     */
    @FXML
    public void clickShuffleButton() throws IOException{
        String shuffleString;
        if(shuffle){
            shuffle = false;
            shufflePane.setStyle("-fx-border-color: #3D4E64;");
            shuffleString = "0";
        }
        else{
            shuffle = true;
            shufflePane.setStyle("-fx-border-color: #C657F5;");
            shuffleString = "1";
        }
        this.saveToConfig("shuffle: "+shuffleString+";");
    }
    
    /**
     * Play selected media from the music list.
     * @throws java.lang.InterruptedException
     * @throws java.io.FileNotFoundException
     */
    @FXML
    public void playSelected() throws InterruptedException, FileNotFoundException, IOException{
        if(mediaFiles == null){
            this.loadMusic();
        }
        else{
            current = musicList.getSelectionModel().getSelectedIndex();
            this.playMedia();
        }
    }
    
    /**
     * Load selected album into the playlist.
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void loadAlbumIntoPlaylist() throws InterruptedException{
        if(mediaFiles != null){
            String albumName = albumList.getSelectionModel().getSelectedItem();
            newPlayList = new ArrayList<>();
            mediaFiles.forEach((fil) ->{
                File parent = new File(fil.getParent());
                if(parent.getName().equals(albumName)){
                    newPlayList.add(fil);
                }
            });
            ObservableList<String> playListObl = FXCollections.observableArrayList("");
            for(File f : newPlayList){ 
                playListObl.add(f.getName().replaceAll(f.getName().substring(this.findPeriod(f.getName()), f.getName().length()), ""));
            }
            playListObl.remove(0);
            playList.setItems(playListObl);  
        }
    }
    
    
    /**
     * Find the index of media in medial files.
     * @param name
     * @return int index
     */
    public int findCurrent(String name){
        int indx;
        for(indx = 0; indx < mediaFiles.size(); indx++){
            if(mediaFiles.get(indx).getName().contains(name)){
                return indx;
            }
        }
        return indx;
    }
    
    /**
     * Play selected media from the playlist.
     * @throws java.lang.InterruptedException
     */
    @FXML
    public void playSelectedOnPlaylist() throws InterruptedException{
        try{
            if(newPlayList != null){
                String songCurrent = playList.getSelectionModel().getSelectedItem();

                if(media != null){
                    stopMedia();
                }

                current = this.findCurrent(songCurrent);
                this.playMedia();

                mediaBar.setProgress(0);

            }
        }catch(NullPointerException e){}
    }
    
    
    /**
     * Change volume
     * @throws java.io.IOException
     */
    @FXML
    public void changeVolume() throws IOException{
        Double volume = volumeSlider.getValue()/100;
        this.saveToConfig("volume: "+Double.toString(volume)+";");
        if(media != null){ 
            mediaPlayer.setVolume(volume);
            
        } 
    }
    
    /**
     * Jump timer to prefed time. 
     */
    @FXML
    public void changeCurrentMediaTime(){
        if(media!=null){
            mediaPlayer.seek(new Duration(media.getDuration().toMillis() * (timer.getValue()/100)));
        }
    }
    
    /**
     * Search music. 
     */
    @FXML
    public void searchMusic(){
        if(mediaFiles!=null && searcher.getText().length() > 0){
            String songNam = searcher.getText();
            ObservableList<String> obl = FXCollections.observableArrayList("");
            newPlayList = new ArrayList<>();
            for(File f : mediaFiles){ 
                if(f.getName().toLowerCase().contains(songNam.toLowerCase())){
                    obl.add(f.getName().replaceAll(f.getName().substring(this.findPeriod(f.getName()), f.getName().length()), ""));
                    newPlayList.add(f);
                }
            }
            obl.remove(0);
            playList.setItems(obl);
            if(songNam.trim().length()<1){
                playList.setItems(FXCollections.observableArrayList(""));
            }
        }
        
    }
    
    /**
     * Handle configurations file.
     * @param str
     * @throws java.io.FileNotFoundException
     */
    public void saveToConfig(String str) throws FileNotFoundException, IOException{
        
        Scanner sc = new Scanner(new File(configFile));
        String configString = "";
        while(sc.hasNextLine()){
            String line = sc.nextLine();
            if(line.contains(str.split(":")[0])){
                configString += str+"\n";
            }else{
                configString += line+"\n";
            }
        }
        try (PrintWriter wr = new PrintWriter(new FileWriter(configFile))) {
            wr.write(configString);
        }
    }
    
    /**
     * Switch between audio play list and video pane.
     */
    @FXML
    public void switchAudioVideo() throws IOException{
        if(MusicAlbumPlaylistPane.isVisible()){
            MusicAlbumPlaylistPane.setVisible(false);
            videoPane.setVisible(true);
            audioVideoSwitch.setImage(new Image("/icons/icons8_playlist_50px.png"));
        }
        else{
            videoPane.setVisible(false);
            MusicAlbumPlaylistPane.setVisible(true);
            audioVideoSwitch.setImage(new Image("/icons/icons8_movie_50px.png"));
        }
    }
    
    /**
     * display video view.
     * @param player
     */
    public void displayVideo(){
        Thread videoThread = new Thread();
        videoThread.stop();
        videoThread = new Thread(() -> {

        mediaView.setMediaPlayer(null);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setFitWidth(912);
        mediaView.setFitHeight(477);
        mediaView.setSmooth(true);
        mediaView.setLayoutX(0);
        mediaView.setLayoutY(0);
            
        });
        videoThread.setName("VideoThread");
        videoThread.start();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        videoPane.setVisible(false);
        try {
            loadMusic();
        } catch (IOException ex) {
            Logger.getLogger(FXMLPlayerController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(FXMLPlayerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
