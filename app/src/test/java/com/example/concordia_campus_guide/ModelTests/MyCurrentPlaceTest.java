package com.example.concordia_campus_guide.ModelTests;

import com.example.concordia_campus_guide.Models.MyCurrentPlace;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class MyCurrentPlaceTest {
    private MyCurrentPlace currentPlace;

    @Before
    public void init() {
        currentPlace = new MyCurrentPlace(-73.57907921075821, 45.49702057370776);
    }

    @Test
    public void getDisplayNameTest(){
        MyCurrentPlace currentPlaceEmpty = new MyCurrentPlace();
        assertEquals(currentPlaceEmpty.getDisplayName(), "Select location");
        assertEquals(currentPlace.getDisplayName(), "My location: -73.57907921075821, 45.49702057370776");
    }
}
