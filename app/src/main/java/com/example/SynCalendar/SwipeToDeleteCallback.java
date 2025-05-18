package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.example.SynCalendar.Adapters.AllEventsAdapter;
import com.google.android.material.snackbar.Snackbar;
import java.util.List;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
    private final AllEventsAdapter adapter;
    private final List<Event> events;
    private final Context context;
    private final Model model;

    public SwipeToDeleteCallback(AllEventsAdapter adapter, List<Event> events, Context context, Model model) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.events = events;
        this.context = context;
        this.model = model;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false; // Drag & drop not needed
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Event deletedEvent = events.get(position);

        // Remove from RecyclerView
        events.remove(position);
        adapter.notifyItemRemoved(position);

        // Show Snackbar with Undo option
        Snackbar snackbar = Snackbar.make(viewHolder.itemView, "Event deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            events.add(position, deletedEvent);
            adapter.notifyItemInserted(position);
        });

        // Delete from database only if NOT undone
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    model.deleteEvent(deletedEvent); // Use the deleteEvent method
                }
            }
        });

        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }
}
