package com.example.concordia_campus_guide.Activities;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.concordia_campus_guide.EspressoHelpers;
import com.example.concordia_campus_guide.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ExplorePOIOptionsTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION",
                    "android.permission.ACCESS_COARSE_LOCATION");

    @Test
    public void explorePOIOptionsTest() {
        android.os.SystemClock.sleep(500);
        ViewInteraction frameLayout = onView(
                allOf(withId(R.id.bottom_card_frame),
                        childAtPosition(
                                allOf(withId(R.id.bottom_card_scroll_view),
                                        childAtPosition(
                                                withId(R.id.bottom_card_coordinator_layout),
                                                0)),
                                0),
                        isDisplayed()));
        frameLayout.perform(EspressoHelpers.hiddenClick());
        android.os.SystemClock.sleep(500);

        ViewInteraction imageView = onView(
                allOf(EspressoHelpers.getElementFromMatchAtPosition(allOf(withId(R.id.serviceIv)), 1),
                        isDisplayed()));

        imageView.check(matches(isDisplayed()));

        ViewInteraction tabView = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withId(R.id.dotsTab),
                                0),
                        1),
                        isDisplayed()));
        tabView.perform(click());

        ViewInteraction imageView1 = onView(
                allOf(EspressoHelpers.getElementFromMatchAtPosition(allOf(withId(R.id.serviceIv)), 1),
                        isDisplayed()));

        ViewInteraction textView = onView(
                allOf(withId(R.id.info_card_title), withText("Explore"),
                        childAtPosition(
                                allOf(withId(R.id.linearLayout),
                                        childAtPosition(
                                                IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                                0)),
                                0),
                        isDisplayed()));
        textView.check(matches(withText("Explore")));

        frameLayout.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
