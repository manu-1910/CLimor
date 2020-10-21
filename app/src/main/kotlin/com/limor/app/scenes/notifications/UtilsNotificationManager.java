package com.limor.app.scenes.notifications;


import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.limor.app.App;
import com.limor.app.R;
import com.limor.app.uimodels.UIAdItem;
import com.limor.app.uimodels.UINotificationItem;
import com.limor.app.uimodels.UIPodcast;

import de.hdodenhof.circleimageview.CircleImageView;


public class UtilsNotificationManager {

    public static final String NOTIFICATION_TYPE_GENERAL = "general";
    public static final String NOTIFICATION_TYPE_FOLLOW = "follow";
    public static final String NOTIFICATION_TYPE_MENTION = "mention";
    public static final String NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE = "podcast_bookmark_share";
    public static final String NOTIFICATION_TYPE_PODCAST_LIKE = "podcast_like";
    public static final String NOTIFICATION_TYPE_PODCAST_RECAST = "podcast_recast";
    public static final String NOTIFICATION_TYPE_PODCAST_COMMENT = "podcast_comment";
    public static final String NOTIFICATION_TYPE_COMMENT_LIKE = "comment_like";
    public static final String NOTIFICATION_TYPE_AD_COMMENT = "ad_comment";
    public static final String NOTIFICATION_TYPE_CONVERSATION_REQUEST = "conversation_request";
    public static final String NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT = "conversation_participant";
    public static final String NOTIFICATION_TYPE_MESSAGE_SENT = "message_sent";

    public static void handleNotificationList(UINotificationItem notification, CircleImageView ivAvatar, ImageView ivNotification, TextView tvMessage, TextView tvTime, RelativeLayout rlAllArea) {
        ivNotification.setBackground(null);
        ivNotification.setImageDrawable(null);
        tvMessage.setText(notification.getMessage());
        //tvTime.setText(Commons.getPrettyTimeStamp((notification.getCreatedAt()));
        tvTime.setText(notification.getCreatedAt());
        ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivNotification.setBackground(App.instance.getResources().getDrawable(R.drawable.podcast));
        if (notification.getResources().getOwner() != null) {
            Glide.with(App.instance).load(notification.getResources().getOwner().getImages().getSmall_url()).into(ivAvatar);
        }
        switch (notification.getNotificationType()) {
            case NOTIFICATION_TYPE_GENERAL:
                //SHOImageUtils.getImage(App.instance, null, notification.getResources().getGeneralNotification().getPlatform().getImages().getSmall_url(), ivAvatar);
                Glide.with(App.instance).load(notification.getResources().getImages().getSmall_url()).into(ivAvatar);
                break;
            case NOTIFICATION_TYPE_FOLLOW:
                ivNotification.setScaleType(ImageView.ScaleType.CENTER);
                ivNotification.setBackground(App.instance.getResources().getDrawable(R.color.white));
                if (notification.getResources().getOwner().getFollowed()) {
                    ivNotification.setImageDrawable(App.instance.getResources().getDrawable(R.drawable.followed));
                } else {
                    ivNotification.setImageDrawable(App.instance.getResources().getDrawable(R.drawable.add));
                }
                break;
            case NOTIFICATION_TYPE_MENTION:
            case NOTIFICATION_TYPE_PODCAST_BOOKMARK_SHARE:
            case NOTIFICATION_TYPE_PODCAST_LIKE:
            case NOTIFICATION_TYPE_COMMENT_LIKE:
            case NOTIFICATION_TYPE_PODCAST_RECAST:
            case NOTIFICATION_TYPE_PODCAST_COMMENT:
                ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivNotification.setBackground(App.instance.getResources().getDrawable(R.drawable.podcast));
                if (notification.getResources().getPodcast() != null) {
                    //SHOImageUtils.getImage(App.instance, null, notification.getResources().getPodcast().getImages().getSmall_url(), ivNotification);
                    Glide.with(App.instance).load(notification.getResources().getPodcast().getImages().getSmall_url()).into(ivNotification);
                } else if (notification.getResources().getAd() != null) {
                    //SHOImageUtils.getImage(App.instance, null, notification.getResources().getAd().getGallery().get(0).getImages().getSmall_url(), ivNotification);
                    Glide.with(App.instance).load(notification.getResources().getAd().getGallery().get(0).getImages().getSmall_url()).into(ivNotification);
                }
                break;
            case NOTIFICATION_TYPE_CONVERSATION_REQUEST:
                ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivNotification.setBackground(null);
                ivNotification.setImageDrawable(App.instance.getResources().getDrawable(R.drawable.message_notif_36));
                //SHOImageUtils.getImage(App.instance, null, notification.getResources().getOwner().getImages().getSmall_url(), ivAvatar);
                Glide.with(App.instance).load(notification.getResources().getOwner().getImages().getSmall_url()).into(ivAvatar);
                break;
            case NOTIFICATION_TYPE_CONVERSATION_PARTICIPANT:
                ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivNotification.setBackground(null);
                ivNotification.setImageDrawable(App.instance.getResources().getDrawable(R.drawable.message_notif_36));
                //SHOImageUtils.getImage(App.instance, null, notification.getResources().getImages().getSmall_url(), ivAvatar);
                Glide.with(App.instance).load(notification.getResources().getImages().getSmall_url()).into(ivAvatar);
                break;
            case NOTIFICATION_TYPE_MESSAGE_SENT:
                ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivNotification.setBackground(null);
                ivNotification.setImageDrawable(App.instance.getResources().getDrawable(R.drawable.message_notif_36));
                //SHOImageUtils.getImage(App.instance, null, notification.getResources().getMessage().getOwner().getInstance().getImages().getSmall_url(), ivAvatar);
                Glide.with(App.instance).load(notification.getResources().getOwner().getImages().getSmall_url()).into(ivAvatar);
                break;
            default:
                UIPodcast podcast = notification.getResources().getPodcast();
                UIAdItem ad = notification.getResources().getAd();
                if (podcast != null) {
                    //SHOImageUtils.getImage(App.instance, null, podcast.getImages().getSmall_url(), ivNotification);
                    Glide.with(App.instance).load(podcast.getImages().getSmall_url()).into(ivNotification);
                } else if (ad != null && ad.getGallery().size() > 0) {
                    //SHOImageUtils.getImage(App.instance, null, ad.getGallery().get(0).getImages().getSmall_url(), ivNotification);
                    Glide.with(App.instance).load(ad.getGallery().get(0).getImages().getSmall_url()).into(ivNotification);
                }
        }
    }

//    public static void playBookmark(Context context, UIPodcast podcast, Bookmark bookmark) {
//        Call<SHOBaseResponse<PodcastData>> call = SHOApiBuilder.getApiBuilder(true).getPodcast(podcast.getId());
//        call.enqueue(new SHOCallback<PodcastData>(context, false, true) {
//            @Override
//            public void onSuccess(Response<SHOBaseResponse<PodcastData>> response) {
//                App.instance.sendBroadcast(new Intent(Constants.BROADCAST_PLAY_PODCAST)
//                        .putExtra(Constants.INTENT_PODCAST, LimorUtils.createPodcastItem(response.body().data.getPodcast()))
//                        .putExtra(Constants.INTENT_BOOKMARK, bookmark));
//            }
//            @Override
//            public void onError(SHOBaseResponse errorResponse) {}
//        });
//    }
}
