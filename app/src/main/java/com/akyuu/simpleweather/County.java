package com.akyuu.simpleweather;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = AppDatabase.class)
public class County extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    @Column
    public String name;

    @Column
    public String weatherId;

    @Column
    public long cityId;
}
