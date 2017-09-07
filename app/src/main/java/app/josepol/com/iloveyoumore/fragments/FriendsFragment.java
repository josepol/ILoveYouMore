package app.josepol.com.iloveyoumore.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import app.josepol.com.iloveyoumore.ViewModels.UserViewModel;
import app.josepol.com.iloveyoumore.R;
import app.josepol.com.iloveyoumore.utils.SessionUtils;

public class FriendsFragment extends Fragment {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private ArrayList<UserViewModel> userViewModels = new ArrayList<>();
    private FriendsAdapter friendsAdapter;
    private RecyclerView recyclerView;
    private TextView findFriendsText;

    public FriendsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.recyclerView);
        findFriendsText = (TextView) root.findViewById(R.id.findFriendsText);

        SessionUtils session = new SessionUtils(getContext());
        final String sessionId = session.getId();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        DatabaseReference usersDatabaseReference = databaseReference.child("users");

        usersDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                DataSnapshot userDataSnapshot = dataSnapshot.child(sessionId);

                for (DataSnapshot userChildrens: userDataSnapshot.getChildren()) {
                    if(userChildrens.getKey().equals("friends")) {
                        for(DataSnapshot friendDataSnapshot: userChildrens.getChildren()) {
                            String friendId = friendDataSnapshot.getValue().toString();
                            DataSnapshot friendChildrens = dataSnapshot.child(friendId);

                            String friendName = friendChildrens.child("userName").getValue().toString();

                            UserViewModel userViewModel = new UserViewModel();
                            userViewModel.setUserName(friendName);

                            userViewModels.add(userViewModel);

                        }
                    }
                }

                friendsAdapter = new FriendsAdapter(userViewModels, getContext());

                recyclerView.setAdapter(friendsAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

                recyclerView.addOnItemTouchListener(
                        new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                            @Override public void onItemClick(View view, int position) {
                                Toast.makeText(getContext(), "Username " + userViewModels.get(position).getUserName() , Toast.LENGTH_LONG).show();
                            }

                            @Override public void onLongItemClick(View view, int position) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("¿Deseas eliminar el amigo?");
                                builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getContext(), "Deletttttiiing", Toast.LENGTH_LONG).show();
                                    }
                                });
                                builder.setNegativeButton("Cancelar", null);
                                builder.show();
                            }
                        })
                );

                findFriendsText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        friendsAdapter.filter(findFriendsText.getText().toString());
                    }
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return root;
    }

}

class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private ArrayList<UserViewModel> userViewModels, usersFilter;
    private Context context;

    public FriendsAdapter(ArrayList<UserViewModel> userViewModels, Context context) {

        this.usersFilter = new ArrayList<>();

        this.userViewModels = userViewModels;
        this.usersFilter.addAll(this.userViewModels);
        this.context = context;
    }

    @Override
    public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friends_recycler_item_content, parent, false);

        FriendsViewHolder friendsViewHolder = new FriendsViewHolder(itemView);

        return friendsViewHolder;
    }

    @Override
    public void onBindViewHolder(FriendsViewHolder holder, int position) {
        UserViewModel userViewModel = usersFilter.get(position);
        holder.bindFriend(userViewModel);
    }

    @Override
    public int getItemCount() {
        return usersFilter.size();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitulo;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            txtTitulo = (TextView)itemView.findViewById(R.id.person_name);
        }

        public void bindFriend(UserViewModel userViewModel) {
            txtTitulo.setText(userViewModel.getUserName());
        }
    }

    public void filter(final String text) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                usersFilter.clear();

                if (TextUtils.isEmpty(text)) {
                    usersFilter.addAll(userViewModels);
                } else {
                    for (UserViewModel userViewModel : userViewModels) {
                        if (userViewModel.getUserName() != null && userViewModel.getUserName().toLowerCase().contains(text.toLowerCase())) {
                            usersFilter.add(userViewModel);
                        }
                    }
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });

            }
        }).start();

    }
}

class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                }
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
}
