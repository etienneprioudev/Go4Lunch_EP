package com.EtiennePriou.go4launch.ui.chat;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.EtiennePriou.go4launch.R;
import com.EtiennePriou.go4launch.models.Message;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyChatRecyclerViewAdapter extends FirestoreRecyclerAdapter<Message, MyChatRecyclerViewAdapter.MessageViewHolder> {

    public interface Listener {
        void onDataChanged();
    }

    //FOR DATA
    private final RequestManager glide;
    private final String idCurrentUser;

    //FOR COMMUNICATION
    private Listener callback;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MyChatRecyclerViewAdapter(@NonNull FirestoreRecyclerOptions<Message> options, RequestManager glide,
    Listener callback, String idCurrentUser) {
        super(options);
        this.glide = glide;
        this.callback = callback;
        this.idCurrentUser = idCurrentUser;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i, @NonNull Message message) {
        messageViewHolder.updateWithMessage(message, this.idCurrentUser, this.glide);
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext())
        .inflate(R.layout.activity_chat_item, parent, false));
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        this.callback.onDataChanged();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private final int colorCurrentUser;
        private final int colorRemoteUser;

        private ImageView imageViewProfile;
        private TextView tvMessage, tvDate;
        private LinearLayout profileContainer,messageContainer;
        private RelativeLayout rootView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            colorCurrentUser = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimary);
            colorRemoteUser = ContextCompat.getColor(itemView.getContext(), R.color.colorPrimarySoft);
            imageViewProfile = itemView.findViewById(R.id.imgProfileChat);
            tvMessage = itemView.findViewById(R.id.tvTxtChat);
            tvDate = itemView.findViewById(R.id.tvTimeChat);
            profileContainer = itemView.findViewById(R.id.containerChatProfile);
            messageContainer = itemView.findViewById(R.id.containerChatTxt);
            rootView = itemView.findViewById(R.id.chat_root_view);
        }

        public void updateWithMessage(Message message, String currentUserId, RequestManager glide){

            // Check if current user is the sender
            Boolean isCurrentUser = message.getUserSender().getUid().equals(currentUserId);

            // Update message TextView
            this.tvMessage.setText(message.getMessage());
            this.tvMessage.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);

            // Update date TextView
            if (message.getDateCreated() != null) this.tvDate.setText(this.convertDateToHour(message.getDateCreated()));

            // Update profile picture ImageView
            if (message.getUserSender().getUrlPicture() != null)
                glide.load(message.getUserSender().getUrlPicture())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);

//            // Update image sent ImageView
//            if (message.getUrlImage() != null){
//                glide.load(message.getUrlImage())
//                        .into(imageViewSent);
//                this.imageViewSent.setVisibility(View.VISIBLE);
//            } else {
//                this.imageViewSent.setVisibility(View.GONE);
//            }

            //Update Message Bubble Color Background
            ((GradientDrawable) messageContainer.getBackground()).setColor(isCurrentUser ? colorCurrentUser : colorRemoteUser);

            // Update all views alignment depending is current user or not
            this.updateDesignDependingUser(isCurrentUser);
        }

        private void updateDesignDependingUser(Boolean isSender){

            // PROFILE CONTAINER
            RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsLayoutHeader.addRule(isSender ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.ALIGN_PARENT_LEFT);
            this.profileContainer.setLayoutParams(paramsLayoutHeader);

            // MESSAGE CONTAINER
            RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsLayoutContent.addRule(isSender ? RelativeLayout.LEFT_OF : RelativeLayout.RIGHT_OF, R.id.containerChatProfile);
            this.messageContainer.setLayoutParams(paramsLayoutContent);

//            // CARDVIEW IMAGE SEND
//            RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            paramsImageView.addRule(isSender ? RelativeLayout.ALIGN_LEFT : RelativeLayout.ALIGN_RIGHT, R.id.activity_mentor_chat_item_message_container_text_message_container);
//            this.cardViewImageSent.setLayoutParams(paramsImageView);

            this.rootView.requestLayout();
        }

        // ---

        private String convertDateToHour(Date date){
            DateFormat dfTime = new SimpleDateFormat("HH:mm");
            return dfTime.format(date);
        }
    }
}
