package com.example.myapplication;

import static android.widget.Toast.LENGTH_LONG;

import static com.example.myapplication.EditOutfitActivity.EDITED_OUTFIT_BUNDLE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ClothesFragment.OnClothesSelectedListener, OutfitsFragment.OnFragmentInteractionListener {
    static final int INTENT_ADD_CLOTHES = 1;
    static final int INTENT_ADD_OUTFIT = 2;
    static final int INTENT_EDIT_CLOTHES = 3;
    static final int INTENT_EDIT_OUTFIT = 4;
    public static final int MENU_CLOTHES = 1;
    public static final int MENU_OUTFITS = 2;
    private ClothesFragment clothesFragment;
    private OutfitsFragment outfitsFragment;



    private int activeMenu;
    private AppDatabase Database;
    private ImageHandle imageHandle;

    private static final String ACTIVEMENU = "com.example.myapplication.activeMenu";
    public static final String CLOTHES_ID = "com.example.myapplication.clothesId";
    private static final String TAG="MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation =  findViewById(R.id.navigation);
        navigation.setOnItemSelectedListener(mOnNavigationItemSelectedListener);

        Database = AppDatabase.getAppDatabase(getApplicationContext());
        imageHandle = new ImageHandle(this, findViewById(R.id.fragment_container));


        activeMenu = MENU_CLOTHES;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user== null){
            mystartActivity(LoginActivity.class);

        }else{
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if(document!=null){
                            if (document.exists()) {

                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        } else {
                                Log.d(TAG, "No such document");
                                mystartActivity(MemberActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });

        }

        findViewById(R.id.btn_logout).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()){
                case R.id.btn_logout:
                    FirebaseAuth.getInstance().signOut();
                    mystartActivity(LoginActivity.class);
                    break;

            }
        }
    };

    private void mystartActivity(Class c){
        Intent intent= new Intent(this,c);
        startActivity(intent);
    }

    private NavigationBarView.OnItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@androidx.annotation.NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_clothes:

                    if(activeMenu == MENU_CLOTHES)
                        reloadFragment();
                    else
                    {
                        activeMenu = MENU_CLOTHES;
                        updateFragments();
                    }


                    break;
                case R.id.navigation_outfit:
                    if(activeMenu == MENU_OUTFITS)
                        reloadFragment();
                    else
                    {
                        activeMenu = MENU_OUTFITS;
                        updateFragments();
                    }
                    break;
                default:
                    return false;
            }

            return true;
        }
    };

    @Override
    public void addClothes() {

        Intent addClothesIntent = new Intent(this, AddClothesActivity.class);
        startActivityForResult(addClothesIntent, INTENT_ADD_CLOTHES);
    }



    @Override
    public void dispatchEditClothes(Clothes clothes) {

        Intent intent = new Intent(this, AddClothesActivity.class);
        intent.putExtra(CLOTHES_ID, clothes.getId());
        startActivityForResult(intent, INTENT_EDIT_CLOTHES);

    }

    @Override
    public void dispatchEditOutfit(Outfit outfit)
    {
        Intent intent = new Intent(this, EditOutfitActivity.class);
        intent.putExtra(EditOutfitActivity.OUTFIT_BUNDLE, outfit.toBundle());
        startActivityForResult(intent, INTENT_EDIT_OUTFIT);

    }

    @Override
    public void setCurrentOutfitsFragment(OutfitsFragment fragment) {
        outfitsFragment = fragment;

    }



    @Override
    public void deleteOutfits(List<Outfit> outfitsToDelete) {

        for(Outfit outfit:outfitsToDelete)
        {
            Database.outfitDao().deleteOutfit(outfit);
        }

    }

    @Override
    public void addOutfits() {

        Intent addOutfitsIntent = new Intent(this, FilterClothesActivity.class);
        startActivityForResult(addOutfitsIntent, INTENT_ADD_OUTFIT);
        activeMenu = MENU_OUTFITS;
        updateFragments();


    }

    @Override
    public void setCurrentClothesFragment(ClothesFragment fragment) {
        clothesFragment = fragment;
    }

    @Override
    public Outfit saveOutfit(ArrayList<Integer> clothesToAddIDS) {


        Outfit outfit = new Outfit(clothesToAddIDS);
        outfit.sortClothesListByType(Database);
        Database.outfitDao().insertOutfit(outfit);

        Snackbar quickMessage = Snackbar.make(findViewById(R.id.fragment_container), R.string.added_successfully, LENGTH_LONG);
        quickMessage.show();

        return outfit;

    }

    public void updateDatabase() {

        if(Database == null)
            Database = AppDatabase.getAppDatabase(getApplicationContext());
        List<Clothes> listOfClothes = Database.clothesDao().getAll();
        Iterator<Clothes> iterator = listOfClothes.iterator();

        List<Clothes> clothesToDelete = new ArrayList<>();
        while(iterator.hasNext()) {
            Clothes clothes = iterator.next();
            if (clothes.getImagePath() == null || !(new File(clothes.getImagePath())).exists()) {
                //delete ones that dont have an image
                clothesToDelete.add(clothes);
            }
        }
        deleteClothes(clothesToDelete);
        clothesToDelete.clear();
        clothesToDelete = null;
        //create thumbnails id they dont exist

        for (Clothes clothes : listOfClothes) {
            if (clothes.getThumbnailPath() == null){
                clothes.setThumbnailPath(imageHandle.createImageFile(ImageHandle.PERMANENT, "THUMBNAIL"));
                Database.clothesDao().updateClothes(clothes);
            }
            if(!(new File(clothes.getThumbnailPath())).exists()) {
                imageHandle.createAndSaveThumbnail(clothes.getThumbnailPath(), clothes.getImagePath());

            }
        }

        List<Outfit> listOfOutfits = Database.outfitDao().getAll();
        for(Outfit outfit:listOfOutfits)
        {
            int id = outfit.getId();
            if(outfit.getClothesIDS().size() == 0)
            {
                Database.outfitDao().deleteOutfit(outfit);
                continue;
            }
            for(int idClothes: outfit.getClothesIDS())
            {
                if(Database.clothesDao().getClothesFromId(idClothes) == null)
                {
                    Outfit outfitUpdtd = Database.outfitDao().getOutfitFromId(outfit.getId());
                    ArrayList<Integer> clothesIds = outfitUpdtd.getClothesIDS();
                    clothesIds.remove((Object) idClothes);
                    outfitUpdtd.setClothesIDS(clothesIds);
                    Database.outfitDao().updateOutfit(outfitUpdtd);

                }
            }
            if(Database.outfitDao().getOutfitFromId(outfit.getId()).getClothesIDS().size() == 0)
            {
                Database.outfitDao().deleteOutfit(Database.outfitDao().getOutfitFromId(outfit.getId()));
            }

        }
    }




    @Override
    public void deleteClothes(List<Clothes> clothesToDelete)
    {
        if(Database == null)
            Database = AppDatabase.getAppDatabase(getApplicationContext());

        if(clothesToDelete == null)
            return;

        for(Clothes clothes:clothesToDelete)
        {
            if(clothes.getImagePath() != null)
                (new File(clothes.getImagePath())).delete();
            if(clothes.getThumbnailPath() != null)
                (new File(clothes.getThumbnailPath())).delete();

            Database.clothesDao().deleteClothes(clothes);
        }



    }


    //TODO
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Snackbar quickMessage;
            switch (requestCode) {

                case INTENT_ADD_CLOTHES:
                    quickMessage = Snackbar.make(findViewById(R.id.fragment_container), R.string.added_successfully, LENGTH_LONG);
                    quickMessage.show();
                    activeMenu = MENU_CLOTHES;
                    reloadFragment();
                    break;

                case INTENT_ADD_OUTFIT:
                    Outfit outfit = saveOutfit(data.getIntegerArrayListExtra(Outfit.OUTFIT_LIST_CLOTHES_IDS));
                    quickMessage = Snackbar.make(findViewById(R.id.fragment_container), R.string.added_successfully, LENGTH_LONG);
                    quickMessage.show();
                    activeMenu = MENU_OUTFITS;
                    dispatchEditOutfit(outfit);
                    break;

                case INTENT_EDIT_CLOTHES:
                    quickMessage = Snackbar.make(findViewById(R.id.fragment_container), "Updated Successfully", LENGTH_LONG);
                    quickMessage.show();
                    activeMenu = MENU_CLOTHES;
                    reloadFragment();
                    break;

                case INTENT_EDIT_OUTFIT:
                    updateOutfit(Outfit.outfitFromBundle(data.getBundleExtra(EDITED_OUTFIT_BUNDLE)));
                    quickMessage = Snackbar.make(findViewById(R.id.fragment_container), "Updated Successfully", LENGTH_LONG);
                    quickMessage.show();
                    activeMenu = MENU_OUTFITS;
                    reloadFragment();
                    break;
            }

        } else {
            switch (requestCode) {

                case INTENT_ADD_CLOTHES:
                    activeMenu = MENU_CLOTHES;
                    reloadFragment();
                    break;

                case INTENT_ADD_OUTFIT:
                    activeMenu = MENU_OUTFITS;
                    reloadFragment();
                    break;

                case INTENT_EDIT_CLOTHES:
                    activeMenu = MENU_CLOTHES;
                    reloadFragment();
                    break;

                case INTENT_EDIT_OUTFIT:
                    activeMenu = MENU_OUTFITS;
                    reloadFragment();
                    break;
            }

        }

    }

    private void updateOutfit(Outfit outfit) {

        Database.outfitDao().updateOutfit(outfit);
    }


    @Override
    public void reloadFragment()
    {
        if(activeMenu == MENU_CLOTHES)
            clothesFragment = null;
        if(activeMenu == MENU_OUTFITS)
            outfitsFragment = null;
        updateFragments();

    }




    private void updateFragments()
    {
        switch (activeMenu)
        {
            case MENU_CLOTHES:
                setTitle(R.string.title_clothes);
                if(clothesFragment == null)
                {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, new ClothesFragment());
                    transaction.commitAllowingStateLoss();

                }
                else
                {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                   transaction.replace(R.id.fragment_container, clothesFragment);
                    transaction.commitAllowingStateLoss();
                }


                break;
            case MENU_OUTFITS:
                setTitle(R.string.title_outfit);
                if(outfitsFragment == null)
                {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                   transaction.replace(R.id.fragment_container, new OutfitsFragment());
                    transaction.commitAllowingStateLoss();

                }
                else
                {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment_container, outfitsFragment);
                    transaction.commitAllowingStateLoss();
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState.putInt(ACTIVEMENU, activeMenu);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        activeMenu = savedInstanceState.getInt(ACTIVEMENU, MENU_CLOTHES);
        super.onRestoreInstanceState(savedInstanceState);

    }


    @Override
    public void onResume()
    {
        super.onResume();
        updateFragments();
    }


    @Override
    public void onBackPressed()
    {
        if(activeMenu == MENU_CLOTHES && clothesFragment != null && clothesFragment.isSelectMode())
        {
            clothesFragment.cancelSelectMode();
        }
        if(activeMenu == MENU_OUTFITS && outfitsFragment != null && outfitsFragment.isSelectMode())
        {
            outfitsFragment.cancelSelectMode();
        }
        else
            super.onBackPressed();
    }
}



