package com.sedmelluq.discord.lavaplayer.source.smule;

import com.sedmelluq.discord.lavaplayer.container.mpeg.MpegAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Audio track that handles processing smule tracks.
 */
public class SmuleAudioTrack extends DelegatedAudioTrack {
    private static final Logger log = LoggerFactory.getLogger(SmuleAudioTrack.class);

    private final SmuleAudioSourceManager sourceManager;

    /**
     * @param trackInfo Track info
     * @param sourceManager Source manager which was used to find this track
     */
    public SmuleAudioTrack(AudioTrackInfo trackInfo, SmuleAudioSourceManager sourceManager) {
        super(trackInfo);

        this.sourceManager = sourceManager;
    }

    @Override
    public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
        try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
            log.debug("Loading Smule track page from URL: {}", trackInfo.identifier);

            String trackMediaUrl = getTrackMediaUrl(httpInterface);
            log.debug("Starting Smule track from URL: {}", trackMediaUrl);

            try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackMediaUrl), null)) {
                processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
            }
        }
    }

    private String getTrackMediaUrl(HttpInterface httpInterface) throws IOException {
        try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(trackInfo.identifier))) {
            String html = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            Document document = Jsoup.parse(html);

            String url = document.selectFirst("meta[name=twitter:player:stream]").attr("content");
            return url;
        }
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new SmuleAudioTrack(trackInfo, sourceManager);
    }

    @Override
    public AudioSourceManager getSourceManager() {
        return sourceManager;
    }
}