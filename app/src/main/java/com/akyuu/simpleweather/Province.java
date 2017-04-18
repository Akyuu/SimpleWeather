package com.akyuu.simpleweather;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = AppDatabase.class)
public class Province extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;

    @Column
    @Unique
    long code;

}
