package com.repalova.donorapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.repalova.donorapplication.Adapter.UserAdapter;
import com.repalova.donorapplication.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private DrawerLayout drawerLayout;
    private NavigationView nav_view;

    private CircleImageView nav_profile_image;
    private TextView nav_fullname, nav_email, nav_bloodgroup, nav_type;

    private ProgressBar progressbar;

    private List<User> userList;
    private UserAdapter userAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Donor App");

        drawerLayout = findViewById(R.id.drawerLayout);
        nav_view = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        nav_view.setNavigationItemSelectedListener(this);

        progressbar = findViewById(R.id.progressbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        userList = new ArrayList<>();
        userAdapter = new UserAdapter(MainActivity.this, userList);

        recyclerView.setAdapter(userAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                if (type.equals("donor")){
                    readRecipients();
                }else {
                    readDonors();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        nav_profile_image = nav_view.getHeaderView(0).findViewById(R.id.nav_user_image);
        nav_fullname = nav_view.getHeaderView(0).findViewById(R.id.nav_user_fullname);
        nav_email = nav_view.getHeaderView(0).findViewById(R.id.nav_user_email);
        nav_bloodgroup = nav_view.getHeaderView(0).findViewById(R.id.nav_user_bloodgroup);
        nav_type = nav_view.getHeaderView(0).findViewById(R.id.nav_user_type);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        );

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                    nav_fullname.setText(name);

                    String email = Objects.requireNonNull(snapshot.child("email").getValue()).toString();
                    nav_email.setText(email);

                    String bloodgroup = Objects.requireNonNull(snapshot.child("bloodgroup").getValue()).toString();
                    nav_bloodgroup.setText(bloodgroup);

                    String type = Objects.requireNonNull(snapshot.child("type").getValue()).toString();
                    nav_type.setText(type);

                    if (snapshot.hasChild("profilepicturleurl")){
                        String imageUrl = Objects.requireNonNull(snapshot.child("profilepictureurl").getValue()).toString();
                        Glide.with(getApplicationContext()).load(imageUrl).into(nav_profile_image);
                    } else {
                        nav_profile_image.setImageResource(R.drawable.profile_image);
                    }

                    Menu nav_menu = nav_view.getMenu();

                    if (type.equals("donor")){
                        nav_menu.findItem(R.id.sentEmail).setTitle("Полученные письма");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readDonors() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("donor");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()){
                    Toast.makeText(MainActivity.this, "No recipients", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readRecipients() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("users");
        Query query = reference.orderByChild("type").equalTo("recipient");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
                progressbar.setVisibility(View.GONE);

                if (userList.isEmpty()){
                    Toast.makeText(MainActivity.this, "No donors", Toast.LENGTH_SHORT).show();
                    progressbar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.oplus) {
            Intent intent3 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent3.putExtra("group", "I(0)+");
            startActivity(intent3);
        } else if (itemId == R.id.ominus) {
            Intent intent4 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent4.putExtra("group", "I(0)-");
            startActivity(intent4);
        } else if (itemId == R.id.aplus) {
            Intent intent5 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent5.putExtra("group", "II(A)+");
            startActivity(intent5);
        } else if (itemId == R.id.aminus) {
            Intent intent6 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent6.putExtra("group", "II(A)-");
            startActivity(intent6);
        } else if (itemId == R.id.bplus) {
            Intent intent7 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent7.putExtra("group", "III(B)+");
            startActivity(intent7);
        } else if (itemId == R.id.bminus) {
            Intent intent8 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent8.putExtra("group", "III(B)-");
            startActivity(intent8);
        } else if (itemId == R.id.abplus) {
            Intent intent9 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent9.putExtra("group", "IV(AB)+");
            startActivity(intent9);
        } else if (itemId == R.id.abminus) {
            Intent intent10 = new Intent(MainActivity.this, CategorySelectedActivity.class);
            intent10.putExtra("group", "IV(AB)-");
            startActivity(intent10);
        } else if (itemId == R.id.sentEmail) {
            Intent intent11 = new Intent(MainActivity.this, SentEmailActivity.class);
            startActivity(intent11);
        } else if (itemId == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.about) {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

}