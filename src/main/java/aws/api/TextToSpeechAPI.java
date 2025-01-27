// Author: OrkhanGG
// Created: 10/10/2022
// Purpose: Creating a bridge between our app and Amazon Polly
// Additionally: Amazon Polly is an AWS service that provides users high quality TTS(Text to Speech) service.
//               Although it's already an API, I had to implement my own API to customize it in our directions.
// API Functionality: API is able to play the text with given voice
//                    or download the current stream in a given output format(MP3 for example).
//                    Additionally, API includes its custom event listener to catch play/stop events.

//------------------------------------------------- Usage ------------------------------------------------------
// 1. Create a reference to TextToSpeechAPI
// 2. Call public void Initialize()   <- This will initialize the fields with appropriate default values
// 3. Call setVoice, setOutputFormat (This is optional as they already have default values came with Initialize() )
// 4. Call RequestSetStream(String stream) (This is required as Amazon Polly will convert this text/stream to Speech.)
// 5. Call RequestPlayStream() or RequestDownloadCurrentStream(String path)
//         RequestPlayStream() will only make Amazon Polly talk
//         RequestDownloadCurrentStream(String path) will download the stream/audio file in the specified path.

package aws.api;

import aws.credentials.AWSCredentialsManager;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.*;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import utils.Callback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
public final class TextToSpeechAPI {
    private AdvancedPlayer advancedPlayer = null;
    Map<String,Callback> _callback_ = null;
    private AmazonPolly amazonPolly = null;
    private Voice currentVoice = null;
    private InputStream currentInputStream = null;
    private OutputFormat currentOutputFormat = null;

    boolean isPlaying = false;

    // Functions-----------------------------------
    public void Initialize() {
        amazonPolly = AmazonPollyClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(AWSCredentialsManager.getInstance())).withRegion(AWSCredentialsManager.getInstance().getRegion()).build();

        _callback_ = new HashMap<>();

        setDefaults();
    }

    //------------------------------------------------
    // EVENTS
    public void addOnStartListener(Callback callback){
        _callback_.put("start",callback);
    }
    public void addOnStopListener(Callback callback){
        _callback_.put("stop",callback);
    }
    private void onStart(){
        for(var i : _callback_.entrySet())
            if(i.getKey().equals("start"))
                i.getValue().call();
        setPlaying(true);
    }
    private void onStop(){
        for(var i : _callback_.entrySet())
            if(i.getKey().equals("stop"))
                i.getValue().call();
        setPlaying(false);
    }
    //------------------------------------------------
    private InputStream synthesize(String text, OutputFormat format) throws IOException {
        SynthesizeSpeechRequest synthReq = new SynthesizeSpeechRequest().withText(text).withVoiceId(currentVoice.getId()).withOutputFormat(format).withSampleRate("8000");
        SynthesizeSpeechResult synthRes = amazonPolly.synthesizeSpeech(synthReq);

        return synthRes.getAudioStream();
    }

    // Getters---------------------------------------
    public OutputFormat getOutputFormat() {
        return currentOutputFormat;
    }
    public InputStream getCurrentInputStream() {
        return currentInputStream;
    }

    public Voice getVoice() {
        return currentVoice;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    // Setters---------------------------------------
    private void setDefaults() {
        setVoice(10);// TODO: Change default Voice index
        setOutputFormat(OutputFormat.Mp3);// default Output format is Mp3
    }
    public void setVoice(int voiceID) {
        DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
        DescribeVoicesResult describeVoicesResult = amazonPolly.describeVoices(describeVoicesRequest);
        currentVoice = describeVoicesResult.getVoices().get(voiceID);// TODO: Change default Voice index
    }
    public void setOutputFormat(OutputFormat outputFormat) {
        currentOutputFormat = outputFormat;
    }

    // Requests--------------------------------------------
    public void RequestSetStream(String stream) {
        try {
            currentInputStream = synthesize(stream, getOutputFormat());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void RequestToStopStream(){
        if(advancedPlayer != null) {
            advancedPlayer.stop();
            onStop(); // Let system know about this event
            return;
        }
    }

    public void RequestPlayStream() {
        //create an MP3 player
        try {
            advancedPlayer = new AdvancedPlayer(currentInputStream, javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
        } catch (JavaLayerException ex) {
            throw new RuntimeException(ex);
        }

        advancedPlayer.setPlayBackListener(new PlaybackListener() {
            @Override
            public void playbackStarted(PlaybackEvent evt) {
                System.out.println("Playback started");// TODO: REMOVE SOUT
                onStart();// Let system know about this event
            }

            @Override
            public void playbackFinished(PlaybackEvent evt) {
                System.out.println("Playback finished");// TODO: REMOVE SOUT
                onStop();// Let system know about this event
            }
        });

        // play it!
        try {
            advancedPlayer.play();
        } catch (JavaLayerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void RequestDownloadCurrentStream(String path) {
        File file = new File(path);
        try {
            Files.copy(getCurrentInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Event Listener-------------------------------
}

