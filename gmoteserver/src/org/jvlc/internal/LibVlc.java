/*****************************************************************************
 * LibVlc.java: VLC Java Bindings JNA Glue
 *****************************************************************************
 * Copyright (C) 1998-2008 the VideoLAN team
 *
 * Authors: Filippo Carone <filippo@carone.org>
 *
 *
 * $Id $
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111, USA.
 *****************************************************************************/

package org.videolan.jvlc.internal;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;


public interface LibVlc extends Library
{
    LibVlc INSTANCE = (LibVlc) Native.loadLibrary(Platform.isWindows()? "libvlc" : "vlc", LibVlc.class);

    LibVlc SYNC_INSTANCE = (LibVlc) Native.synchronizedLibrary(INSTANCE);
    
    public static class libvlc_exception_t extends Structure
    {

        public int raised;

        public int code;

        public String message;
    }

    public static class libvlc_log_message_t extends Structure
    {

        public int sizeof_msg; /* sizeof() of message structure, must be filled in by user */

        public int i_severity; /* 0=INFO, 1=ERR, 2=WARN, 3=DBG */

        public String psz_type; /* module type */

        public String psz_name; /* module name */

        public String psz_header; /* optional header */

        public String psz_message; /* message */
    }

    public static class libvlc_event_t extends Structure
    {

        public int type;

        public Pointer obj;

        public event_type_specific event_type_specific;

    }

    public class media_meta_changed extends Structure
    {

        public Pointer meta_type;
    }

    public class media_subitem_added extends Structure
    {

        public LibVlcMediaDescriptor new_child;
    }

    public class media_duration_changed extends Structure
    {

        public NativeLong new_duration;
    }

    public class media_preparsed_changed extends Structure
    {

        public int new_status;
    }

    public class media_freed extends Structure
    {

        public LibVlcMediaDescriptor md;
    }

    public class media_state_changed extends Structure
    {

        // @todo: check this one
        public int new_state;
    }

    /* media instance */

    public class media_player_position_changed extends Structure
    {

        public float new_position;
    }

    public class media_player_time_changed extends Structure
    {

        // @todo: check this one
        public long new_time;
    }

    /* media list */
    public class media_list_item_added extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_will_add_item extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_item_deleted extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_will_delete_item extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    /* media list view */
    public class media_list_view_item_added extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_view_will_add_item extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_view_item_deleted extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    public class media_list_view_will_delete_item extends Structure
    {

        public LibVlcMediaDescriptor item;

        public int index;
    }

    /* media discoverer */
    public class media_media_discoverer_started extends Structure
    {

        public Pointer unused;
    }

    public class media_media_discoverer_ended extends Structure
    {

        public Pointer unused;
    }
    
    public enum libvlc_meta_t {
      
      libvlc_meta_Title(0), 
      libvlc_meta_Artist  (1), 
      libvlc_meta_Genre  (2),
      libvlc_meta_Copyright(3),  
      libvlc_meta_Album(4),
      libvlc_meta_TrackNumber(5),   
      libvlc_meta_Description(6),  
      libvlc_meta_Rating(7),
      libvlc_meta_Date(8),
      libvlc_meta_Setting(9),   
      libvlc_meta_URL(10),
      libvlc_meta_Language(11),  
      libvlc_meta_NowPlaying(12),  
      libvlc_meta_Publisher(13), 
      libvlc_meta_EncodedBy (14),
      libvlc_meta_ArtworkURL(15),
      libvlc_meta_TrackID(16);
      
      int code;
      libvlc_meta_t(int code) {
        this.code = code;
      }
      
      public int getCode() {
        return code;
      }
    }

    public class event_type_specific extends Union
    {

        public media_meta_changed media_meta_changed;

        public media_subitem_added media_subitem_added;

        public media_duration_changed media_duration_changed;

        public media_preparsed_changed media_preparsed_changed;

        public media_freed media_freed;

