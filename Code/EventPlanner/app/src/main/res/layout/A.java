<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:orientation="vertical"
android:background="#2A2940"
android:padding="16dp"
android:layout_marginBottom="12dp">

    <TextView
android:id="@+id/txt_admin_event_name"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:text="Event Name"
android:textColor="#FFFFFF"
android:textStyle="bold"
android:textSize="18sp"
android:layout_marginBottom="8dp" />

    <TextView
android:id="@+id/txt_admin_event_date"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:text="Date: N/A"
android:textColor="#DDDDDD"
android:textSize="14sp"
android:layout_marginBottom="4dp" />

    <TextView
android:id="@+id/txt_admin_event_location"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:text="Location: N/A"
android:textColor="#DDDDDD"
android:textSize="14sp"
android:layout_marginBottom="4dp" />

    <TextView
android:id="@+id/txt_admin_event_status"
android:layout_width="match_parent"
android:layout_height="wrap_content"
android:text="Status: N/A"
android:textColor="#DDDDDD"
android:textSize="14sp"
android:layout_marginBottom="12dp" />

    <Button
android:id="@+id/btn_admin_remove_event"
android:layout_width="wrap_content"
android:layout_height="wrap_content"
android:text="Remove Event"
android:textAllCaps="false"
android:textColor="#FFFFFF"
android:background="@drawable/button_purple" />

</LinearLayout>