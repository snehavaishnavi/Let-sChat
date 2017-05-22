package com.example.amrut.easychatapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserPageActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,InboxAdapter.InboxInterface{
    private DatabaseReference mDatabase;
    RecyclerView usersRecyclerView,inboxRecyclerView;
    UsersDetailsAdapter usersDetailsAdapter;
    InboxAdapter inboxAdapter;
    ArrayList<User> allUsersList=new ArrayList<>();
    ArrayList<InboxObj> allInboxList=new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private RecyclerView.LayoutManager horizontalLayoutManager,verticalLayoutManager;
    private CharSequence options[] = new CharSequence[] {"View Profile", "Send Message"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_page);
        horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        verticalLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        usersRecyclerView= (RecyclerView) findViewById(R.id.usersRecyclerView);
        inboxRecyclerView= (RecyclerView) findViewById(R.id.inboxRecyclerView);
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(usersRecyclerView);
        snapHelper.attachToRecyclerView(inboxRecyclerView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("827209507255-thhd3ke4u97orb60ngnqqt7u5n9377na.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* MainActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mDatabase= FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsersList.clear();
                Log.d("demo","datasnapshot"+dataSnapshot.getChildren().toString());
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    DatabaseReference dbRefUser= ds.getRef();
                    dbRefUser.child("profile").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user=dataSnapshot.getValue(User.class);
                            if (!user.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                                allUsersList.add(user);
                            }
                            //set users to recycler view
                            showAllUsers();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("inboxObjs").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allInboxList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    InboxObj inboxObj=ds.getValue(InboxObj.class);
                    Log.d("demo","inboxMsg "+inboxObj.toString());
                    allInboxList.add(inboxObj);
                }
                showInboxItems();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.user_menu_options,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_option_view_profile:
                Intent intent= new Intent(this,ProfileDetailsActivity.class);
                startActivity(intent);
                return true;
            case R.id.item_option_logout:
                FirebaseAuth.getInstance().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                Intent intent1=new Intent(this, MainActivity.class);
                startActivity(intent1);
                finish();

                LoginManager.getInstance().logOut();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showAllUsers(){
        if (allUsersList!=null && !allUsersList.isEmpty()){
            usersDetailsAdapter= new UsersDetailsAdapter(UserPageActivity.this,allUsersList);
            usersRecyclerView.setLayoutManager(horizontalLayoutManager);
            usersRecyclerView.setAdapter(usersDetailsAdapter);
            usersDetailsAdapter.notifyDataSetChanged();
            usersRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(UserPageActivity.this,usersRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, final int position) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserPageActivity.this);
                    builder.setTitle("Choose one");
                    builder.setCancelable(true);
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which==0){
                                User clickedUser=allUsersList.get(position);
                                Intent intent=new Intent(UserPageActivity.this,ViewUserProfile.class);
                                intent.putExtra("clickedUser",clickedUser);
                                startActivity(intent);
                                finish();
                            }else if (which==1){
                                //go to Chat Activity
                                Intent intent=new Intent(UserPageActivity.this,ChatActivity.class);
                                intent.putExtra("receivingUser",allUsersList.get(position));
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
                    builder.show();
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            }));
        }
    }

    public void showInboxItems(){
        if (!allInboxList.isEmpty() && allInboxList!=null){
            Log.d("demo","allInboxList"+allInboxList.toString());
            inboxAdapter=new InboxAdapter(allInboxList,UserPageActivity.this);
            inboxRecyclerView.setLayoutManager(verticalLayoutManager);
            inboxRecyclerView.setAdapter(inboxAdapter);
            inboxAdapter.notifyDataSetChanged();


        }else {
            Toast.makeText(getApplicationContext(),"Your inbox is empty..start a conversation with any of the users",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("demo","ONConnectionFAiled:"+connectionResult);
    }

    @Override
    public void onItemClick(User user) {
        Intent intent = new Intent(UserPageActivity.this,ChatActivity.class);
        intent.putExtra("receivingUser",user);
        startActivity(intent);
    }
}
