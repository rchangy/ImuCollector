package com.example.imucollector.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imucollector.R;
import com.example.imucollector.data.Session;
import com.example.imucollector.database.SessionRepository;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

public class SessionListAdapter extends ListAdapter<Session, SessionViewHolder> {
    private final List<Session> selectedSession;

    public SessionListAdapter(@NonNull DiffUtil.ItemCallback<Session> diffCallback, List<Session> selectedSession){
        super(diffCallback);
        this.selectedSession = selectedSession;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return SessionViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        Session current = getItem(position);
        boolean checked = selectedSession.contains(current);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = holder.getCheckBox();
                if (checkBox.isChecked() && !selectedSession.contains(current)) {
                    selectedSession.add(current);
                } else if (!checkBox.isChecked() && selectedSession.contains(current)) {
                    selectedSession.remove(current);
                }
            }
        };
        holder.bind(String.valueOf(current.recordId), String.valueOf(current.sessionId), DateFormat.getDateInstance().format(current.getDate()),
                checked, listener);

    }

    public void deleteSelectedSessions(){
        SessionRepository.getInstance().deleteSessions(selectedSession.toArray(new Session[0]));
        selectedSession.clear();
    }

    static class SessionDiff extends DiffUtil.ItemCallback<Session> {
        @Override
        public boolean areItemsTheSame(@NonNull Session oldItem, @NonNull Session newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Session oldItem, @NonNull Session newItem) {
            return oldItem.timestamp == newItem.timestamp;
        }
    }
}

class SessionViewHolder extends RecyclerView.ViewHolder{
    private final TextView textRecordId;
    private final TextView textSessionId;
    private final TextView textTimestamp;
    private final CheckBox checkBox;

    private SessionViewHolder(View itemView){
        super(itemView);
        textRecordId = itemView.findViewById(R.id.session_item_text_record_id);
        textSessionId = itemView.findViewById(R.id.session_item_text_session_id);
        textTimestamp = itemView.findViewById(R.id.session_item_text_timestamp);
        checkBox = itemView.findViewById(R.id.session_item_checkbox);
    }

    public CheckBox getCheckBox() { return checkBox; }

    public void bind(String recordId, String sessionId, String timestamp, boolean checked, View.OnClickListener listener){
        textRecordId.setText(recordId);
        textSessionId.setText(sessionId);
        textTimestamp.setText(timestamp);
        checkBox.setOnClickListener(null);
        checkBox.setChecked(checked);
        checkBox.setOnClickListener(listener);
    }

    static SessionViewHolder create(ViewGroup parent){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.session_item, parent, false);
        return new SessionViewHolder(view);
    }
}