        public media_state_changed media_state_changed;

        public media_player_position_changed media_player_position_changed;

        public media_player_time_changed media_player_time_changed;

        public media_list_item_added media_list_item_added;

        public media_list_will_add_item media_list_will_add_item;

        public media_list_item_deleted media_list_item_deleted;

        public media_list_will_delete_item media_list_will_delete_item;

        public media_list_view_item_added media_list_view_item_added;

        public media_list_view_will_add_item media_list_view_will_add_item;

        public media_list_view_item_deleted media_list_view_item_deleted;

        public media_list_view_will_delete_item media_list_view_will_delete_item;
    }

    public class LibVlcInstance extends PointerType
    {
    }

    public class LibVlcMediaDescriptor extends PointerType
    {
    }

    public class LibVlcMediaInstance extends PointerType
    {
    }

    public class LibVlcMediaList extends PointerType
    {
    }

    public class LibVlcMediaListPlayer extends PointerType
    {
    }

    public class LibVlcEventManager extends PointerType
    {
    }

    public class LibVlcLog extends PointerType
    {
    }

    public class LibVlcLogIterator extends PointerType
    {
    }

    // exception handling
    void libvlc_exception_init(libvlc_exception_t exception);

    int libvlc_exception_raised(final libvlc_exception_t exception);

    void libvlc_exception_raise(libvlc_exception_t exception, String format, Object... args);

    void libvlc_exception_clear(libvlc_exception_t exception);

    String libvlc_exception_get_message(libvlc_exception_t exception);

    // core
    LibVlcInstance libvlc_new(int argc, String[] argv, libvlc_exception_t exception);

    void libvlc_release(LibVlcInstance libvlc_instance_t);
    
    String libvlc_get_version();
    
    String libvlc_get_compiler();
    
    String libvlc_get_changeset();
    
    void libvlc_add_intf(LibVlcInstance libvlc_instance, String name, libvlc_exception_t exception);

    // video

    void libvlc_video_set_parent(LibVlcInstance libvlc_instance, long drawable, libvlc_exception_t exception);

    void libvlc_toggle_fullscreen(LibVlcMediaInstance libvlc_instance);

    void libvlc_set_fullscreen(LibVlcMediaInstance instance, int fullscreen, libvlc_exception_t exception);

    int libvlc_get_fullscreen(LibVlcMediaInstance instance, libvlc_exception_t exception);

    int libvlc_video_get_height(LibVlcMediaInstance instance, libvlc_exception_t exception);

    int libvlc_video_get_width(LibVlcMediaInstance instance, libvlc_exception_t exception);

