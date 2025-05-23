package com.example.SynCalendar.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.SynCalendar.Model;
import com.example.SynCalendar.R;
import com.example.SynCalendar.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class RequestAdapter extends ArrayAdapter<User> {
    private Context context;
    private List<User> users;
    private Model model;
    private User currentUser;
    private ArrayAdapter<User> followersAdapter;
    private List<User> followersList;

    public RequestAdapter(Context context, List<User> users, ArrayAdapter<User> followersAdapter, List<User> followersList) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        this.followersAdapter = followersAdapter;
        this.followersList = followersList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        }

        model = Model.getInstance(context);
        currentUser = model.getCurrentUser();
        User requester = users.get(position); // This user sent the request

        TextView tvUName = convertView.findViewById(R.id.tvUName);
        Button btnAccept = convertView.findViewById(R.id.btnAccept);
        Button btnReject = convertView.findViewById(R.id.btnReject);

        tvUName.setText(requester.getuName());

        // Handle accept button click
        btnAccept.setOnClickListener(v -> {
            // Get the latest data
            Map<String, String> requests = currentUser.getRequests();

            if (requests.containsKey(requester.getId())) {
                // Accept the request
                String username = requests.get(requester.getId());
                currentUser.approveFollowRequest(requester.getId());
                
                // Update requester's following list
                requester.addFollowing(currentUser.getId(), currentUser.getuName());

                // Update Firestore for both users
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                
                db.collection("users")
                    .document(currentUser.getId())
                    .set(currentUser)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("RequestAdapter", "Current user updated successfully");
                        // Remove from the requests list
                        users.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Request accepted", Toast.LENGTH_SHORT).show();
                        
                        // Add to followers list if not already there
                        if (!followersList.contains(requester)) {
                            followersList.add(requester);
                            if (followersAdapter != null) {
                                followersAdapter.notifyDataSetChanged();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RequestAdapter", "Error updating current user", e);
                        Toast.makeText(context, "Error accepting request", Toast.LENGTH_SHORT).show();
                    });

                db.collection("users")
                    .document(requester.getId())
                    .set(requester)
                    .addOnFailureListener(e -> 
                        Log.e("RequestAdapter", "Error updating requester", e));
            }
        });

        // Handle reject button click
        btnReject.setOnClickListener(v -> {
            Map<String, String> requests = currentUser.getRequests();
            
            if (requests.containsKey(requester.getId())) {
                // Reject the request
                currentUser.denyFollowRequest(requester.getId());

                // Update Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users")
                    .document(currentUser.getId())
                    .set(currentUser)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("RequestAdapter", "Request rejected successfully");
                        // Remove from the adapter's list
                        users.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Request rejected", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RequestAdapter", "Error rejecting request", e);
                        Toast.makeText(context, "Error rejecting request", Toast.LENGTH_SHORT).show();
                    });
            }
        });

        return convertView;
    }
}
