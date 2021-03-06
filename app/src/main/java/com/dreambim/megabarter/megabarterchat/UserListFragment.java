package com.dreambim.megabarter.megabarterchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserListFragment extends Fragment{


    private static final String TAG = "UserListFragment";
    public static final String USERS_CHILD = "users";
    public static final String ANONYMOUS = "anonymous";
    public static final String USER_ID = "user_id";
    //private static final String USERS_URL = "https://megabarterchat.firebaseio.com/users";

    private String mUsername;
    private SharedPreferences mSharedPreferences;

    private RecyclerView mChatRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Users, userViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private EditText mMessageEditText;


    public static class userViewHolder extends RecyclerView.ViewHolder{
        public TextView userTextView;
        public TextView emailTextView;


        public userViewHolder(View v) {
            super(v);

            userTextView = (TextView) itemView.findViewById(R.id.userTextView);
            emailTextView = (TextView) itemView.findViewById(R.id.emailTextView);
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mChatRecyclerView = (RecyclerView) view.findViewById(R.id.user_recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());

        //mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(getActivity(), EmailPasswordActivity.class));
            getActivity().finish();
            //return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            Log.d(TAG, "Username: " + mUsername + " mFirebaserUserId = " + mFirebaseUser.getUid());
            //mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Users, userViewHolder> (
                Users.class,
                R.layout.item_user,
                userViewHolder.class,
                mFirebaseDatabaseReference.child(USERS_CHILD)) {



            @Override
            protected Users parseSnapshot(DataSnapshot snapshot) {
                Users users = super.parseSnapshot(snapshot);
                Log.d(TAG, "users.getid " + users.getId() + "mFirebaserUserId = " + mFirebaseUser.getUid());
                if (users != null ) {
                    users.setId(snapshot.getKey());
                }

                return users;
            }

            @Override
            protected void populateViewHolder(userViewHolder viewHolder, Users users, int position) {
                final Users mUsers = users;
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                //if(!users.getId().contentEquals(mFirebaseUser.getUid())){
                    viewHolder.userTextView.setText(users.getName());
                    viewHolder.emailTextView.setText(users.getEmail());
                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.d(TAG, "Item clicked: " + mUsers.getId());
                            final Intent intent = new Intent(getActivity(), ChatListActivity.class);
                            intent.putExtra(USER_ID, mUsers);
                            startActivity(intent);
                        }
                    });
                /*} else {
                  viewHolder.itemView.setVisibility(view.GONE);               }*/

            }

        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int usersCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (usersCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mChatRecyclerView.scrollToPosition(positionStart);
                }
            }
        });


        mChatRecyclerView.setLayoutManager(mLinearLayoutManager);
        mChatRecyclerView.setAdapter(mFirebaseAdapter);





        return view;
    }



    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mFirebaseUser = null;
                mUsername = ANONYMOUS;

                startActivity(new Intent(getActivity(), EmailPasswordActivity.class));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