    String libvlc_video_get_aspect_ration(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_video_set_aspect_ration(LibVlcMediaInstance instance, String ratio, libvlc_exception_t exception);

    int libvlc_video_get_spu(LibVlcMediaInstance instance, libvlc_exception_t exception);

    int libvlc_video_set_spu(LibVlcMediaInstance instance, int spu, libvlc_exception_t exception);

    String libvlc_video_get_crop_geometry(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_video_set_crop_geometry(LibVlcMediaInstance instance, String geometry, libvlc_exception_t exception);

    void libvlc_video_take_snapshot(LibVlcMediaInstance instance, String filename, int width, int height,
        libvlc_exception_t exception);

    void libvlc_video_destroy(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_video_resize(LibVlcMediaInstance instance, int width, int height, libvlc_exception_t exception);

    void libvlc_video_reparent(LibVlcMediaInstance instance, long drawable, libvlc_exception_t exception);

    void libvlc_video_set_size(LibVlcInstance instance, int width, int height, libvlc_exception_t exception);

    // audio

    void libvlc_audio_toggle_mute(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_audio_set_mute(LibVlcInstance instance, int mute, libvlc_exception_t exception);

    int libvlc_audio_get_mute(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_audio_get_volume(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_audio_set_volume(LibVlcInstance instance, int volume, libvlc_exception_t exception);

    int libvlc_audio_get_track_count(LibVlcMediaInstance mediaInstance, libvlc_exception_t exception);

    int libvlc_audio_get_track(LibVlcMediaInstance mediaInstance, libvlc_exception_t exception);

    void libvlc_audio_set_track(LibVlcMediaInstance mediaInstance, int channel, libvlc_exception_t exception);

    int libvlc_audio_get_channel(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_audio_set_channel(LibVlcInstance instance, int channel, libvlc_exception_t exception);

    // playlist

    void libvlc_playlist_loop(LibVlcInstance instance, int loop, libvlc_exception_t exception);

    void libvlc_playlist_play(LibVlcInstance instance, int itemIndex, int optionsCount, String[] options,
        libvlc_exception_t exception);

    void libvlc_playlist_pause(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_playlist_stop(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_playlist_isplaying(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_playlist_items_count(LibVlcInstance instance, libvlc_exception_t exception);
    
    int libvlc_playlist_get_current_index(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_playlist_next(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_playlist_prev(LibVlcInstance instance, libvlc_exception_t exception);

    void libvlc_playlist_clear(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_playlist_add(LibVlcInstance instance, String uri, String name, libvlc_exception_t exception);

    int libvlc_playlist_delete_item(LibVlcInstance instance, int itemIndex, libvlc_exception_t exception);

    LibVlcMediaInstance libvlc_playlist_get_media_player(LibVlcInstance instance, libvlc_exception_t exception);

    int libvlc_media_player_is_seekable(LibVlcMediaInstance instance, libvlc_exception_t exception);

    int libvlc_media_player_can_pause(LibVlcMediaInstance instance, libvlc_exception_t exception);

    // media descriptor

    LibVlcMediaDescriptor libvlc_media_new(LibVlcInstance libvlc_instance, String mrl, libvlc_exception_t exception);

    void libvlc_media_retain(LibVlcMediaDescriptor media);
    void libvlc_media_add_option(LibVlcMediaDescriptor media, String option, libvlc_exception_t exception);

    String libvlc_media_get_mrl(LibVlcMediaDescriptor media);

    void libvlc_media_release(LibVlcMediaDescriptor media);

    LibVlcEventManager libvlc_media_event_manager(LibVlcMediaDescriptor media, libvlc_exception_t exception);

    String libvlc_media_get_meta (LibVlcMediaDescriptor media, int libvlc_meta_code, libvlc_exception_t exception);
    
    // media player

    LibVlcMediaDescriptor libvlc_media_player_get_media(LibVlcMediaInstance libvlc_instance, libvlc_exception_t exception);
    
    LibVlcMediaInstance libvlc_media_player_new(LibVlcInstance instance, libvlc_exception_t exception);

    LibVlcMediaInstance libvlc_media_player_new_from_media(LibVlcMediaDescriptor media, libvlc_exception_t exception);

    void libvlc_media_player_play(LibVlcMediaInstance media_player, libvlc_exception_t exception);

    void libvlc_media_player_pause(LibVlcMediaInstance media_player, libvlc_exception_t exception);

    void libvlc_media_player_stop(LibVlcMediaInstance media_player, libvlc_exception_t exception);

    void libvlc_media_player_set_drawable(LibVlcMediaInstance libvlc_media_player, int drawable,
        libvlc_exception_t exception);

    long libvlc_media_player_get_length(LibVlcMediaInstance instance, libvlc_exception_t exception);

    long libvlc_media_player_get_time(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_media_player_set_time(LibVlcMediaInstance instance, long time, libvlc_exception_t exception);

    float libvlc_media_player_get_position(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_media_player_set_position(LibVlcMediaInstance instance, float position, libvlc_exception_t exception);

    int libvlc_media_player_will_play(LibVlcMediaInstance instance, libvlc_exception_t exception);

    void libvlc_media_player_set_rate(LibVlcMediaInstance instance, float rate, libvlc_exception_t exception);

    float libvlc_media_player_get_rate(LibVlcMediaInstance instance, libvlc_exception_t exception);

    int libvlc_media_player_has_vout(LibVlcMediaInstance instance2, libvlc_exception_t exception);

    float libvlc_media_player_get_fps(LibVlcMediaInstance instance2, libvlc_exception_t exception);

    void libvlc_media_player_release(LibVlcMediaInstance instance);

    LibVlcEventManager libvlc_media_player_event_manager(LibVlcMediaInstance media_player, libvlc_exception_t exception);

    // media list

    LibVlcMediaList libvlc_media_list_new(LibVlcInstance libvlc_instance, libvlc_exception_t exception);

    void libvlc_media_list_release(LibVlcMediaList libVlcMediaList);

    void libvlc_media_list_add_file_content(LibVlcMediaList libvlc_media_list, String fileName,
        libvlc_exception_t exception);

    void libvlc_media_list_set_media(LibVlcMediaList libvlc_media_list, LibVlcMediaDescriptor libvlc_media,
        libvlc_exception_t exception);

    LibVlcMediaDescriptor libvlc_media_list_media(LibVlcMediaList libvlc_media_list, libvlc_exception_t exception);

    void libvlc_media_list_add_media(LibVlcMediaList libvlc_media_list, LibVlcMediaDescriptor libvlc_media,
        libvlc_exception_t exception);

    void libvlc_media_list_insert_media(LibVlcMediaList libvlc_media_list, LibVlcMediaDescriptor libvlc_media,
        int position, libvlc_exception_t exception);

    void libvlc_media_list_remove_index(LibVlcMediaList libvlc_media_list, int position, libvlc_exception_t exception);

    int libvlc_media_list_count(LibVlcMediaList libvlc_media_list, libvlc_exception_t exception);

    LibVlcMediaDescriptor libvlc_media_list_item_at_index(LibVlcMediaList libvlc_media_list, int position,
        libvlc_exception_t exception);

    int libvlc_media_list_index_of_item(LibVlcMediaList libvlc_media_list, LibVlcMediaDescriptor libvlc_media,
        libvlc_exception_t exception);

    int libvlc_media_list_is_readonly(LibVlcMediaList libvlc_media_list);

    LibVlcEventManager libvlc_media_list_event_manager(LibVlcMediaList libvlc_media_list, libvlc_exception_t exception);

    // libvlc_media_list_player

    LibVlcMediaListPlayer libvlc_media_list_player_new(LibVlcInstance libvlc_media_player, libvlc_exception_t exception);

    void libvlc_media_list_player_release(LibVlcMediaListPlayer libvlc_media_list_player);

    void libvlc_media_list_player_set_media_player(LibVlcMediaListPlayer libvlc_media_list_player,
        LibVlcMediaInstance libvlc_media_player, libvlc_exception_t exception);

    void libvlc_media_list_player_set_media_list(LibVlcMediaListPlayer libvlc_media_list_player,
        LibVlcMediaList libvlc_media_list, libvlc_exception_t exception);

    void libvlc_media_list_player_play(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    void libvlc_media_list_player_pause(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    int libvlc_media_list_player_is_playing(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    int libvlc_media_list_player_get_state(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    void libvlc_media_list_player_play_item_at_index(LibVlcMediaListPlayer libvlc_media_list_player, int position,
        libvlc_exception_t exception);

    void libvlc_media_list_player_play_item(LibVlcMediaListPlayer libvlc_media_list_player,
        LibVlcMediaDescriptor libvlc_media, libvlc_exception_t exception);

    void libvlc_media_list_player_stop(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    void libvlc_media_list_player_next(LibVlcMediaListPlayer libvlc_media_list_player, libvlc_exception_t exception);

    // VLM

    void libvlc_vlm_add_broadcast(LibVlcInstance p_instance, String psz_name, String psz_input, String psz_output,
        int i_options, String[] ppsz_options, int b_enabled, int b_loop, libvlc_exception_t p_e);

    void libvlc_vlm_add_vod(LibVlcInstance p_instance, String psz_name, String psz_input, int i_options,
        String[] ppsz_options, int b_enabled, String psz_mux, libvlc_exception_t p_e);

    void libvlc_vlm_del_media(LibVlcInstance p_instance, String psz_name, libvlc_exception_t p_e);

    void libvlc_vlm_set_enabled(LibVlcInstance p_instance, String psz_name, int b_enabled, libvlc_exception_t p_e);

    void libvlc_vlm_set_output(LibVlcInstance p_instance, String psz_name, String psz_output, libvlc_exception_t p_e);

    void libvlc_vlm_set_input(LibVlcInstance p_instance, String psz_name, String psz_input, libvlc_exception_t p_e);

    void libvlc_vlm_add_input(LibVlcInstance p_instance, String psz_name, String psz_input, libvlc_exception_t p_e);

    void libvlc_vlm_set_loop(LibVlcInstance p_instance, String psz_name, int b_loop, libvlc_exception_t p_e);

    void libvlc_vlm_set_mux(LibVlcInstance p_instance, String psz_name, String psz_mux, libvlc_exception_t p_e);

    void libvlc_vlm_change_media(LibVlcInstance p_instance, String psz_name, String psz_input, String psz_output,
        int i_options, String[] ppsz_options, int b_enabled, int b_loop, libvlc_exception_t p_e);

    void libvlc_vlm_play_media(LibVlcInstance p_instance, String psz_name, libvlc_exception_t p_e);

    void libvlc_vlm_stop_media(LibVlcInstance p_instance, String psz_name, libvlc_exception_t p_e);

    void libvlc_vlm_pause_media(LibVlcInstance p_instance, String psz_name, libvlc_exception_t p_e);

    void libvlc_vlm_seek_media(LibVlcInstance p_instance, String psz_name, float f_percentage, libvlc_exception_t p_e);

    String libvlc_vlm_show_media(LibVlcInstance p_instance, String psz_name, libvlc_exception_t p_e);

    void libvlc_vlm_release(LibVlcInstance p_instance, libvlc_exception_t p_e);

    // event manager

    public static interface LibVlcCallback extends Callback
    {

        void callback(libvlc_event_t libvlc_event, Pointer userData);
    }

    void libvlc_event_attach(LibVlcEventManager event_manager, int event_type, LibVlcCallback callback,
        Pointer userData, libvlc_exception_t exception);

    void libvlc_event_detach(LibVlcEventManager event_manager, int event_type, LibVlcCallback callback,
        Pointer userData, libvlc_exception_t excecption);

    void libvlc_toggle_fullscreen(LibVlcMediaInstance instance, libvlc_exception_t exception);

    // logging

    int libvlc_get_log_verbosity(LibVlcInstance p_instance, libvlc_exception_t p_e);

    void libvlc_set_log_verbosity(LibVlcInstance p_instance, int level, libvlc_exception_t p_e);

    LibVlcLog libvlc_log_open(LibVlcInstance p_instance, libvlc_exception_t p_e);

    void libvlc_log_close(LibVlcLog p_log, libvlc_exception_t p_e);

    int libvlc_log_count(LibVlcLog p_log, libvlc_exception_t p_e);

    void libvlc_log_clear(LibVlcLog p_log, libvlc_exception_t p_e);

    LibVlcLogIterator libvlc_log_get_iterator(LibVlcLog p_log, libvlc_exception_t p_e);

    void libvlc_log_iterator_free(LibVlcLogIterator p_iter, libvlc_exception_t p_e);

    int libvlc_log_iterator_has_next(LibVlcLogIterator p_iter, libvlc_exception_t p_e);

    libvlc_log_message_t libvlc_log_iterator_next(LibVlcLogIterator p_iter, libvlc_log_message_t p_buffer,
        libvlc_exception_t p_e);

}
