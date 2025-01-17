package com.sedmelluq.discord.lavaplayer.source.yamusic;

import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;
import java.util.stream.Collectors;

public class YandexMusicUtils {
  private static final String TRACK_URL_FORMAT = "https://music.yandex.ru/album/%s/track/%s";

  public static AudioTrack extractTrack(JsonBrowser trackInfo, Function<AudioTrackInfo, AudioTrack> trackFactory) {
    if (!trackInfo.get("track").isNull()) {
      trackInfo = trackInfo.get("track");
    }
    String artists = trackInfo.get("artists").values().stream()
        .map(e -> e.get("name").text())
        .collect(Collectors.joining(", "));

    String trackId = trackInfo.get("id").text();

    JsonBrowser album = trackInfo.get("albums").index(0);

    String albumId = album.get("id").text();

    return trackFactory.apply(new AudioTrackInfo(
        trackInfo.get("title").text(),
        artists,
        trackInfo.get("durationMs").as(Long.class),
        trackInfo.get("id").text(),
        false,
        String.format(TRACK_URL_FORMAT, albumId, trackId),
        getArtwork(trackInfo)
    ));
  }
  public static String getArtwork(JsonBrowser data) {
    String artwork = null;
    JsonBrowser cover = data.get("coverUri");
    if (!cover.isNull()) {
      artwork = "https://" + cover.text().replace("%%", "1000x1000");
    }
    if (artwork == null) {
      JsonBrowser ogImage = data.get("ogImage");
      if (!ogImage.isNull()) {
        artwork = "https://" + ogImage.text().replace("%%", "1000x1000");
      }
    }

    if (artwork == null) {
      cover = data.get("coverUri");
      if (!cover.isNull()) {
        artwork = "https://" + cover.text().replace("%%", "1000x1000");
      }
    }

    if (artwork == null) {
      cover = data.get("cover");
      if (!cover.isNull()) {
        artwork = "https://" + cover.get("uri").text().replace("%%", "1000x1000");
      }
    }
    return artwork;
  }
}
