package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Created by Diogo on 08/02/2018.
 */

@Dao
public interface OutfitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOutfit(Outfit outfit);

    @Update
    void updateOutfit(Outfit outfit);

    @Delete
    void deleteOutfit(Outfit outfit);

    @Query("SELECT * FROM outfit WHERE id = :id LIMIT 1")
    Outfit getOutfitFromId(int id);

    @Query("SELECT * FROM outfit")
    List<Outfit> getAll();
}
